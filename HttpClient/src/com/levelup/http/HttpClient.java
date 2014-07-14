package com.levelup.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import com.google.gson.JsonParseException;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.http.libcore.RawHeaders;
import com.koushikdutta.ion.Response;
import com.koushikdutta.ion.future.ResponseFuture;
import com.levelup.http.gson.InputStreamGsonParser;
import com.levelup.http.internal.HttpRequestIon;
import com.levelup.http.internal.HttpRequestUrlConnection;
import com.levelup.http.internal.HttpResponseIon;
import com.levelup.http.internal.HttpResponseUrlConnection;

/**
 * HTTP client that handles {@link HttpRequest} 
 */
public class HttpClient {
	public static final String ACCEPT_ENCODING = "Accept-Encoding";

	private static String userAgent;
	private static CookieManager cookieManager;
	private static Header[] defaultHeaders;
	public static Context defaultContext;

	/**
	 * Setup internal values of the {@link HttpClient} using the provided {@link Context}
	 * <p>The user agent is deduced from the app name of the {@code context} if it's not {@code null}</p>
	 * @param context Used to get a proper User Agent for your app, may be {@code null}
	 */
	public static void setup(Context context) {
		userAgent = "LevelUp-HttpClient/00000";
		if (null!=context) {
			defaultContext = context;
			PackageManager pM = context.getPackageManager();
			try {
				PackageInfo pI = pM.getPackageInfo(context.getPackageName(), 0);
				if (pI != null)
					userAgent = pI.applicationInfo.nonLocalizedLabel + "/" + pI.versionCode;
			} catch (NameNotFoundException ignored) {
			}
		}
	}

	public static void setCookieManager(CookieManager cookieManager) {
		HttpClient.cookieManager = cookieManager;
	}

	public static CookieManager getCookieManager() {
		return cookieManager;
	}

	public static void setDefaultHeaders(Header[] headers) {
		defaultHeaders = headers;
	}

	public static Header[] getDefaultHeaders() {
		return defaultHeaders;
	}

	/**
	 * Process the HTTP request on the network and return the HttpURLConnection
	 * @param request
	 * @return an {@link HttpURLConnection} with the network response
	 * @throws HttpException
	 */
	private static void getQueryResponse(HttpRequestUrlConnection request, boolean allowGzip) throws HttpException {
		try {
			if (allowGzip && request.getHeader(ACCEPT_ENCODING)==null) {
				request.setHeader(ACCEPT_ENCODING, "gzip,deflate");
			}

			request.prepareRequest(userAgent);

			final LoggerTagged logger = request.getLogger();
			if (null != logger) {
				logger.v(request.getHttpMethod() + ' ' + request.getUri());
				/* TODO for (Map.Entry<String, List<String>> header : request.urlConnection.getRequestProperties().entrySet()) {
					logger.v(header.getKey()+": "+header.getValue());
				}*/
			}

			request.doConnection();

			if (null != logger) {
				/* TODO logger.v(request.urlConnection.getResponseMessage());
				for (Map.Entry<String, List<String>> header : request.urlConnection.getHeaderFields().entrySet()) {
					logger.v(header.getKey()+": "+header.getValue());
				}*/
			}

		} catch (SecurityException e) {
			forwardResponseException(request, e);

		} catch (IOException e) {
			forwardResponseException(request, e);

		} finally {
			try {
				request.setResponse(new HttpResponseUrlConnection(request));
			} catch (IllegalStateException e) {
				// okhttp 2.0.0 issue https://github.com/square/okhttp/issues/689
				LogManager.getLogger().d("connection closed ? for "+request+' '+e);
				HttpException.Builder builder = request.newException();
				builder.setErrorMessage("Connection closed "+e.getMessage());
				builder.setCause(e);
				builder.setErrorCode(HttpException.ERROR_NETWORK);
				throw builder.build();
			}
		}
	}

	/**
	 * Perform the query on the network and get the resulting body as an InputStream
	 * <p>Does various checks on the result and throw {@link HttpException} in case of problem</p>
	 * @param request The HTTP request to process
	 * @return The InputStream corresponding to the data stream, may be null
	 * @throws HttpException
	 */
	public static InputStream getInputStream(HttpRequest request) throws HttpException {
		if (request instanceof BaseHttpRequest) {
			BaseHttpRequest baseHttpRequest = (BaseHttpRequest) request;
			HttpRequestImpl httpRequestImpl = baseHttpRequest.getHttpRequestImpl();

			if (httpRequestImpl instanceof HttpRequestIon) {
				HttpRequestIon httpRequest = (HttpRequestIon) httpRequestImpl;
				httpRequest.prepareRequest(userAgent);
				try {
					ResponseFuture<InputStream> req = httpRequest.requestBuilder.asInputStream();
					Future<Response<InputStream>> withResponse = req.withResponse();
					Response<InputStream> response = withResponse.get();
					request.setResponse(new HttpResponseIon(response));
					throwResponseException(request, response);
					return response.getResult();
				} catch (InterruptedException e) {
					forwardResponseException(request, e);

				} catch (ExecutionException e) {
					forwardResponseException(request, e);

				}
			}

			if (httpRequestImpl instanceof HttpRequestUrlConnection) {
				HttpRequestUrlConnection httpRequest = (HttpRequestUrlConnection) httpRequestImpl;
				httpRequest.prepareRequest(userAgent);
				getQueryResponse(httpRequest, true);
				try {
					return ((HttpResponseUrlConnection) httpRequest.getResponse()).getInputStream();
				} catch (IOException e) {
					forwardResponseException(request, e);
				}
			}
		}

		return null;
		/*
		HttpURLConnection resp = getQueryResponse(request, true);

		InputStream is = null;
		if (resp!=null) {
			try {
				final int contentLength = resp.getContentLength();
				if (contentLength != 0) {
					is = resp.getInputStream();
					if ("deflate".equals(resp.getContentEncoding()) && !(is instanceof InflaterInputStream))
						is = new InflaterInputStream(is);
					if ("gzip".equals(resp.getContentEncoding()) && !(is instanceof GZIPInputStream))
						is = new GZIPInputStream(is);
				}

				if (resp.getResponseMessage()==null && null!=is) {
					String body = InputStreamStringParser.instance.parseInputStream(is, request);

					HttpException.Builder builder = request.newException();
					builder.setErrorMessage(TextUtils.isEmpty(body) ? "empty response" : body);
					builder.setErrorCode(HttpException.ERROR_HTTP);
					throw builder.build();
				}

				if (resp.getResponseCode() < 200 || resp.getResponseCode() >= 300) {
					HttpException.Builder builder = request.newExceptionFromResponse(null);
					builder.setErrorCode(HttpException.ERROR_HTTP);
					throw builder.build();
				}

				final String expectedMimeType = resp.getRequestProperty("Accept");
				if (!TextUtils.isEmpty(expectedMimeType)) {
					// test if it's the right MIME type or throw an exception that can be caught to use the bad data
					MediaType expectedType = MediaType.parse(expectedMimeType);
					if (null!=expectedType && !expectedType.equalsType(MediaType.parse(resp.getContentType()))) {
						String body = InputStreamStringParser.instance.parseInputStream(is, request);

						HttpException.Builder builder = request.newException();
						builder.setErrorMessage("Expected '"+expectedMimeType+"' got '"+resp.getContentType()+"' - "+body);
						builder.setErrorCode(HttpException.ERROR_HTTP_MIME);
						throw builder.build();
					}
				}

			} catch (FileNotFoundException e) {
				HttpException.Builder builder = request.newExceptionFromResponse(e);
				HttpException exception = builder.build();
				if (null==exception.getCause())
					LogManager.getLogger().d("http error "+exception.getMessage());
				else
					LogManager.getLogger().d("http error for "+request, e);
				throw exception;

			} catch (SocketTimeoutException e) {
				LogManager.getLogger().d("timeout for "+request);
				HttpException.Builder builder = request.newException();
				builder.setErrorMessage("timeout");
				builder.setCause(e);
				builder.setErrorCode(HttpException.ERROR_TIMEOUT);
				throw builder.build();

			} catch (IOException e) {
				LogManager.getLogger().d("i/o error for "+request+' '+e.getMessage());
				HttpException.Builder builder = request.newException();
				builder.setErrorMessage("IO error "+e.getMessage());
				builder.setCause(e);
				builder.setErrorCode(HttpException.ERROR_NETWORK);
				throw builder.build();
			}
		}

		return is;*/
	}

	/**
	 * Perform the query on the network and get the resulting body as an InputStream
	 * <p>Does various checks on the result and throw {@link HttpException} in case of problem</p>
	 * @param request The HTTP request to process
	 * @return The parsed object or null
	 * @throws HttpException
	 */
	public static <T> T parseRequest(TypedHttpRequest<T> request) throws HttpException {
		InputStreamParser<T> streamParser = request.getInputStreamParser();
		if (null==streamParser) throw new NullPointerException("typed request without a stream parser:"+request);
		return parseRequest(request, streamParser);
	}

	public static void forwardResponseException(HttpRequest request, Exception e) throws HttpException {
		if (e instanceof InterruptedException) {
			HttpException.Builder builder = request.newException();
			builder.setErrorMessage("interrupted");
			builder.setCause(e);
			builder.setErrorCode(HttpException.ERROR_HTTP);
			throw builder.build();
		}

		if (e instanceof ExecutionException) {
			if (e.getCause() instanceof Exception)
				forwardResponseException(request, (Exception) e.getCause());
			else {
				HttpException.Builder builder = request.newException();
				builder.setErrorMessage("execution error");
				builder.setCause(e.getCause());
				builder.setErrorCode(HttpException.ERROR_HTTP);
				throw builder.build();
			}
		}

		if (e instanceof SocketTimeoutException || e instanceof TimeoutException) {
			LogManager.getLogger().d("timeout for "+request);
			HttpException.Builder builder = request.newException();
			builder.setErrorMessage("Timeout error "+e.getMessage());
			builder.setCause(e);
			builder.setErrorCode(HttpException.ERROR_TIMEOUT);
			throw builder.build();
		}

		if (e instanceof ProtocolException) {
			LogManager.getLogger().d("bad method for " + request + ' ' + e.getMessage());
			HttpException.Builder builder = request.newException();
			builder.setErrorMessage("Method error " + e.getMessage());
			builder.setCause(e);
			builder.setErrorCode(HttpException.ERROR_HTTP);
			throw builder.build();
		}

		if (e instanceof IOException) {
			LogManager.getLogger().d("i/o error for " + request + ' ' + e.getMessage());
			HttpException.Builder builder = request.newException();
			builder.setErrorMessage("IO error " + e.getMessage());
			builder.setCause(e);
			builder.setErrorCode(HttpException.ERROR_NETWORK);
			throw builder.build();
		}

		if (e instanceof ParserException) {
			LogManager.getLogger().i("incorrect data for " + request);
			if (e.getCause() instanceof HttpException)
				throw (HttpException) e.getCause();

			HttpException.Builder builder = request.newException();
			builder.setCause(e);
			builder.setErrorCode(HttpException.ERROR_PARSER);
			throw builder.build();
		}

		if (e instanceof JsonParseException) {
			LogManager.getLogger().i("incorrect data for " + request);
			HttpException.Builder builder = request.newException();
			builder.setCause(e);
			builder.setErrorCode(HttpException.ERROR_PARSER);
			throw builder.build();
		}

		if (e instanceof SecurityException) {
			LogManager.getLogger().w("security error for "+request+' '+e);
			HttpException.Builder builder = request.newException();
			builder.setErrorMessage("Security error "+e.getMessage());
			builder.setCause(e);
			builder.setErrorCode(HttpException.ERROR_NETWORK);
			throw builder.build();
		}
	}

	private static void throwResponseException(HttpRequest request, Response<?> response) throws HttpException {
		RawHeaders headers = response.getHeaders();
		if (null!=headers) {
			if (headers.getResponseCode() < 200 || headers.getResponseCode() >= 300) {
				HttpException.Builder builder = request.newExceptionFromResponse(null);
				throw builder.build();
			}
		}

		Exception e = response.getException();
		if (null!=e) {
			forwardResponseException(request, e);
		}
	}

	/**
	 * Perform the query on the network and get the resulting body as an InputStream
	 * <p>Does various checks on the result and throw {@link HttpException} in case of problem</p>
	 * @param request The HTTP request to process
	 * @param parser The {@link InputStreamParser parser} used to transform the input stream into the desired type. May be {@code null}
	 * @return The parsed object or null
	 * @throws HttpException
	 */
	public static <T> T parseRequest(final HttpRequest request, InputStreamParser<T> parser) throws HttpException {
		if (request instanceof BaseHttpRequest) {
			BaseHttpRequest baseHttpRequest = (BaseHttpRequest) request;
			HttpRequestImpl httpRequestImpl = baseHttpRequest.getHttpRequestImpl();

			if (httpRequestImpl instanceof HttpRequestUrlConnection && baseHttpRequest.isStreaming()) {
				// special case: streaming with HttpRequestUrlConnection
				HttpRequestUrlConnection httpRequest = (HttpRequestUrlConnection) httpRequestImpl;
				getQueryResponse(httpRequest, true);
				try {
					return (T) new HttpStream(((HttpResponseUrlConnection) httpRequest.getResponse()).getInputStream(), request);
				} catch (IOException e) {
					forwardResponseException(request, e);
				}
			}

			if (httpRequestImpl instanceof HttpRequestIon) {
				// special case: Gson data handling with HttpRequestIon
				HttpRequestIon<T> httpRequest = (HttpRequestIon<T>) httpRequestImpl;

				if (parser == null)
					parser = httpRequest.getInputStreamParser();

				try {
					if (parser instanceof InputStreamGsonParser) {
						InputStreamGsonParser gsonParser = (InputStreamGsonParser) parser;
						final ResponseFuture<T> req;
						if (gsonParser.typeToken != null) {
							httpRequest.prepareRequest(userAgent);
							req = httpRequest.requestBuilder.as(gsonParser.typeToken);
						} else if (gsonParser.type instanceof Class) {
							Class<T> clazz = (Class<T>) gsonParser.type;
							httpRequest.prepareRequest(userAgent);
							req = httpRequest.requestBuilder.as(clazz);
						} else {
							req = null;
						}
						if (null != req) {
							Future<Response<T>> withResponse = req.withResponse();
							Response<T> response = withResponse.get();
							request.setResponse(new HttpResponseIon(response));
							throwResponseException(request, response);
							return response.getResult();
						}
					}

				} catch (InterruptedException e) {
					forwardResponseException(request, e);

				} catch (ExecutionException e) {
					forwardResponseException(request, e);

				} catch (ParserException e) {
					forwardResponseException(request, e);
				}
			}
		}

		InputStream is = getInputStream(request);
		if (null != is)
			try {
				if (null != parser)
					return parser.parseInputStream(is, request);

			} catch (IOException e) {
				forwardResponseException(request, e);

			} catch (ParserException e) {
				forwardResponseException(request, e);

			} finally {
				try {
					is.close();
				} catch (NullPointerException ignored) {
					// okhttp 2.0 bug https://github.com/square/okhttp/issues/690
				} catch (IOException ignored) {
				}
			}

		return null;
	}

	/**
	 * Perform the query on the network and get the resulting body as a String
	 * <p>Does various checks on the result and throw {@link HttpException} in case of problem</p>
	 * @param request The HTTP request to process
	 * @return The resulting body as a String
	 * @throws HttpException
	 */
	public static String getStringResponse(HttpRequest request) throws HttpException {
		return parseRequest(request, InputStreamStringParser.instance);
	}
}
