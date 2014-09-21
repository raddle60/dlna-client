package com.raddle.dlna.video.flv.tag.script;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * description: 
 * @author raddle
 * time : 2014年9月21日 下午2:41:10
 */
public class ScriptDataObject extends AbstractScriptData implements ScriptData {
	private List<ScriptDataKeyPair> attrs = new ArrayList<ScriptDataKeyPair>();

	@Override
	public void read(InputStream inputStream) throws IOException {
		while (!isEndScriptObject(inputStream) && inputStream.available() > 0) {
			ScriptDataString key = new ScriptDataString(false);
			key.read(inputStream);
			ScriptDataKeyPair attr = new ScriptDataKeyPair(key);
			attrs.add(attr);
			if (((String) key.getValue()).charAt(0) == 0 || isEndScriptObject(inputStream)) {
				return;
			}
			attr.setValue(readElement(inputStream));
		}
	}

	@Override
	public void write(OutputStream outputStream) throws IOException {
		outputStream.write(3);//对象类型
		for (ScriptDataKeyPair scriptDataKeyPair : attrs) {
			scriptDataKeyPair.getKey().write(outputStream);
			if (scriptDataKeyPair.getValue() != null) {
				scriptDataKeyPair.getValue().write(outputStream);
			}
		}
		// 写入结束标记
		writeScriptEnd(outputStream);
	}

	@Override
	public Object getValue() {
		return attrs;
	}

}
