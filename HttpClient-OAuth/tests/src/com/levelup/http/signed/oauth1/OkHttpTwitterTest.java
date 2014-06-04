package com.levelup.http.signed.oauth1;

import oauth.signpost.exception.OAuthException;

import com.levelup.http.HttpClient;
import com.levelup.http.HttpRequest;
import com.levelup.http.okhttp.OkHttpClient;

public class OkHttpTwitterTest extends AbstractTwitterTest {
	protected void setUp() throws Exception {
		super.setUp();
		HttpClient.setConnectionFactory(OkHttpClient.instance);
	};

	public void testRequestTokenBlacklist() {
		try {
			OkHttpClient.addUrlBlacklist(TWITTER_REQUEST_TOKEN);
			assertNotNull(twitterAppProvider.retrieveRequestToken("androidhttp://request_token/"));
		} catch (OAuthException e) {
			fail(e.getMessage());
		} finally {
			OkHttpClient.removeUrlBlacklist(TWITTER_REQUEST_TOKEN);
		}
	}

	public void testIdentityEncoding() throws Exception {
		HttpRequest search = getSearchRequest();
		search.setHeader(HttpClient.ACCEPT_ENCODING, "identity");
		String response = HttpClient.getStringResponse(search);
		assertNotNull(response);
		assertTrue(response.length() > 0);
		assertEquals('{', response.charAt(0));
	}

	public void testGzipEncoding() throws Exception {
		HttpRequest search = getSearchRequest();
		search.setHeader(HttpClient.ACCEPT_ENCODING, "gzip");
		String response = HttpClient.getStringResponse(search);
		assertNotNull(response);
		assertTrue(response.length() > 0);
		assertEquals('{', response.charAt(0));
	}

	public void testDirectTransport() throws Exception {
		HttpRequest search = getSearchRequest();
		search.setHeader("X-Android-Transports", "http/1.1");
		String response = HttpClient.getStringResponse(search);
		assertNotNull(response);
		assertTrue(response.length() > 0);
		assertEquals('{', response.charAt(0));
	}

	public void testBlacklistTransport() throws Exception {
		HttpRequest search = getSearchRequest();
		OkHttpClient.addUrlBlacklist(search.getUri().toString());
		try {
			String response = HttpClient.getStringResponse(search);
			assertNotNull(response);
			assertTrue(response.length() > 0);
			assertEquals('{', response.charAt(0));
		} finally {
			OkHttpClient.removeUrlBlacklist(search.getUri().toString());
		}
	}
}