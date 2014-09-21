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
public class ScriptDataArray extends AbstractScriptData implements ScriptData {
	private List<ScriptDataKeyPair> attrs = new ArrayList<ScriptDataKeyPair>();

	@Override
	public void read(InputStream inputStream) throws IOException {
		byte[] arraySize = new byte[4];
		inputStream.read(arraySize);
		int length = ByteUtils.byteToInt(arraySize);
		for (int i = 0; i < length; i++) {
			ScriptDataString key = new ScriptDataString(false);
			key.read(inputStream);
			ScriptDataKeyPair attr = new ScriptDataKeyPair(key);
			attrs.add(attr);
			attr.setValue(readElement(inputStream));
		}
	}

	@Override
	public void write(OutputStream outputStream) throws IOException {
		outputStream.write(8);//对象类型
		outputStream.write(ByteUtils.intToByte(attrs.size()));
		for (ScriptDataKeyPair scriptDataKeyPair : attrs) {
			scriptDataKeyPair.getKey().write(outputStream);
			scriptDataKeyPair.getValue().write(outputStream);
		}
	}

	@Override
	public Object getValue() {
		return attrs;
	}

}
