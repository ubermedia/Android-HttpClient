package co.tophe;

import java.net.ProtocolException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Exception when the wrong IP protocol is used by the HTTP engine.
 *
 * @author Created by robUx4 on 24/09/2014.
 * @see java.net.ProtocolException
 */
public class HttpProtocolException extends HttpException {
	protected HttpProtocolException(@NonNull Builder builder) {
		super(builder);
	}

	public static class Builder extends AbstractBuilder<HttpProtocolException, Builder> {

		public Builder(@NonNull HttpRequestInfo httpRequest, @Nullable HttpResponse response, @NonNull ProtocolException e) {
			super(httpRequest, response);
			super.setCause(e);
		}

		@Override
		public Builder setCause(Throwable cause) {
			throw new IllegalStateException("pass the parser exception in the constructor");
		}

		@Override
		public HttpProtocolException build() {
			return new HttpProtocolException(this);
		}
	}
}
