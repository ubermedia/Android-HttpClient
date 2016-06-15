package com.debugger.tophe_volley.volley.request;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Denis Babak on 15/06/16.
 */
public class StringRequestWithHeaders extends StringRequest {
    public StringRequestWithHeaders(int method, String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }

    public StringRequestWithHeaders(String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(url, listener, errorListener);
    }

    private Map<String, String> customHeaders = new HashMap<>();

    public void addHeaders(Map<String, String> collection) {
        this.customHeaders.putAll(collection);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        if(customHeaders != null && customHeaders.size() > 0) {
            return customHeaders;
        }
        return super.getHeaders();
    }
}
