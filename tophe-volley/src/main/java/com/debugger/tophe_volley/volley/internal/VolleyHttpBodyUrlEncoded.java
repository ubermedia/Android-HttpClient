package com.debugger.tophe_volley.volley.internal;

import com.android.volley.Request;

import java.util.HashMap;
import java.util.Map;

import co.tophe.body.HttpBodyUrlEncoded;
import co.tophe.body.NameValuePair;

/**
 * Created by Denis Babak on 14/06/16.
 */
public class VolleyHttpBodyUrlEncoded extends HttpBodyUrlEncoded implements VolleyBody {

    public VolleyHttpBodyUrlEncoded(HttpBodyUrlEncoded sourceBody) {
        super(sourceBody);
    }

    protected Map<String, String> params = new HashMap<String, String>();

    public Map<String, String> getParams() {
        if(params.isEmpty())
            for (NameValuePair param : mParams) {
                params.put(param.getName(), param.getValue());
                //requestBuilder.setBodyParameter(param.getName(), param.getValue());
            }
        return params;
    }

    @Override
    public String getContentType() {
        return "application/x-www-form-urlencoded; charset=utf-8";
        //return "application/json;";
    }

    @Override
    public long getContentLength() {
        return super.getContentLength();
    }

    @Override
    public void setOutputData(Request requestBuilder) {
    }

}
