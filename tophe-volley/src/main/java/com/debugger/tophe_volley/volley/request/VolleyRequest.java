package com.debugger.tophe_volley.volley.request;

import java.util.Map;

/**
 * Created by Denis Babak on 15/06/16.
 */
public interface VolleyRequest {

    public int getResponseCode();
    public Map<String, String> getResponseHeaders();
    public void addHeaders(Map<String, String> headers);
}
