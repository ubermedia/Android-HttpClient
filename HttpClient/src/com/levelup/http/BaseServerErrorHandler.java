package com.levelup.http;

import java.io.IOException;
import java.io.InputStream;

import com.levelup.http.parser.ParserException;
import com.levelup.http.parser.XferTransformInputStreamString;
import com.levelup.http.parser.XferTransformResponseInputStream;
import com.levelup.http.parser.XferTransformStringJSONObject;

/**
 * Created by robUx4 on 26/08/2014.
 */
public class BaseServerErrorHandler extends ServerErrorHandler<InputStream, ServerException> {
	public static final BaseServerErrorHandler INSTANCE = new BaseServerErrorHandler();

	public static final MediaType MediaTypeJSON = MediaType.parse("application/json");

	public BaseServerErrorHandler() {
		super(XferTransformResponseInputStream.INSTANCE);
	}

	@Override
	public ServerException exceptionFromErrorData(InputStream errorStream, ImmutableHttpRequest request) throws IOException, ParserException {
		Object errorData = null;
		MediaType type = MediaType.parse(request.getHttpResponse().getContentType());
		if (MediaTypeJSON.equalsType(type)) {
			try {
				errorData = XferTransformStringJSONObject.INSTANCE.transformData(
						XferTransformInputStreamString.INSTANCE.transformData(errorStream, request)
						, request
				);
			} finally {
				errorStream.close();
			}
		} else if (null == type || "text".equals(type.type())) {
			try {
				errorData = XferTransformInputStreamString.INSTANCE.transformData(errorStream, request);
			} finally {
				errorStream.close();
			}
		} else {
			errorData = errorStream;
		}
		return new ServerException.Builder(request, errorData).build();
	}
}