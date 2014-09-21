package com.raddle.dlna.video.flv;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import com.raddle.dlna.util.ByteUtils;

/**
 * description: 
 * @author raddle
 * time : 2014年9月21日 下午4:34:02
 */
public class FlvHeader {
	private String fileType;
	private byte version;
	private byte streamFlag;
	private byte[] extHeader;
	private int length;
	private int preTagLength;

	public static FlvHeader readFlvHeader(InputStream inputStream) throws IOException {
		byte[] headers = new byte[9];
		inputStream.read(headers);
		String fileType = new String(Arrays.copyOfRange(headers, 0, 3));
		if (!fileType.equals("FLV")) {
			// 不是flv视频
			return null;
		}
		FlvHeader flvHeader = new FlvHeader();
		flvHeader.setFileType(fileType);
		flvHeader.setVersion(headers[3]);
		flvHeader.setStreamFlag(headers[4]);
		flvHeader.setLength(ByteUtils.byteToInt(Arrays.copyOfRange(headers, 5, 9)));
		if (flvHeader.getLength() > headers.length) {
			byte[] ext = new byte[flvHeader.getLength() - headers.length];
			inputStream.read(ext);
			flvHeader.setExtHeader(ext);
		}
		byte[] preTagSizeBytes = new byte[4];
		inputStream.read(preTagSizeBytes);
		flvHeader.setPreTagLength(ByteUtils.byteToInt(preTagSizeBytes));
		return flvHeader;
	}

	public void writeFlvHeader(OutputStream outputStream) throws IOException {
		outputStream.write(fileType.getBytes());
		outputStream.write(version);
		outputStream.write(streamFlag);
		outputStream.write(ByteUtils.intToByte(length));
		if (extHeader != null) {
			outputStream.write(extHeader);
		}
		outputStream.write(ByteUtils.intToByte(preTagLength));
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public byte getVersion() {
		return version;
	}

	public void setVersion(byte version) {
		this.version = version;
	}

	public byte getStreamFlag() {
		return streamFlag;
	}

	public void setStreamFlag(byte streamFlag) {
		this.streamFlag = streamFlag;
	}

	public byte[] getExtHeader() {
		return extHeader;
	}

	public void setExtHeader(byte[] extHeader) {
		this.extHeader = extHeader;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getPreTagLength() {
		return preTagLength;
	}

	public void setPreTagLength(int preTagLength) {
		this.preTagLength = preTagLength;
	}
}
