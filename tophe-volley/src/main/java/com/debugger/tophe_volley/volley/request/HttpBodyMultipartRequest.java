package com.debugger.tophe_volley.volley.request;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.debugger.tophe_volley.volley.internal.VolleyHttpBodyMultiPart;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.tophe.body.HttpBodyMultiPart;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.mime.HttpMultipartMode;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;

/**
 * Created by Denis Babak on 07/07/16.
 */
public class HttpBodyMultipartRequest extends StringRequestWithHeaders {

    public HttpBodyMultipartRequest(int method, String url, VolleyHttpBodyMultiPart params, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(method, url, params, listener, errorListener);
        mListener = listener;
        this.mFilePartData = new ArrayList<>();
        this.mStringPart = new HashMap<>();
        for(HttpBodyMultiPart.HttpParam p : params.mParams) {
            if (p.value instanceof File) {
                mFilePartData.add(p);
            } else if(p.value instanceof InputStream) {
                //???
            }
        }
        for(HttpBodyMultiPart.HttpParam p : params.mParams) {
            if(p.value instanceof String) {
                mStringPart.put(p.name, (String)p.value);
            }
        }
        /*this.mFilePartData = mFilePartData;
        this.mStringPart = mStringPart;
        this.mHeaderPart = mHeaderPart;
        this.mContext = mContext;*/
        mEntityBuilder.setMode(HttpMultipartMode.STRICT);
        mEntityBuilder.setBoundary(HttpBodyMultiPart.boundary);
        mEntityBuilder.setCharset(Charset.forName("UTF-8"));
        mEntityBuilder.seContentType(ContentType.MULTIPART_FORM_DATA);
        buildMultipartFileEntity();
        buildMultipartTextEntity();
        mHttpEntity = mEntityBuilder.build();
        params.setContentLength(mHttpEntity.getContentLength());
        params.apacheContentLength = mHttpEntity.getContentLength();
    }

    public long getContentLength() {
        return mHttpEntity.getContentLength();
    }

    @Override
    public String getBodyContentType() {
        //return "multipart/form-data;boundary=" + HttpBodyMultiPart.boundary + "; charset=UTF-8";
        return mHttpEntity.getContentType().getValue();
    }

    private final Response.Listener<String> mListener;
    private final List<HttpBodyMultiPart.HttpParam> mFilePartData;
    private final Map<String, String> mStringPart;

    private MultipartEntityBuilder mEntityBuilder = MultipartEntityBuilder.create();
    private HttpEntity mHttpEntity;

    /*public static String getMimeType(Context context, String url) {
        Uri uri = Uri.fromFile(new File(url));
        String mimeType = null;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = context.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }*/

    private void buildMultipartFileEntity() {
        for (HttpBodyMultiPart.HttpParam entry : mFilePartData) {
            try {
                String key = entry.name;
                File file = (File)entry.value;
                String mimeType = entry.contentType;
                mEntityBuilder.addBinaryBody(key, file, ContentType.create(mimeType), file.getName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void buildMultipartTextEntity() {
        for (Map.Entry<String, String> entry : mStringPart.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key != null && value != null)
                mEntityBuilder.addTextBody(key, value);
        }
    }


    /*@Override
    public String getBodyContentType() {
        return mHttpEntity.getContentType().getValue();
    }*/

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            mHttpEntity.writeTo(bos);
        } catch (IOException e) {
            VolleyLog.e("IOException writing to ByteArrayOutputStream");
        }
        return bos.toByteArray();
    }


}
