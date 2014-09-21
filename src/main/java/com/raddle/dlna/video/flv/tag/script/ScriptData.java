package com.raddle.dlna.video.flv.tag.script;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface ScriptData {
	/**
	 * 需要支持mark,在检查是否end时，需要reset
	 * @param inputStream
	 */
	public void read(InputStream inputStream) throws IOException;

	public void write(OutputStream outputStream) throws IOException;

	public Object getValue();
}
