package com.raddle.dlna.http;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

public class HttpHelperTest {

	@Test
	public void testGetHttpHeader() throws ClientProtocolException, IOException {
		HttpHeaderInfo httpHeader = HttpHelper.getHttpHeader(
				"http://he.yinyuetai.com/uploads/videos/common/7F3701485E014CF4A502A346B5C9EBCD.flv?sc=f7f2ba7d7d339290", null);
		System.out.println(httpHeader.getHeaders());
		HttpHelper.close();
	}

}
