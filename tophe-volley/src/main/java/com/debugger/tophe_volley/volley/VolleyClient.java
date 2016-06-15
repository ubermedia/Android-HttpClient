package com.debugger.tophe_volley.volley;

import android.content.Context;

import co.tophe.TopheClient;
import co.tophe.engine.HttpEngineFactoryFallback;

/**
 * Created by Denis Babak on 13/06/16.
 */
public class VolleyClient {

    public static void setup(Context context) {
        VolleyHttpEngineFactory factory = VolleyHttpEngineFactory.getInstance(context);
        TopheClient.setHttpEngineFactory(new HttpEngineFactoryFallback(factory, TopheClient.getHttpEngineFactory()));
        TopheClient.setup(context);
    }

}
