package com.raddle.dlna.video.flv.tag.script;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * description: 
 * @author raddle
 * time : 2014年9月21日 下午3:07:25
 */
public abstract class AbstractScriptData implements ScriptData {
	private static final Logger logger = LoggerFactory.getLogger(AbstractScriptData.class);

	/**
	 * 009就是scriptObject的结束标记
	 * @param inputStrieam
	 * @throws IOException
	 */
	public static boolean isEndScriptObject(InputStream inputStrieam) throws IOException {
		byte[] endScript = new byte[3];
		inputStrieam.mark(-1);
		inputStrieam.read(endScript);
		if (endScript[0] == 0 && endScript[1] == 0 && endScript[2] == 9) {
			return true;
		}
		inputStrieam.reset();
		return false;
	}

	public static ScriptData readElement(InputStream inputStrieam) throws IOException {
		byte[] type = new byte[1];
		if (inputStrieam.read(type) == -1) {
			return null;
		}
		ScriptData data = null;
		switch (type[0]) {
		case 0: // Number - 8
			data = new ScriptDataDouble();
			data.read(inputStrieam);
			return data;
		case 1: // Boolean - 1
			data = new ScriptDataBoolean();
			data.read(inputStrieam);
			return data;
		case 2: // String - 2+n
			data = new ScriptDataString();
			data.read(inputStrieam);
			return data;
		case 3: // Object
			data = new ScriptDataObject();
			data.read(inputStrieam);
			return data;
		case 4: // MovieClip
			data = new ScriptDataMovieClip();
			data.read(inputStrieam);
			return data;
		case 5: // Null
			break;
		case 6: // Undefined
			break;
		case 7: // Reference - 2
			data = new ScriptDataShort();
			data.read(inputStrieam);
			return data;
		case 8: // ECMA array
			data = new ScriptDataArray();
			data.read(inputStrieam);
			return data;
		case 10: // Strict array
			data = new ScriptDataStrictArray();
			data.read(inputStrieam);
			return data;
		case 11: // Date - 8+2
			data = new ScriptDataDate();
			data.read(inputStrieam);
			return data;
		case 12: // Long string - 4+n
			data = new ScriptDataLongString();
			data.read(inputStrieam);
			return data;
		}
		logger.warn("unkown script data type : " + type[0]);
		return null;
	}

	public static void writeScriptEnd(OutputStream outputStream) throws IOException {
		byte[] end = new byte[3];
		end[2] = 9;
		outputStream.write(end);
	}

	@SuppressWarnings("unchecked")
	public static void printScriptData(ScriptData scriptData, int indent) {
		String indetStr = StringUtils.leftPad("", indent * 4, " ");
		if (scriptData == null || scriptData.getValue() == null) {
			logger.info(indetStr + "null");
		}
		if (scriptData instanceof ScriptDataStrictArray) {
			List<ScriptData> value = (List<ScriptData>) scriptData.getValue();
			for (ScriptData d : value) {
				printScriptData(d, indent);
			}
		} else if (scriptData instanceof ScriptDataArray || scriptData instanceof ScriptDataObject) {
			List<ScriptDataKeyPair> value = (List<ScriptDataKeyPair>) scriptData.getValue();
			for (ScriptDataKeyPair d : value) {
				printScriptData(d.getKey(), indent);
				printScriptData(d.getValue(), indent + 1);
			}
		} else {
			logger.info(indetStr + scriptData.getValue());
		}
	}
}
