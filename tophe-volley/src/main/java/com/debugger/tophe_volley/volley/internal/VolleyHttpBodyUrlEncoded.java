package com.debugger.tophe_volley.volley.internal;

import com.android.volley.Request;

import co.tophe.body.HttpBodyUrlEncoded;

/**
 * Created by Denis Babak on 14/06/16.
 */
public class VolleyHttpBodyUrlEncoded extends HttpBodyUrlEncoded implements VolleyBody {

    public VolleyHttpBodyUrlEncoded(HttpBodyUrlEncoded sourceBody) {
        super(sourceBody);
    }

    @Override
    public String getContentType() {
        return "application/x-www-form-urlencoded; charset=utf-8";
    }

    @Override
    public void setOutputData(Request requestBuilder) {
        /*for (NameValuePair param : mParams) {
            requestBuilder.setBodyParameter(param.getName(), param.getValue());
        }*/
    }

}
