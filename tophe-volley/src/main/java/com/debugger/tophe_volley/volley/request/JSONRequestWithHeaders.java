package com.debugger.tophe_volley.volley.request;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.debugger.tophe_volley.volley.internal.ByteBufferList;
import com.debugger.tophe_volley.volley.internal.ByteBufferListInputStream;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Denis Babak on 16/06/16.
 */
public class JSONRequestWithHeaders extends JsonObjectRequest implements VolleyRequest {

    public JSONRequestWithHeaders(int method, String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
    }

    public JSONRequestWithHeaders(String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(url, jsonRequest, listener, errorListener);
    }

    ByteBufferList bbList;
    public ByteBufferListInputStream bbIs;

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        bbList = new ByteBufferList(response.data);
        bbIs = new ByteBufferListInputStream(bbList);
        code = response.statusCode;
        responseHeaders = new HashMap<>();
        responseHeaders.putAll(response.headers);
        //return super.parseNetworkResponse(response);
        /*try {
            String jsonString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));
            return Response.success(new JSONObject(jsonString),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }*/




        try {
            String jsonString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));

            JSONObject result = null;

            if (jsonString != null && jsonString.length() > 0)
                result = new JSONObject(jsonString);

            return Response.success(result,
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }
    }

    @Override
    public int getResponseCode() {
        return code;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        if(customHeaders != null && customHeaders.size() > 0) {
            customHeaders.putAll(super.getHeaders());
            return customHeaders;
        }
        return super.getHeaders();
    }

    private Map<String, String> customHeaders = new HashMap<>();
    private Map<String, String> responseHeaders;
    private int code = -1;

    @Override
    public void addHeaders(Map<String, String> collection) {
        this.customHeaders.putAll(collection);
    }

    @Override
    public Map<String, String> getResponseHeaders() {
        return responseHeaders;
    }
}
