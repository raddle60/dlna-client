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
public class ScriptDataDate implements ScriptData {
	private Double value;
	private byte[] offset = new byte[2];

	@Override
	public void read(InputStream inputStream) throws IOException {
		byte[] buffer = new byte[8];
		inputStream.read(buffer);
		value = ByteUtils.byteToDouble(buffer);
		inputStream.read(offset);
	}

	@Override
	public void write(OutputStream outputStream) throws IOException {
		outputStream.write(11);//对象类型
		outputStream.write(ByteUtils.doubleToByte(value));
		outputStream.write(offset);
	}

	@Override
	public Double getValue() {
		return value;
	}

}
