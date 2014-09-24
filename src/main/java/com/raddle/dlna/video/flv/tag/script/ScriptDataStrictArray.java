package com.raddle.dlna.video.flv.tag.script;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.raddle.dlna.util.ByteUtils;

/**
 * description: 
 * @author raddle
 * time : 2014年9月21日 下午2:41:10
 */
public class ScriptDataStrictArray extends AbstractScriptData implements ScriptData {
	private List<ScriptData> elements = new ArrayList<ScriptData>();

	@Override
	public void read(InputStream inputStream) throws IOException {
		byte[] arraySize = new byte[4];
		inputStream.read(arraySize);
		int length = ByteUtils.byteToInt(arraySize);
		for (int i = 0; i < length; i++) {
			elements.add(readElement(inputStream));
		}
	}

	@Override
	public void write(OutputStream outputStream) throws IOException {
		outputStream.write(10);//对象类型
		outputStream.write(ByteUtils.intToByte(elements.size()));
		for (ScriptData scriptData : elements) {
			scriptData.write(outputStream);
		}
	}

	@Override
	public List<ScriptData> getValue() {
		return elements;
	}
}
