package com.raddle.dlna.http;

/**
 * 
 */

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * @author raddle
 * 
 */
public class HttpHelper {
	private final static CloseableHttpClient httpclient;
	private final static CloseableHttpClient minHttpclient;
	static {
		HttpClientBuilder custom = HttpClients.custom();
		custom.setUserAgent("Mozilla/5.0 (Windows NT 6.1; rv:25.0) Gecko/20100101 Firefox/25.0");
		RequestConfig config = RequestConfig.custom().setConnectionRequestTimeout(5 * 1000).setSocketTimeout(30 * 1000)
				.setConnectTimeout(5 * 1000).build();
		custom.setDefaultRequestConfig(config);
		httpclient = custom.build();
		minHttpclient = HttpClients.createMinimal();
	}

	public static String getRemotePage(String url, String charset, Map<Object, Object> headers) throws IOException {
		HttpGet httpGet = new HttpGet(url);
		if (headers != null) {
			for (Map.Entry<Object, Object> entry : headers.entrySet()) {
				httpGet.addHeader(entry.getKey() + "", entry.getValue() + "");
			}
		}
		CloseableHttpResponse response = httpclient.execute(httpGet);
		try {
			HttpEntity entity1 = response.getEntity();
			if (response.getStatusLine().getStatusCode() == 200) {
				return EntityUtils.toString(entity1, charset);
			} else {
				EntityUtils.consume(entity1);
				throw new RuntimeException("获得内容失败:" + response.getStatusLine() + " , " + url);
			}
		} finally {
			response.close();
		}
	}

	public static Object getRemotePageWithCallback(String url, Map<Object, Object> headers, HttpCallback callback)
			throws IOException {
		HttpGet httpGet = new HttpGet(url);
		if (headers != null) {
			for (Map.Entry<Object, Object> entry : headers.entrySet()) {
				httpGet.addHeader(entry.getKey() + "", entry.getValue() + "");
			}
		}
		CloseableHttpResponse response = httpclient.execute(httpGet);
		try {
			return callback.httpResponse(response);
		} finally {
			response.close();
		}
	}

	public static HttpHeaderInfo getHttpHeader(String url, Map<Object, Object> headers) throws ClientProtocolException,
			IOException {
		HttpGet httpGet = new HttpGet(url);
		if (headers != null) {
			for (Map.Entry<Object, Object> entry : headers.entrySet()) {
				httpGet.addHeader(entry.getKey() + "", entry.getValue() + "");
			}
		}
		CloseableHttpResponse response = minHttpclient.execute(httpGet);
		try {
			response.close();
			HttpHeaderInfo headerInfo = new HttpHeaderInfo();
			headerInfo.setStatus(response.getStatusLine().getStatusCode());
			headerInfo.setReasonPhrase(response.getStatusLine().getReasonPhrase());
			Header[] allHeaders = response.getAllHeaders();
			if (allHeaders != null) {
				Map<String, Object> headerMap = new HashMap<String, Object>();
				for (Header header : allHeaders) {
					headerMap.put(header.getName(), header.getValue());
				}
				headerInfo.setHeaders(headerMap);
			}
			return headerInfo;
		} finally {
			response.close();
		}
	}

	public static HttpHeaderInfo getHttpHeaderUseDefaultClient(String url, Map<Object, Object> headers)
			throws ClientProtocolException, IOException {
		HttpGet httpGet = new HttpGet(url);
		if (headers != null) {
			for (Map.Entry<Object, Object> entry : headers.entrySet()) {
				httpGet.addHeader(entry.getKey() + "", entry.getValue() + "");
			}
		}
		CloseableHttpResponse response = httpclient.execute(httpGet);
		try {
			response.close();
			HttpHeaderInfo headerInfo = new HttpHeaderInfo();
			headerInfo.setStatus(response.getStatusLine().getStatusCode());
			headerInfo.setReasonPhrase(response.getStatusLine().getReasonPhrase());
			Header[] allHeaders = response.getAllHeaders();
			if (allHeaders != null) {
				Map<String, Object> headerMap = new HashMap<String, Object>();
				for (Header header : allHeaders) {
					headerMap.put(header.getName(), header.getValue());
				}
				headerInfo.setHeaders(headerMap);
			}
			return headerInfo;
		} finally {
			response.close();
		}
	}

	public static String encodeUrl(String url, String charset) throws UnsupportedEncodingException {
		return URLEncoder.encode(url, charset);
	}

	public static void close() {
		try {
			httpclient.close();
		} catch (IOException e) {
		}
	}
}
