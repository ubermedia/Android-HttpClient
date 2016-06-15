package com.debugger.tophe_volley.volley.internal;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import co.tophe.body.HttpBodyJSON;
import co.tophe.body.HttpBodyString;

/**
 * Created by Denis Babak on 14/06/16.
 */
public class VolleyHttpBodyString extends HttpBodyString implements VolleyBody {

    String url;

    public VolleyHttpBodyString(HttpBodyString sourceBody, String url) {
        super(sourceBody);
        this.url = url;
    }

    @Override
    public String getContentType() {
        return "text/plain";
    }

    @Override
    public void setOutputData(Request requestBuilder) {
        //requestBuilder.setStringBody(value);
    }
}
