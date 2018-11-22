package com.debugger.tophe_volley.volley.request;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.debugger.tophe_volley.volley.internal.VolleyHttpBodyUrlEncoded;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by Denis Babak on 06/07/16.
 */
public class HttpBodyUrlEncodedRequest extends StringRequestWithHeaders {

    public HttpBodyUrlEncodedRequest(int method, String url, VolleyHttpBodyUrlEncoded params, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(method, url, params, listener, errorListener);
        this.params = params.getParams();
    }

    public HttpBodyUrlEncodedRequest(String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(url, listener, errorListener);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return super.getParams();
    }

    private byte[] encodeParameters(Map<String, String> params, String paramsEncoding) {
        StringBuilder encodedParams = new StringBuilder();
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                encodedParams.append(URLEncoder.encode(entry.getKey(), paramsEncoding));
                encodedParams.append('=');
                encodedParams.append(URLEncoder.encode(entry.getValue(), paramsEncoding).replace("*", "%2A"));
                encodedParams.append('&');
            }
            return encodedParams.toString().getBytes(paramsEncoding);
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: " + paramsEncoding, uee);
        }
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        Log.e("", "BODY " + super.getBody().toString());
        Map<String, String> params = getParams();
        if (params != null && params.size() > 0) {
            return encodeParameters(params, getParamsEncoding());
        }
        return null;
    }

    @Override
    public String getBodyContentType() {
        return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
    }
}
