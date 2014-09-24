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
public class ScriptDataShort implements ScriptData {
	private Short value;

	@Override
	public void read(InputStream inputStream) throws IOException {
		byte[] buffer = new byte[2];
		inputStream.read(buffer);
		value = ByteUtils.byteToShort(buffer);
	}

	@Override
	public void write(OutputStream outputStream) throws IOException {
		outputStream.write(7);//对象类型
		outputStream.write(ByteUtils.shortToByte(value));
	}

	@Override
	public Short getValue() {
		return value;
	}

}
