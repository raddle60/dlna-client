package com.raddle.dlna.video.flv.tag.script;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.raddle.dlna.util.ByteUtils;

/**
 * description: 
 * @author raddle
 * time : 2014年9月21日 下午2:41:10
 */
public class ScriptDataLongString implements ScriptData {
	private String value;

	@Override
	public void read(InputStream inputStream) throws IOException {
		byte[] strLengthBytes = new byte[4];
		inputStream.read(strLengthBytes);
		int strLength = ByteUtils.byteToInt(strLengthBytes);
		byte[] strBytes = new byte[strLength];
		inputStream.read(strBytes);
		value = new String(strBytes);
	}

	@Override
	public void write(OutputStream outputStream) throws IOException {
		outputStream.write(12);//对象类型
		byte[] strBytes = value.getBytes();
		outputStream.write(ByteUtils.intToByte(strBytes.length));
		outputStream.write(strBytes);
	}

	@Override
	public Object getValue() {
		return value;
	}

}
