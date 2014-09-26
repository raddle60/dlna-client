package com.raddle.dlna.util;

import java.nio.ByteBuffer;
import java.text.DecimalFormat;

public class ByteUtils {
	private static final long KB = 1024;
	private static final long MB = 1024 * KB;
	private static final long GB = 1024 * MB;

	public static long byteToLong(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.allocate(8);
		if (bytes.length < 8) {
			buffer.put(new byte[8 - bytes.length]);
		}
		buffer.put(bytes);
		buffer.flip();//need flip 
		return buffer.getLong();
	}

	public static byte[] longToByte(long value) {
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.putLong(value);
		return buffer.array();
	}

	public static int byteToInt(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.allocate(4);
		if (bytes.length < 4) {
			buffer.put(new byte[4 - bytes.length]);
		}
		buffer.put(bytes);
		buffer.flip();//need flip 
		return buffer.getInt();
	}

	public static byte[] intToByte(int value) {
		ByteBuffer buffer = ByteBuffer.allocate(4);
		buffer.putInt(value);
		return buffer.array();
	}

	public static short byteToShort(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.allocate(2);
		if (bytes.length < 2) {
			buffer.put(new byte[2 - bytes.length]);
		}
		buffer.put(bytes);
		buffer.flip();//need flip 
		return buffer.getShort();
	}

	public static byte[] shortToByte(short value) {
		ByteBuffer buffer = ByteBuffer.allocate(2);
		buffer.putShort(value);
		return buffer.array();
	}

	public static double byteToDouble(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.put(bytes);
		buffer.flip();//need flip 
		return buffer.getDouble();
	}

	public static byte[] doubleToByte(double value) {
		ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.putDouble(value);
		return buffer.array();
	}

	public static String readable(long bytes) {
		DecimalFormat df = new DecimalFormat("#.###");
		if (bytes >= GB) {
			return df.format((double) bytes / GB) + "GB";
		} else if (bytes >= MB) {
			return df.format((double) bytes / MB) + "MB";
		} else if (bytes >= KB) {
			return df.format((double) bytes / KB) + "KB";
		} else {
			return bytes + "";
		}
	}
}
