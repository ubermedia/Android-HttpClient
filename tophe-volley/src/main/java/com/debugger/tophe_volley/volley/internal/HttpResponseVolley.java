package com.debugger.tophe_volley.volley.internal;

import android.support.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import co.tophe.HttpResponse;

/**
 * Created by Denis Babak on 10/06/16.
 */
public class HttpResponseVolley<T> implements HttpResponse {
    @Nullable
    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public int getResponseCode() throws IOException {
        return 0;
    }

    @Override
    public Map<String, List<String>> getHeaderFields() {
        return null;
    }

    @Nullable
    @Override
    public String getHeaderField(String name) {
        return null;
    }

    @Override
    public int getContentLength() {
        return 0;
    }

    @Override
    public String getResponseMessage() throws IOException {
        return null;
    }

    @Nullable
    @Override
    public String getContentEncoding() {
        return null;
    }

    @Override
    public void disconnect() {

    }

    @Override
    public InputStream getContentStream() throws IOException {
        return null;
    }
}
