package com.raddle.dlna.http;

/**
 * description: 
 * @author raddle
 * time : 2014年9月26日 下午10:42:19
 */
public interface ReceiveSpeedCallback {
	public void receivedBytes(int videIndex, int totalSegments, long receivedBytes);

	public void receivedComplete(int videIndex, int totalSegments);
}
