package co.tophe.body;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.client.utils.URLEncodedUtils;

import androidx.annotation.NonNull;
import android.util.Log;

import co.tophe.HttpRequestInfo;
import co.tophe.UploadProgressListener;

/**
 * HTTP body class with data sent as {@code form-urlencoded}
 */
public class HttpBodyUrlEncoded implements HttpBodyParameters {

	protected final ArrayList<NameValuePair> mParams;
	private byte[] encodedParams;
	private static final String CONTENT_TYPE = URLEncodedUtils.CONTENT_TYPE + "; charset=utf-8";

	/**
	 * Basic constructor
	 */
	public HttpBodyUrlEncoded() {
		mParams = new ArrayList<NameValuePair>();
	}

	/**
	 * Constructor with an initial amount of parameters to hold
	 * @param capacity amount of parameters the object will get
	 */
	public HttpBodyUrlEncoded(int capacity) {
		mParams = new ArrayList<NameValuePair>(capacity);
	}

	/**
	 * Copy constructor
	 * @param copy body to copy parameters from
	 */
	public HttpBodyUrlEncoded(HttpBodyUrlEncoded copy) {
		this.mParams = new ArrayList<NameValuePair>(copy.mParams);
	}

	private byte[] getEncodedParams() {
		if (null==encodedParams) {
			StringBuilder b = new StringBuilder();

			try {
				for (NameValuePair pair: mParams) {
					if (pair.getValue() == null)
						continue;

					b.append(URLEncoder.encode(pair.getName(), "UTF-8"));
					b.append('=');
					b.append(URLEncoder.encode(pair.getValue(), "UTF-8").replace("*", "%2A"));
					b.append('&');
				}
				encodedParams = b.toString().getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return new byte[]{};
			}

			mParams.clear();
		}
		Log.e("", "length " + encodedParams.length);
		return encodedParams;
	}

	@Override
	public String getContentType() {
		return CONTENT_TYPE;
	}

	@Override
	public long getContentLength() {
		return getEncodedParams().length;
	}

	@Override
	public void writeBodyTo(OutputStream output, HttpRequestInfo request, UploadProgressListener progressListener) throws IOException {
		output.write(getEncodedParams());
	}

	@Override
	public void add(@NonNull String name, String value) {
		mParams.add(new BasicNameValuePair(name, value));
	}

	@Override
	public void add(@NonNull String name, boolean b) {
		add(name, String.valueOf(b));
	}

	@Override
	public void add(@NonNull String name, int i) {
		add(name, Integer.toString(i));
	}

	@Override
	public void add(@NonNull String name, long l) {
		add(name, Long.toString(l));
	}
}
