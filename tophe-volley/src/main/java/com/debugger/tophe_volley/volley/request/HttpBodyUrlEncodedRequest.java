package com.debugger.tophe_volley.volley.request;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.debugger.tophe_volley.volley.internal.VolleyHttpBodyUrlEncoded;

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

    @Override
    public byte[] getBody() throws AuthFailureError {
        Log.e("", "BODY " + super.getBody().toString());
        return super.getBody();
    }

    @Override
    public String getBodyContentType() {
        return "application/x-www-form-urlencoded; charset=" + getParamsEncoding();
    }
}
