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
public class ScriptDataDouble implements ScriptData {
	private Double value;

	public ScriptDataDouble() {
	}

	public ScriptDataDouble(Double value) {
		this.value = value;
	}

	@Override
	public void read(InputStream inputStream) throws IOException {
		byte[] buffer = new byte[8];
		inputStream.read(buffer);
		value = ByteUtils.byteToDouble(buffer);
	}

	@Override
	public void write(OutputStream outputStream) throws IOException {
		outputStream.write(0);//对象类型
		outputStream.write(ByteUtils.doubleToByte(value));
	}

	@Override
	public Object getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}

}
