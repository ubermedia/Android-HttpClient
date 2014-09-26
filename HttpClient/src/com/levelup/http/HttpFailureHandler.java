package com.levelup.http;

import java.io.IOException;

import com.levelup.http.parser.ParserException;

/**
 * Handle the HTTP response body when the server returned an error
 * @author Created by robUx4 on 26/08/2014.
 */
public interface HttpFailureHandler {
	/**
	 * Parse the server error {@link com.levelup.http.HttpResponse}
	 * @param request The request that created the server error
	 * @return The {@link HttpFailureException} containing the parsed server error data
	 * @throws IOException
	 * @throws ParserException
	 */
	HttpFailureException getHttpFailureException(ImmutableHttpRequest request) throws IOException, ParserException;
}
