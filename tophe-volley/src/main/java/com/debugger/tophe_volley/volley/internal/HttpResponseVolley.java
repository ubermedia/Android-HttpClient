package com.debugger.tophe_volley.volley.internal;

import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.debugger.tophe_volley.volley.request.JSONRequestWithHeaders;
import com.debugger.tophe_volley.volley.request.StringRequestWithHeaders;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.tophe.HttpResponse;
import co.tophe.parser.XferTransform;

/**
 * Created by Denis Babak on 10/06/16.
 */
public class HttpResponseVolley<T> implements HttpResponse {

    T response;
    Request request;
    int code;
    String message;
    ByteBufferListInputStream is;
    Map<String, String> responseHeaders;
    private final XferTransform<HttpResponse, ?> commonTransform;

    public HttpResponseVolley(T response, Request request, int code, String message, XferTransform<HttpResponse, ?> commonTransform) {
        this.response = response;
        this.request = request;
        this.code = code;
        this.message = message;
        this.commonTransform = commonTransform;
        if(request instanceof StringRequestWithHeaders) {
            this.is = ((StringRequestWithHeaders) request).is;
            this.responseHeaders = ((StringRequestWithHeaders) request).getResponseHeaders();
        } else if(request instanceof JSONRequestWithHeaders) {
            this.is = ((JSONRequestWithHeaders) request).bbIs;
            this.responseHeaders = ((JSONRequestWithHeaders) request).getResponseHeaders();
        }
    }

    public T getResponse() {
        return response;
    }

    private Map<String, List<String>> constructHeaders() throws AuthFailureError {
        Map<String, List<String>> result = new HashMap<>();
        for(Object key : request.getHeaders().keySet()) {
            List<String> params = new ArrayList<>(1);
            params.add((String)request.getHeaders().get((String)key));
            result.put((String)key, params);
        }
        for(Object key : responseHeaders.keySet()) {
            List<String> params = new ArrayList<>(1);
            params.add(responseHeaders.get(key));
            result.put((String)key, params);
        }
        return result;
    }

    @Nullable
    @Override
    public String getContentType() {
        if(responseHeaders.containsKey("content-type")) {
            return responseHeaders.get("content-type");
        }
        if(responseHeaders.containsKey("Content-Type")) {
            return responseHeaders.get("Content-Type");
        }
        return request.getBodyContentType();
    }

    @Override
    public int getResponseCode() throws IOException {
        return code;
    }

    @Override
    public Map<String, List<String>> getHeaderFields() {
        try {
            return constructHeaders();
        } catch (AuthFailureError e) {
            return null;
        }
    }

    @Nullable
    @Override
    public String getHeaderField(String name) {
        return responseHeaders.get(name);
    }

    @Override
    public int getContentLength() {
        String contentLength = getHeaderField("Content-Length");
        if (TextUtils.isEmpty(contentLength))
            return -1;
        return Integer.parseInt(contentLength);
    }

    @Override
    public String getResponseMessage() throws IOException {
        return message;
    }

    @Nullable
    @Override
    public String getContentEncoding() {
        return getHeaderField("Content-Encoding");
    }

    @Override
    public void disconnect() {
    }

    @Override
    public InputStream getContentStream() throws IOException {
        if (response instanceof InputStream)
            return (InputStream) response;

        /*if(response instanceof String) {
            InputStream stream = new ByteArrayInputStream(((String)response).getBytes("UTF-8"));
            return stream;
        }*/
        if(is != null) {
            return is;
        }

        /*if (response.getException() instanceof ServerException) {
            ServerException exception = (ServerException) response.getException();
            if (exception.getServerError() instanceof InputStream)
                return (InputStream) exception.getServerError();
        }*/

        throw new IOException("trying to read an InputStream from Volley result:"+response+" error:");
    }

    public XferTransform<HttpResponse, ?> getCommonTransform() {
        return commonTransform;
    }
}
