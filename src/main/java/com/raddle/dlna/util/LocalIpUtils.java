/**
 * 
 */
package com.raddle.dlna.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * @author raddle
 *
 */
public class LocalIpUtils {
	public static List<String> getLocalIpv4() {
		try {
			List<String> ips = new ArrayList<String>();
			for (Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces(); interfaces
					.hasMoreElements();) {
				NetworkInterface networkInterface = interfaces.nextElement();
				if (networkInterface.isLoopback() || networkInterface.isVirtual() || !networkInterface.isUp()) {
					continue;
				}
				Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
				if (addresses.hasMoreElements()) {
					InetAddress nextElement = addresses.nextElement();
					if (nextElement instanceof Inet4Address) {
						ips.add(nextElement.getHostAddress());
					}
				}
			}
			return ips;
		} catch (SocketException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}
