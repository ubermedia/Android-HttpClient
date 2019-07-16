package com.debugger.tophe_volley.volley;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.RequestQueue;

import co.tophe.HttpEngine;
import co.tophe.HttpEngineFactory;
import co.tophe.HttpResponse;
import co.tophe.ResponseHandler;
import co.tophe.ServerException;
import co.tophe.parser.XferTransform;
import co.tophe.parser.XferTransformChain;
import co.tophe.parser.XferTransformInputStreamHttpStream;

/**
 * Created by Denis Babak on 13/06/16.
 */
public class VolleyHttpEngineFactory implements HttpEngineFactory {

    private static VolleyHttpEngineFactory INSTANCE;
    //public static final int BOGUS_CONSCRYPT_DUAL_FEEDLY = 6587000; // see https://github.com/koush/ion/issues/443
    //public static final int CONSCRYPT_LACKS_SNI = 6599038; // 6587030 to 6599038 don't have it see https://github.com/koush/ion/issues/428

    private RequestQueue volleyQueue;

    public static VolleyHttpEngineFactory getInstance(Context context, RequestQueue queue) {
        if (null == INSTANCE) {
            INSTANCE = new VolleyHttpEngineFactory(context);
            INSTANCE.volleyQueue = queue;
        }
        return INSTANCE;
    }

    private VolleyHttpEngineFactory(Context context) {
        if (context == null) {
            throw new NullPointerException("Volley HTTP request with no Context");
        }

        //volleyQueue = Volley.newRequestQueue(context);
        //IonClient.setupIon(ion);
    }

    /**
     * Get the {@link RequestQueue} instance used by default by TOPHE.
     */
    @NonNull
    public RequestQueue getDefaultVolley() {
        return volleyQueue;
    }

    @Nullable
    @Override
    public <T, SE extends ServerException> HttpEngine<T, SE> createEngine(HttpEngine.Builder<T, SE> builder) {
        return createEngine(builder, volleyQueue, false);
    }

    /**
     *
     * @param builder
     * @param volleyQueue
     * @param allowBogusSSL Sometimes Ion maybe have problems with SSL, especially with Conscrypt, but you may decide to take the
     *                         risk anyway and use it in conditions where it may fail
     * @param <T>
     * @param <SE>
     * @return
     */
    @Nullable
    public <T, SE extends ServerException> HttpEngine<T,SE> createEngine(HttpEngine.Builder<T,SE> builder, RequestQueue volleyQueue, boolean allowBogusSSL) {
        if (!canHandleXferTransform(builder.getResponseHandler().contentParser))
            return null;

        if (!errorCompatibleWithData(builder.getResponseHandler()))
            // Ion returns the data fully parsed so if we don't have common ground to parse the data and the error data, Ion can't handle the request
            return null;

        return new HttpEngineVolley<T,SE>(builder, volleyQueue);
    }

    private static <T> boolean canHandleXferTransform(XferTransform<HttpResponse, T> contentParser) {
        if (contentParser instanceof XferTransformChain) {
            XferTransformChain<HttpResponse, T> parser = (XferTransformChain<HttpResponse, T>) contentParser;
            for (XferTransform transform : parser.transforms) {
                if (transform == XferTransformInputStreamHttpStream.INSTANCE)
                    return false;
            }
        }
        return true;
    }

    /**
     * See if we can find common ground to parse the data and the error data inside Ion
     * @param responseHandler
     * @return whether Ion will be able to parse the data and the error in its processing thread
     */
    private static boolean errorCompatibleWithData(ResponseHandler<?,?> responseHandler) {
        return true;//Utils.getCommonXferTransform(responseHandler.contentParser, responseHandler.errorParser, false) != null;
    }

}
