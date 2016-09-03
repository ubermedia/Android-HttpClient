package com.debugger.tophe_volley.volley;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.debugger.tophe_volley.volley.internal.HttpResponseVolley;
import com.debugger.tophe_volley.volley.internal.VolleyBody;
import com.debugger.tophe_volley.volley.internal.VolleyHttpBodyJSON;
import com.debugger.tophe_volley.volley.internal.VolleyHttpBodyMultiPart;
import com.debugger.tophe_volley.volley.internal.VolleyHttpBodyString;
import com.debugger.tophe_volley.volley.internal.VolleyHttpBodyUrlEncoded;
import com.debugger.tophe_volley.volley.request.HttpBodyMultipartRequest;
import com.debugger.tophe_volley.volley.request.HttpBodyUrlEncodedRequest;
import com.debugger.tophe_volley.volley.request.JSONRequestWithHeaders;
import com.debugger.tophe_volley.volley.request.StringRequestWithHeaders;
import com.debugger.tophe_volley.volley.request.VolleyRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import co.tophe.AbstractHttpEngine;
import co.tophe.BasicHttpConfig;
import co.tophe.HttpException;
import co.tophe.HttpResponse;
import co.tophe.HttpSignException;
import co.tophe.ServerException;
import co.tophe.body.HttpBodyJSON;
import co.tophe.body.HttpBodyMultiPart;
import co.tophe.body.HttpBodyParameters;
import co.tophe.body.HttpBodyString;
import co.tophe.body.HttpBodyUrlEncoded;
import co.tophe.parser.ParserException;
import co.tophe.parser.Utils;
import co.tophe.parser.XferTransform;
import cz.msebera.android.httpclient.protocol.HTTP;

/**
 * Created by Denis Babak on 10/06/16.
 */
public class HttpEngineVolley<T, SE extends ServerException> extends AbstractHttpEngine<T, SE, HttpResponseVolley<T>> {

    Request volleyRequest;
    final VolleyBody volleyBody;
    RequestQueue queue;
    Builder builder;
    RequestFuture<Object> requestFuture;

    private void createRequest(RequestFuture future) {
        int method = Request.Method.GET;
        if(request.getHttpMethod().equalsIgnoreCase("GET")) {
            method = Request.Method.GET;
        } else if(request.getHttpMethod().equalsIgnoreCase("POST")) {
            method = Request.Method.POST;
        } else if(request.getHttpMethod().equalsIgnoreCase("DELETE")) {
            method = Request.Method.DELETE;
        } else if(request.getHttpMethod().equalsIgnoreCase("PUT")) {
            method = Request.Method.PUT;
        }
        if(volleyBody instanceof VolleyHttpBodyString) {
            volleyRequest = new StringRequest(method, request.getUri().toString(), future, future);
        } else if(volleyBody instanceof VolleyHttpBodyJSON) {
            try {
                volleyRequest = new JSONRequestWithHeaders(method, request.getUri().toString(),
                        new JSONObject(((VolleyHttpBodyJSON)volleyBody).getJsonElement().toString()), future, future);
            } catch (JSONException e) {
                e.printStackTrace();
                volleyRequest = new JSONRequestWithHeaders(method, request.getUri().toString(),
                        null, future, future);
            }
        } else if(volleyBody instanceof VolleyHttpBodyUrlEncoded) {
            volleyRequest = new HttpBodyUrlEncodedRequest(method,
                    request.getUri().toString(), ((VolleyHttpBodyUrlEncoded) volleyBody), future, future);
        } else if(volleyBody instanceof VolleyHttpBodyMultiPart) {
            volleyRequest = new HttpBodyMultipartRequest(method,
                    request.getUri().toString(), ((VolleyHttpBodyMultiPart) volleyBody), future, future);
            /*((VolleyHttpBodyMultiPart) volleyBody).setContentLength(((HttpBodyMultipartRequest)volleyRequest).getContentLength());
            ((HttpBodyMultiPart)request.getBodyParameters()).apacheContentLength = ((HttpBodyMultipartRequest)volleyRequest).getContentLength();*/
            requestHeaders.remove(HTTP.CONTENT_LEN);
            requestHeaders.put(HTTP.CONTENT_LEN, String.valueOf(((HttpBodyMultipartRequest)volleyRequest).getContentLength()));
        } else {
            volleyRequest = new StringRequestWithHeaders(method,
                    request.getUri().toString(), volleyBody, future, future);
            //volleyRequest = new JSONRequestWithHeaders(request.getHttpMethod().equalsIgnoreCase("GET") ? Request.Method.GET : Request.Method.POST, request.getUri().toString(), null, future, future);

        }

        ((VolleyRequest) volleyRequest).addHeaders(requestHeaders);
        volleyRequest.setRetryPolicy(new DefaultRetryPolicy(BasicHttpConfig.READ_TIMEOUT_IN_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


        /*HttpConfig httpConfig = request.getHttpConfig();
        if (null != httpConfig) {
            int readTimeout = httpConfig.getReadTimeout(request);
            if (readTimeout >= 0)
                volleyRequest.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        }*/
    }

    @Override
    public void prepareEngine() throws HttpSignException {
        super.prepareEngine();
        requestFuture = RequestFuture.newFuture();
        createRequest(requestFuture);
    }

    public HttpEngineVolley(Builder<T, SE> builder, RequestQueue queue) {
        super(builder);

        this.queue = queue;
        this.builder = builder;

        final HttpBodyParameters sourceBody = request.getBodyParameters();
        if (sourceBody instanceof HttpBodyMultiPart)
            volleyBody = new VolleyHttpBodyMultiPart((HttpBodyMultiPart) sourceBody);
        else if (sourceBody instanceof HttpBodyJSON)
            volleyBody = new VolleyHttpBodyJSON((HttpBodyJSON) sourceBody);
        else if (sourceBody instanceof HttpBodyUrlEncoded) {
            volleyBody = new VolleyHttpBodyUrlEncoded((HttpBodyUrlEncoded) sourceBody);
        }
        else if (sourceBody instanceof HttpBodyString)
            volleyBody = new VolleyHttpBodyString((HttpBodyString) sourceBody, request.getUri().toString());
        else if (sourceBody != null)
            throw new IllegalStateException("Unknown body type "+sourceBody);
        else
            volleyBody = null;

        /*if (null != volleyBody) {
            volleyBody.setOutputData(requestBuilder);

            final UploadProgressListener progressListener = request.getProgressListener();
            if (null != progressListener) {
                requestBuilder.progress(new ProgressCallback() {
                    @Override
                    public void onProgress(long downloaded, long total) {
                        progressListener.onParamUploadProgress(request, null, (int) ((100 * downloaded) / total));
                    }
                });
            }
        }*/
    }

    @Override
    protected void setHeadersAndConfig() {
        /*if(volleyRequest instanceof HttpBodyMultipartRequest && volleyRequest.getMethod() == Request.Method.POST) {
            setHeader(HTTP.CONTENT_LEN, String.valueOf(((HttpBodyMultipartRequest)volleyRequest).getContentLength()));
            ((VolleyHttpBodyMultiPart) volleyBody).setContentLength(((HttpBodyMultipartRequest)volleyRequest).getContentLength());
            ((HttpBodyMultiPart)request.getBodyParameters()).apacheContentLength = ((HttpBodyMultipartRequest)volleyRequest).getContentLength();
        }*/
        /*if (request.getBodyParameters() instanceof HttpBodyMultiPart) {
            setHeader("content-type", null);
        }*/

        /*for (Map.Entry<String, String> entry : requestHeaders.entrySet()) {
            requestBuilder.setHeader(entry.getKey(), entry.getValue());
        }*/

        /*if (null != responseHandler.followsRedirect()) {
            requestBuilder.followRedirect(responseHandler.followsRedirect());
        }*/


    }

    @Override
    protected String getEngineSignature() {
        return "TopheVolley";
    }

    @Override
    protected HttpResponseVolley queryResponse() throws ServerException, HttpException {
        XferTransform<HttpResponse, SE> errorParser = responseHandler.errorParser;
        XferTransform<HttpResponse, ?> commonTransforms = Utils.getCommonXferTransform(responseHandler.contentParser, errorParser, true);
        //AsyncParser<Object> parser = getXferTransformParser(commonTransforms);
        //ResponseFuture<Object> req = requestBuilder.as(parser);

        queue.add(volleyRequest);
        //Future<Response<Object>> withResponse = req.withResponse();
        try {
            Object response = requestFuture.get();
            HttpResponseVolley volleyResponse = new HttpResponseVolley(response, volleyRequest, ((VolleyRequest) volleyRequest).getResponseCode(), "", commonTransforms);
            setRequestResponse(volleyResponse);

            /*Exception e = response.getException();
            if (null != e) {
                throw exceptionToHttpException(e).build();
            }*/

            if (isHttpError(volleyResponse)) {
                XferTransform<Object, Object> transformToResult = Utils.skipCommonTransforms(errorParser, commonTransforms);
                SE errorData;
                if (null == transformToResult)
                    errorData = (SE) response;
                else
                    errorData = (SE) transformToResult.transformData(response, this);
                throw errorData;
            }
            return volleyResponse;

        } catch (InterruptedException e) {
            e.printStackTrace();
            throw exceptionToHttpException(e).build();

        } catch (ExecutionException e) {
            e.printStackTrace();
            throw exceptionToHttpException(e).build();

        } catch (ParserException e) {
            e.printStackTrace();
            throw exceptionToHttpException(e).build();

        } catch (IOException e) {
            e.printStackTrace();
            throw exceptionToHttpException(e).build();

        }
    }

    @Override
    protected T responseToResult(HttpResponseVolley<T> response) throws ParserException, IOException {
        try {
            Object data = response.getResponse();
            XferTransform<Object, Object> transformToResult = Utils.skipCommonTransforms(responseHandler.contentParser, response.getCommonTransform());
            if (null == transformToResult) {
                return (T) response.getContentStream();
            }

            return (T) transformToResult.transformData(response.getContentStream(), this);

        } catch(ParserException e) {
            e.printStackTrace();
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }

        //return response.getResponse();
    }

}
