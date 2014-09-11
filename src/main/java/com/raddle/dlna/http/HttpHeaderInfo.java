/**
 * 
 */
package com.raddle.dlna.http;

import java.util.Map;

/**
 * @author raddle
 *
 */
public class HttpHeaderInfo {
	private int status;
	private String reasonPhrase;
	private Map<String, Object> headers;

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getReasonPhrase() {
		return reasonPhrase;
	}

	public void setReasonPhrase(String reasonPhrase) {
		this.reasonPhrase = reasonPhrase;
	}

	public Map<String, Object> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, Object> headers) {
		this.headers = headers;
	}

}
