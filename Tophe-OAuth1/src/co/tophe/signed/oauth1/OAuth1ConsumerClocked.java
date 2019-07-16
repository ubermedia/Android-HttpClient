package co.tophe.signed.oauth1;

import java.io.InputStream;
import java.util.Date;


import androidx.annotation.NonNull;
import android.text.TextUtils;

import co.tophe.BaseResponseHandler;
import co.tophe.HttpRequest;
import co.tophe.HttpResponse;
import co.tophe.parser.XferTransformResponseInputStream;
import co.tophe.signed.OAuthClientApp;
import co.tophe.utils.DateUtils;


/**
 * An {@link HttpClientOAuth1Consumer OAuth Consumer} that can handle a device clock difference between the client and the server transparently.
 */
public class OAuth1ConsumerClocked extends HttpClientOAuth1Consumer {

	private static final long serialVersionUID = 3963386898609696262L;

	private long serverDelayInMilliseconds;

	final BaseResponseHandler<InputStream> responseHandler = new BaseResponseHandler<InputStream>(XferTransformResponseInputStream.INSTANCE) {
		@Override
		public void onHttpResponse(@NonNull HttpRequest request, @NonNull HttpResponse response) {
			super.onHttpResponse(request, response);

			String serverDate = response.getHeaderField("Date");
			if (!TextUtils.isEmpty(serverDate)) {
				setServerDate(serverDate);
			}
		}
	};

	public long getServerTime() {
		return System.currentTimeMillis() - serverDelayInMilliseconds;
	}

	@Override
	protected String generateTimestamp() {
		return String.valueOf(getServerTime() / 1000);
	}

	protected void setServerDate(String value) {
		long now = System.currentTimeMillis();
		Date serverDate = DateUtils.parseDate(value);
		if (null != serverDate) {
			serverDelayInMilliseconds = now - serverDate.getTime();
		}
	}

	public OAuth1ConsumerClocked(OAuthClientApp clientApp) {
		super(clientApp);
	}
}