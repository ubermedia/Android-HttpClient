package com.levelup.http.ion;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import org.apache.http.protocol.HTTP;

import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.DataSink;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.TransformFuture;
import com.koushikdutta.async.http.ConnectionClosedException;
import com.koushikdutta.async.http.libcore.RawHeaders;
import com.koushikdutta.async.parser.AsyncParser;
import com.koushikdutta.async.parser.JSONArrayParser;
import com.koushikdutta.async.parser.JSONObjectParser;
import com.koushikdutta.async.parser.StringParser;
import com.koushikdutta.ion.InputStreamParser;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import com.koushikdutta.ion.Response;
import com.koushikdutta.ion.builder.Builders;
import com.koushikdutta.ion.builder.LoadBuilder;
import com.koushikdutta.ion.future.ResponseFuture;
import com.levelup.http.BaseHttpRequest;
import com.levelup.http.DataErrorException;
import com.levelup.http.HttpBodyJSON;
import com.levelup.http.HttpBodyMultiPart;
import com.levelup.http.HttpBodyParameters;
import com.levelup.http.HttpBodyString;
import com.levelup.http.HttpBodyUrlEncoded;
import com.levelup.http.HttpException;
import com.levelup.http.HttpExceptionCreator;
import com.levelup.http.HttpResponse;
import com.levelup.http.ParserException;
import com.levelup.http.ResponseHandler;
import com.levelup.http.TypedHttpRequest;
import com.levelup.http.UploadProgressListener;
import com.levelup.http.internal.BaseHttpEngine;
import com.levelup.http.ion.internal.IonBody;
import com.levelup.http.ion.internal.IonHttpBodyJSON;
import com.levelup.http.ion.internal.IonHttpBodyMultiPart;
import com.levelup.http.ion.internal.IonHttpBodyString;
import com.levelup.http.ion.internal.IonHttpBodyUrlEncoded;
import com.levelup.http.parser.ErrorHandlerParser;
import com.levelup.http.parser.Utils;
import com.levelup.http.parser.XferTransform;
import com.levelup.http.parser.XferTransformChain;
import com.levelup.http.parser.XferTransformInputStreamHttpStream;
import com.levelup.http.parser.XferTransformInputStreamString;
import com.levelup.http.parser.XferTransformResponseInputStream;
import com.levelup.http.parser.XferTransformStringJSONArray;
import com.levelup.http.parser.XferTransformStringJSONObject;

/**
 * Basic HTTP request to be passed to {@link com.levelup.http.HttpClient}
 *
 * @param <T> type of the data read from the HTTP response
 * @see com.levelup.http.HttpRequestGet for a more simple API
 * @see com.levelup.http.HttpRequestPost for a more simple POST API
 */
public class HttpEngineIon<T> extends BaseHttpEngine<T, HttpResponseIon<T>> {
	public final Builders.Any.B requestBuilder;
	private static Ion ion;
	private RawHeaders headers;
	private InputStream inputStream;

	public HttpEngineIon(BaseHttpRequest.AbstractBuilder<T, ?> builder) {
		super(wrapBuilderBodyParams(builder));

		if (builder.getContext() == null) {
			throw new NullPointerException("Ion HTTP request with no Context, try calling HttpClient.setup() first or a constructor with a Context");
		}

		synchronized (HttpEngineIon.class) {
			if (ion == null) {
				ion = Ion.getDefault(builder.getContext());
				// until https://github.com/koush/AndroidAsync/issues/210 is fixed
				ion.getConscryptMiddleware().enable(false);
			}
		}

		final LoadBuilder<Builders.Any.B> ionLoadBuilder = ion.build(builder.getContext());
		this.requestBuilder = ionLoadBuilder.load(getHttpMethod(), getUri().toString());
	}

	private static <T> BaseHttpRequest.AbstractBuilder<T, ?> wrapBuilderBodyParams(BaseHttpRequest.AbstractBuilder<T, ?> builder) {
		final HttpBodyParameters sourceBody = builder.getBodyParams();
		if (sourceBody instanceof HttpBodyMultiPart)
			builder.setBody(builder.getHttpMethod(), new IonHttpBodyMultiPart((HttpBodyMultiPart) sourceBody));
		else if (sourceBody instanceof HttpBodyJSON)
			builder.setBody(builder.getHttpMethod(), new IonHttpBodyJSON((HttpBodyJSON) sourceBody));
		else if (sourceBody instanceof HttpBodyUrlEncoded)
			builder.setBody(builder.getHttpMethod(), new IonHttpBodyUrlEncoded((HttpBodyUrlEncoded) sourceBody));
		else if (sourceBody instanceof HttpBodyString)
			builder.setBody(builder.getHttpMethod(), new IonHttpBodyString((HttpBodyString) sourceBody));
		else if (sourceBody != null)
			throw new IllegalStateException("Unknown body type "+sourceBody);

		return builder;
	}

	@Override
	public void settleHttpHeaders(TypedHttpRequest<T> request) throws HttpException {
		if (!isMethodWithBody(getHttpMethod())) {
			setHeader(HTTP.CONTENT_LEN, "0");
		}

		super.settleHttpHeaders(request);

		for (Entry<String, String> entry : mRequestSetHeaders.entrySet()) {
			requestBuilder.setHeader(entry.getKey(), entry.getValue());
		}
		for (Entry<String, HashSet<String>> entry : mRequestAddHeaders.entrySet()) {
			for (String value : entry.getValue()) {
				requestBuilder.addHeader(entry.getKey(), value);
			}
		}

		if (null!=followRedirect) {
			requestBuilder.followRedirect(followRedirect);
		}

		if (null != getHttpConfig()) {
			int readTimeout = getHttpConfig().getReadTimeout(request);
			if (readTimeout >= 0)
				requestBuilder.setTimeout(readTimeout);
		}
	}

	@Override
	public final void setupBody() {
		if (null == requestBuilder) throw new IllegalStateException("is this a streaming request?");
		if (null != bodyParams) {
			((IonBody) bodyParams).setOutputData(requestBuilder);

			final UploadProgressListener progressListener = getProgressListener();
			if (null != progressListener) {
				requestBuilder.progress(new ProgressCallback() {
					@Override
					public void onProgress(long downloaded, long total) {
						progressListener.onParamUploadProgress(HttpEngineIon.this, null, (int) ((100 * downloaded) / total));
					}
				});
			}
		}
	}

	@Override
	public final void doConnection() throws IOException {
		// do nothing
	}

	public RawHeaders getHeaders() {
		return headers;
	}

	public void setHeaders(RawHeaders headers) {
		this.headers = headers;
	}

	@Override
	protected HttpResponseIon<T> queryResponse(TypedHttpRequest<T> request, ResponseHandler<T> responseHandler) throws HttpException {
		XferTransform<HttpResponse, ?> errorParser = ((ErrorHandlerParser) responseHandler.errorHandler).errorDataParser;
		XferTransform<HttpResponse, ?> commonTransforms = Utils.getCommonXferTransform(responseHandler.contentParser, errorParser);
		AsyncParser<Object> parser = getXferTransformParser(commonTransforms);
		prepareRequest(request);
		ResponseFuture<Object> req = requestBuilder.as(parser);
		Future<Response<Object>> withResponse = req.withResponse();
		try {
			Response<Object> response = withResponse.get();
			HttpResponseIon ionResponse = new HttpResponseIon(response, commonTransforms);
			setRequestResponse(request, ionResponse);

			Exception e = response.getException();
			if (null != e) {
				throw exceptionToHttpException(request, e).build();
			}

			if (isHttpError(ionResponse)) {
				DataErrorException exceptionWithData = null;

				if (responseHandler.errorHandler instanceof ErrorHandlerParser) {
					Object data = response.getResult();
					ErrorHandlerParser errorHandler = (ErrorHandlerParser) responseHandler.errorHandler;
					XferTransform<Object, Object> transformToResult = Utils.skipCommonTransforms(errorHandler.errorDataParser, commonTransforms);
					Object errorData;
					if (null == transformToResult)
						errorData = data;
					else
						errorData = transformToResult.transformData(data, this);
					exceptionWithData = ((ErrorHandlerParser) responseHandler.errorHandler).handleErrorData(errorData, this);
				}

				HttpException.Builder exceptionBuilder = exceptionToHttpException(request, exceptionWithData);
				throw exceptionBuilder.build();
			}

			return ionResponse;

		} catch (InterruptedException e) {
			throw exceptionToHttpException(request, e).build();

		} catch (ExecutionException e) {
			throw exceptionToHttpException(request, e).build();

		} catch (ParserException e) {
			throw exceptionToHttpException(request, e).build();

		} catch (IOException e) {
			throw exceptionToHttpException(request, e).build();

		}
	}

	@Override
	protected T responseToResult(HttpResponseIon<T> response, ResponseHandler<T> responseHandler) throws ParserException, IOException {
		Object data = response.getResult();
		XferTransform<Object, Object> transformToResult = Utils.skipCommonTransforms(responseHandler.contentParser, response.getCommonTransform());
		if (null == transformToResult)
			return (T) data;

		return (T) transformToResult.transformData(data, this);
	}

	@Override
	protected HttpException.Builder exceptionToHttpException(HttpExceptionCreator request, Exception e) throws HttpException {
		if (e instanceof ConnectionClosedException && e.getCause() instanceof Exception) {
			return exceptionToHttpException(request, (Exception) e.getCause());
		}

		return super.exceptionToHttpException(request, e);
	}

	private static final AsyncParser<InputStream> INPUT_STREAM_ASYNC_PARSER = new InputStreamParser();
	private static final AsyncParser<String> STRING_ASYNC_PARSER = new StringParser();
	private static final AsyncParser<?> JSON_OBJECT_ASYNC_PARSER = new JSONObjectParser();
	private static final AsyncParser<?> JSON_ARRAY_ASYNC_PARSER = new JSONArrayParser();

	private <P> AsyncParser<P> getXferTransformParser(XferTransform<HttpResponse, ?> transform) {
		if (transform == XferTransformResponseInputStream.INSTANCE) {
			return (AsyncParser<P>) INPUT_STREAM_ASYNC_PARSER;
		}

		if (transform instanceof XferTransformChain) {
			final XferTransformChain chain = (XferTransformChain) transform;
			if (chain.transforms.length != 0) {
				if (chain.transforms[0] == XferTransformResponseInputStream.INSTANCE) {
					if (chain.transforms.length == 1) {
						return (AsyncParser<P>) INPUT_STREAM_ASYNC_PARSER;
					}

					if (chain.transforms[1] == XferTransformInputStreamString.INSTANCE) {
						if (chain.transforms.length == 2) {
							return (AsyncParser<P>) STRING_ASYNC_PARSER;
						}

						if (chain.transforms[2] == XferTransformStringJSONObject.INSTANCE) {
							if (chain.transforms.length == 3) {
								return (AsyncParser<P>) JSON_OBJECT_ASYNC_PARSER;
							}
						}

						if (chain.transforms[2] == XferTransformStringJSONArray.INSTANCE) {
							if (chain.transforms.length == 3) {
								return (AsyncParser<P>) JSON_ARRAY_ASYNC_PARSER;
							}
						}
					}

					return new AsyncParser<P>() {
						@Override
						public Future<P> parse(DataEmitter emitter) {
							Future<InputStream> inputStreamFuture = INPUT_STREAM_ASYNC_PARSER.parse(emitter);
							return inputStreamFuture.then(new TransformFuture<P, InputStream>() {
								@Override
								protected void transform(InputStream result) throws Exception {
									setComplete((P) chain.skipFirstTransform().transformData(result, HttpEngineIon.this));
								}
							});
						}

						@Override
						public void write(DataSink sink, P value, CompletedCallback completed) {
						}
					};
				}
			}
		}

		throw new IllegalStateException();
	}

	/**
	 * See if we can find common ground to parse the data and the error data inside Ion
	 * @param responseHandler
	 * @return whether Ion will be able to parse the data and the error in its processing thread
	 */
	public static boolean errorCompatibleWithData(ResponseHandler<?> responseHandler) {
		if (!(responseHandler.errorHandler instanceof ErrorHandlerParser)) {
			// not possible to handle the error data with the data coming out of the data parser
			return false;
		}

		ErrorHandlerParser errorHandlerParser = (ErrorHandlerParser) responseHandler.errorHandler;
		return Utils.getCommonXferTransform(responseHandler.contentParser, errorHandlerParser.errorDataParser) != null;
	}

	public static <T> boolean canHandleXferTransform(XferTransform<HttpResponse, T> contentParser) {
		if (contentParser instanceof XferTransformChain) {
			XferTransformChain<HttpResponse, T> parser = (XferTransformChain<HttpResponse, T>) contentParser;
			for (XferTransform transform : parser.transforms) {
				if (transform == XferTransformInputStreamHttpStream.INSTANCE)
					return false;
			}
		}
		return true;
	}
}
