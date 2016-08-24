package com.debugger.tophe_volley.volley;

import android.content.Context;

import com.android.volley.RequestQueue;

import co.tophe.TopheClient;
import co.tophe.engine.HttpEngineFactoryFallback;

/**
 * Created by Denis Babak on 13/06/16.
 */
public class VolleyClient {

    public static void setup(Context context, RequestQueue queue) {
        VolleyHttpEngineFactory factory = VolleyHttpEngineFactory.getInstance(context, queue);
        TopheClient.setHttpEngineFactory(new HttpEngineFactoryFallback(factory, TopheClient.getHttpEngineFactory()));
        TopheClient.setup(context);
    }

}
