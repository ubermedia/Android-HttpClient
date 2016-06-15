package com.debugger.tophe_volley.volley;

import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.debugger.tophe_volley.volley.internal.HttpResponseVolley;
import com.debugger.tophe_volley.volley.internal.MultipartRequest;
import com.debugger.tophe_volley.volley.internal.VolleyBody;
import com.debugger.tophe_volley.volley.internal.VolleyHttpBodyJSON;
import com.debugger.tophe_volley.volley.internal.VolleyHttpBodyMultiPart;
import com.debugger.tophe_volley.volley.internal.VolleyHttpBodyString;
import com.debugger.tophe_volley.volley.internal.VolleyHttpBodyUrlEncoded;
import com.debugger.tophe_volley.volley.request.StringRequestWithHeaders;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import co.tophe.AbstractHttpEngine;
import co.tophe.HttpConfig;
import co.tophe.HttpException;
import co.tophe.HttpResponse;
import co.tophe.ServerException;
import co.tophe.body.HttpBodyJSON;
import co.tophe.body.HttpBodyMultiPart;
import co.tophe.body.HttpBodyParameters;
import co.tophe.body.HttpBodyString;
import co.tophe.body.HttpBodyUrlEncoded;
import co.tophe.log.LogManager;
import co.tophe.parser.ParserException;
import co.tophe.parser.Utils;
import co.tophe.parser.XferTransform;

/**
 * Created by Denis Babak on 10/06/16.
 */
public class HttpEngineVolley<T, SE extends ServerException> extends AbstractHttpEngine<T, SE, HttpResponseVolley<T>> {

    Request volleyRequest;
    final VolleyBody volleyBody;
    RequestQueue queue;
    Builder builder;

    private Request createRequest() {
        Log.e("Volley", "Volley createRequest() " + request.getHttpMethod() + " " + request.getUri());
        if(volleyBody instanceof VolleyHttpBodyString) {
            volleyRequest = new StringRequest(request.getHttpMethod().equalsIgnoreCase("GET") ? Request.Method.GET : Request.Method.POST, request.getUri().toString(), null, null);
        } else if(volleyBody instanceof VolleyHttpBodyJSON) {
            volleyRequest = new JsonObjectRequest(Request.Method.GET, request.getUri().toString(), null, null, null);
        } else {
            Log.e("Volley", "Volley createRequest() 1");
            volleyRequest = new StringRequestWithHeaders(request.getHttpMethod().equalsIgnoreCase("GET") ? Request.Method.GET : Request.Method.POST, request.getUri().toString(), new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                }
            }, null);
            Log.e("Volley", "Volley createRequest() 2");
            ((StringRequestWithHeaders) volleyRequest).addHeaders(requestHeaders);
            Log.e("Volley", "Volley createRequest() 3");
        }

        return volleyRequest;
    }

    public HttpEngineVolley(Builder<T, SE> builder, RequestQueue queue) {
        super(builder);
        Log.e("Volley", "VolleyEngine created! " + builder.getHttpRequest().getUri());
        //LogManager.getLogger().d("VolleyEngine created! cyka");

        this.queue = queue;
        this.builder = builder;

        final HttpBodyParameters sourceBody = request.getBodyParameters();
        Log.e("Volley", "VolleyEngine sourceBody null ? " + (sourceBody == null ? "NULL" : sourceBody.getClass().getName()));
        if (sourceBody instanceof HttpBodyMultiPart)
            volleyBody = new VolleyHttpBodyMultiPart((HttpBodyMultiPart) sourceBody);
        else if (sourceBody instanceof HttpBodyJSON)
            volleyBody = new VolleyHttpBodyJSON((HttpBodyJSON) sourceBody);
        else if (sourceBody instanceof HttpBodyUrlEncoded)
            volleyBody = new VolleyHttpBodyUrlEncoded((HttpBodyUrlEncoded) sourceBody);
        else if (sourceBody instanceof HttpBodyString)
            volleyBody = new VolleyHttpBodyString((HttpBodyString) sourceBody, request.getUri().toString());
        else if (sourceBody != null)
            throw new IllegalStateException("Unknown body type "+sourceBody);
        else
            volleyBody = null;

        Log.e("Volley", "VolleyEngine DONE");
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
        Log.e("Volley", "Volley setHeadersAndConfig()");
        /*if (request.getBodyParameters() instanceof HttpBodyMultiPart) {
            setHeader("content-type", null);
        }*/

        /*for (Map.Entry<String, String> entry : requestHeaders.entrySet()) {
            requestBuilder.setHeader(entry.getKey(), entry.getValue());
        }*/

        /*if (null != responseHandler.followsRedirect()) {
            requestBuilder.followRedirect(responseHandler.followsRedirect());
        }*/

        /*HttpConfig httpConfig = request.getHttpConfig();
        if (null != httpConfig) {
            int readTimeout = httpConfig.getReadTimeout(request);
            if (readTimeout >= 0)
                volleyRequest.setRetryPolicy(new DefaultRetryPolicy(readTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        }*/
        Log.e("Volley", "setheadersAndConfig DONE");
    }

    @Override
    protected String getEngineSignature() {
        return "TopheVolley";
    }

    @Override
    protected HttpResponseVolley queryResponse() throws ServerException, HttpException {
        Log.e("Volley", "Volley queryResponse()");
        XferTransform<HttpResponse, SE> errorParser = responseHandler.errorParser;
        XferTransform<HttpResponse, ?> commonTransforms = Utils.getCommonXferTransform(responseHandler.contentParser, errorParser, true);
        Log.e("Volley", "Volley queryResponse() 1");
        //AsyncParser<Object> parser = getXferTransformParser(commonTransforms);
        //ResponseFuture<Object> req = requestBuilder.as(parser);
        RequestFuture<Object> requestFuture = RequestFuture.newFuture();
        Request request = createRequest();
        Log.e("Volley", "Volley queryResponse() 2");
        queue.add(request);
        //Future<Response<Object>> withResponse = req.withResponse();
        try {
            Object response = requestFuture.get();
            Log.e("Volley", "Volley queryResponse() 3");
            HttpResponseVolley volleyResponse = new HttpResponseVolley(response, commonTransforms);
            setRequestResponse(volleyResponse);
            Log.e("Volley", "Volley queryResponse() 4");

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
        Log.e("Volley", "Volley responseToResult()");
        Object data = response.getResponse();
        XferTransform<Object, Object> transformToResult = Utils.skipCommonTransforms(responseHandler.contentParser, response.getCommonTransform());
        if (null == transformToResult)
            return (T) data;

        return (T) transformToResult.transformData(data, this);
    }
}
