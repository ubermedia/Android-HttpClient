package com.debugger.tophe_volley.volley.internal;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Denis Babak on 17/06/16.
 */
public class ByteBufferListInputStream extends InputStream {
    ByteBufferList bb;
    public ByteBufferListInputStream(ByteBufferList bb) {
        this.bb = bb;
    }

    @Override
    public int read() throws IOException {
        if (bb.remaining() <= 0)
            return -1;
        return bb.get();
    }

    @Override
    public int read(byte[] buffer) throws IOException {
        return this.read(buffer, 0, buffer.length);
    }

    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
        if (bb.remaining() <= 0)
            return -1;
        int toRead = Math.min(length, bb.remaining());
        bb.get(buffer, offset, toRead);
        return toRead;
    }
}
