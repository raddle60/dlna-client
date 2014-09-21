package com.raddle.dlna.video.flv.tag;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import com.raddle.dlna.util.ByteUtils;

public class TagHeader {
	private byte tagType;
	private int dataLength;
	private int timestamp;
	private byte extTimestamp;
	private int streamsID;

	public static TagHeader readTagHeader(InputStream inputStream) throws IOException {
		byte[] tagHeadBytes = new byte[11];
		int read = inputStream.read(tagHeadBytes);
		if (read != 11) {
			return null;
		}
		TagHeader tagHeader = new TagHeader();
		tagHeader.setTagType(tagHeadBytes[0]);
		int tagDataLength = ByteUtils.byteToInt(Arrays.copyOfRange(tagHeadBytes, 1, 4));
		tagHeader.setDataLength(tagDataLength);
		int tagTimestamp = ByteUtils.byteToInt(Arrays.copyOfRange(tagHeadBytes, 4, 7));
		tagHeader.setTimestamp(tagTimestamp);
		tagHeader.setExtTimestamp(tagHeadBytes[7]);
		tagHeader.setStreamsID(ByteUtils.byteToInt(Arrays.copyOfRange(tagHeadBytes, 8, 11)));
		return tagHeader;
	}

	public void writeTagHeader(OutputStream outputStream) throws IOException {
		outputStream.write(tagType);
		outputStream.write(ByteUtils.intToByte(dataLength), 1, 3);
		outputStream.write(ByteUtils.intToByte(timestamp), 1, 3);
		outputStream.write(extTimestamp);
		outputStream.write(ByteUtils.intToByte(streamsID), 1, 3);
	}

	public byte getTagType() {
		return tagType;
	}

	public void setTagType(byte tagType) {
		this.tagType = tagType;
	}

	public int getDataLength() {
		return dataLength;
	}

	public void setDataLength(int dataLength) {
		this.dataLength = dataLength;
	}

	public int getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(int timestamp) {
		this.timestamp = timestamp;
	}

	public byte getExtTimestamp() {
		return extTimestamp;
	}

	public void setExtTimestamp(byte extTimestamp) {
		this.extTimestamp = extTimestamp;
	}

	public int getStreamsID() {
		return streamsID;
	}

	public void setStreamsID(int streamsID) {
		this.streamsID = streamsID;
	}

}
