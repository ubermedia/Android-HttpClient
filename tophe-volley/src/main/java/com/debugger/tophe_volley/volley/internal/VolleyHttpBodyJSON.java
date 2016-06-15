package com.debugger.tophe_volley.volley.internal;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import co.tophe.body.HttpBodyJSON;

/**
 * Created by Denis Babak on 13/06/16.
 */
public class VolleyHttpBodyJSON extends HttpBodyJSON implements VolleyBody {

    public VolleyHttpBodyJSON(HttpBodyJSON sourceBody) {
        super(sourceBody);
    }

    @Override
    public String getContentType() {
        return "application/json";
    }

    @Override
    public void setOutputData(Request requestBuilder) {
    }
}
