/**
 * 
 */
package com.raddle.dlna.http;

import org.apache.http.client.methods.CloseableHttpResponse;

/**
 * @author raddle
 *
 */
public interface HttpCallback {
	public Object httpResponse(CloseableHttpResponse response);
}
