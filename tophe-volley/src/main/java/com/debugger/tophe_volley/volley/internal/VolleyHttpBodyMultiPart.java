package com.debugger.tophe_volley.volley.internal;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.VolleyLog;

import java.io.File;

import co.tophe.body.HttpBodyMultiPart;

/**
 * Created by Denis Babak on 14/06/16.
 */
public class VolleyHttpBodyMultiPart extends HttpBodyMultiPart implements VolleyBody {

    private long contentLength = -1;

    public VolleyHttpBodyMultiPart(HttpBodyMultiPart sourceBody) {
        super(sourceBody);
        /*for (HttpParam param : mParams) {
            if (param.value instanceof File) {
                FilePart part = new FilePart(param.name, (File) param.value);
                if (!TextUtils.isEmpty(param.contentType))
                    part.setContentType(param.contentType);
                part.getRawHeaders().add("Content-Transfer-Encoding", "binary");
                List<Part> partList = new ArrayList<Part>(1);
                partList.add(part);
                requestBuilder.addMultipartParts(partList);
            } else if (param.value instanceof InputStream) {
                InputStreamPart part = new InputStreamPart(param.name, (InputStream) param.value, param.length);
                if (!TextUtils.isEmpty(param.contentType))
                    part.setContentType(param.contentType);
                part.getRawHeaders().add("Content-Transfer-Encoding", "binary");
                List<Part> partList = new ArrayList<Part>(1);
                partList.add(part);
                requestBuilder.addMultipartParts(partList);
            }
        }
        for (HttpParam param : mParams) {
            if (param.value instanceof String) {
                requestBuilder.setMultipartParameter(param.name, (String) param.value);
            }
        }*/
    }

    @Override
    public String getContentType() {
        return "multipart/form-data; boundary=t0Ph3Multip4rt; charset=UTF-8";
    }

    public void setContentLength(long length) {
        this.contentLength = length;
    }

    @Override
    public long getContentLength() {
        if(contentLength >= 0)
            return contentLength;
        else
            return super.getContentLength();

    }

    @Override
    public void setOutputData(Request requestBuilder) {
    }
}
