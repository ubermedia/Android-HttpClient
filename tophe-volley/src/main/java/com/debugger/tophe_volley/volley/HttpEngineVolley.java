package com.debugger.tophe_volley.volley;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import com.debugger.tophe_volley.volley.internal.HttpResponseVolley;

import co.tophe.AbstractHttpEngine;
import co.tophe.HttpException;
import co.tophe.HttpResponse;
import co.tophe.ServerException;

/**
 * Created by Denis Babak on 10/06/16.
 */
public class HttpEngineVolley<T, SE extends ServerException> extends AbstractHttpEngine<T, SE, HttpResponseVolley<T>> {

    Response<T> response;

    public HttpEngineVolley(Builder<T, SE> builder) {
        super(builder);
    }

    @Override
    protected void setHeadersAndConfig() {

    }

    @Override
    protected String getEngineSignature() {
        return null;
    }

    @Override
    protected HttpResponseVolley queryResponse() throws ServerException, HttpException {
        return null;
    }
}
