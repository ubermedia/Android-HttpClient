package com.debugger.tophe_volley.volley.internal;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;

import java.io.IOException;
import java.io.InputStream;
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
    private final XferTransform<HttpResponse, ?> commonTransform;

    public HttpResponseVolley(T response, Request request, int code, XferTransform<HttpResponse, ?> commonTransform) {
        this.response = response;
        this.request = request;
        this.code = code;
        this.commonTransform = commonTransform;
    }

    public T getResponse() {
        return response;
    }

    @Nullable
    @Override
    public String getContentType() {
        return request.getBodyContentType();
    }

    @Override
    public int getResponseCode() throws IOException {
        return code;
    }

    @Override
    public Map<String, List<String>> getHeaderFields() {
        return request.getHeaders();
    }

    @Nullable
    @Override
    public String getHeaderField(String name) {
        try {
            return (String)request.getHeaders().get(name);
        } catch (AuthFailureError authFailureError) {
            authFailureError.printStackTrace();
            return null;
        }
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
        return null;
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
        return null;
    }

    public XferTransform<HttpResponse, ?> getCommonTransform() {
        return commonTransform;
    }
}
