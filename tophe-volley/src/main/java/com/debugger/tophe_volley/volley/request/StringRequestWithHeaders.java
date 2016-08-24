package com.debugger.tophe_volley.volley.request;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.debugger.tophe_volley.volley.internal.ByteBufferList;
import com.debugger.tophe_volley.volley.internal.ByteBufferListInputStream;
import com.debugger.tophe_volley.volley.internal.VolleyBody;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Denis Babak on 15/06/16.
 */
public class StringRequestWithHeaders extends StringRequest implements VolleyRequest {
    public StringRequestWithHeaders(int method, String url, VolleyBody params, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }

    public StringRequestWithHeaders(String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(url, listener, errorListener);
    }

    protected Map<String, String> customHeaders = new HashMap<>();
    protected Map<String, String> params;

    @Override
    public void addHeaders(Map<String, String> collection) {
        this.customHeaders.putAll(collection);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return params;
    }

    @Override
    public int getResponseCode() {
        return code;
    }

    @Override
    public Map<String, String> getResponseHeaders() {
        return responseHeaders;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        if(customHeaders != null && customHeaders.size() > 0) {
            return customHeaders;
        }
        return super.getHeaders();
    }

    public ByteBufferListInputStream is;

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        code = response.statusCode;
        responseHeaders = new HashMap<>();
        responseHeaders.putAll(response.headers);
        is = new ByteBufferListInputStream(new ByteBufferList(response.data));
        return super.parseNetworkResponse(response);
    }

    protected int code = -1;
    protected Map<String, String> responseHeaders;
}
