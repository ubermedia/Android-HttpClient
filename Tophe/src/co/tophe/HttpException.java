package co.tophe;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Exception that will occur by using {@link TopheClient}, Anything that is not a clean error generated by the HTTP server.
 *
 * @see co.tophe.ServerException
 */
public class HttpException extends TopheException {

	private static final long serialVersionUID = 4993791558983072165L;

	protected HttpException(@NonNull AbstractBuilder builder) {
		super(builder.httpRequest, builder.response, builder.errorMessage);
		initCause(builder.cause);
	}

	/**
	 * Builder for a {@link co.tophe.HttpException} child.
	 *
	 * @param <EXCEPTION> The class of the exception that will be raised.
	 * @param <BUILDER>   The build type that should be used to raise the {@link EXCEPTION}.
	 */
	public static abstract class AbstractBuilder<EXCEPTION extends HttpException, BUILDER extends AbstractBuilder<EXCEPTION, ?>> {
		private final HttpRequestInfo httpRequest;
		private final HttpResponse response;
		private String errorMessage;
		private Throwable cause;

		public AbstractBuilder(@NonNull HttpRequestInfo httpRequest, @Nullable HttpResponse response) {
			this.httpRequest = httpRequest;
			this.response = response;
		}

		public BUILDER setErrorMessage(String errorMessage) {
			this.errorMessage = errorMessage;
			return (BUILDER) this;
		}

		public BUILDER setCause(Throwable cause) {
			this.cause = cause;
			return (BUILDER) this;
		}

		public abstract EXCEPTION build();
	}

	public static class Builder extends AbstractBuilder<HttpException, Builder> {
		public Builder(@NonNull HttpRequestInfo httpRequest, @Nullable HttpResponse response) {
			super(httpRequest, response);
		}

		@Override
		public HttpException build() {
			return new HttpException(this);
		}
	}
}
