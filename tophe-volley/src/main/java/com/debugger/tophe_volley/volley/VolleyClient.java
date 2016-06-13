package com.debugger.tophe_volley.volley;

import com.android.volley.toolbox.Volley;

/**
 * Created by Denis Babak on 13/06/16.
 */
public class VolleyClient {

    public void setupVolley(Volley volley) throws IllegalArgumentException {
        if(volley == null)
            throw new IllegalArgumentException("Volley can't be null");

    }

}
