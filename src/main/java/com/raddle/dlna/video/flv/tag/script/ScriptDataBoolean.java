package com.raddle.dlna.video.flv.tag.script;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * description: 
 * @author raddle
 * time : 2014年9月21日 下午2:41:10
 */
public class ScriptDataBoolean implements ScriptData {
	private Boolean value;

	@Override
	public void read(InputStream inputStream) throws IOException {
		byte[] buffer = new byte[1];
		inputStream.read(buffer);
		value = 1 == buffer[0];
	}

	@Override
	public void write(OutputStream outputStream) throws IOException {
		outputStream.write(1);//对象类型
		outputStream.write(value == true ? 1 : 0);
	}

	@Override
	public Boolean getValue() {
		return value;
	}

}
