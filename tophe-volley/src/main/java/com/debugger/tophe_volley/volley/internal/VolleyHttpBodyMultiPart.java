package com.debugger.tophe_volley.volley.internal;

import com.android.volley.Request;

import java.io.File;

import co.tophe.body.HttpBodyMultiPart;

/**
 * Created by Denis Babak on 14/06/16.
 */
public class VolleyHttpBodyMultiPart extends HttpBodyMultiPart implements VolleyBody {
    public VolleyHttpBodyMultiPart(HttpBodyMultiPart sourceBody) {
        super(sourceBody);
    }

    @Override
    public String getContentType() {
        return "multipart/form-data";
    }

    @Override
    public void setOutputData(Request requestBuilder) {
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
}
