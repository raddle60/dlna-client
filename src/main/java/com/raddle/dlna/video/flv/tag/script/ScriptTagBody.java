package com.raddle.dlna.video.flv.tag.script;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.raddle.dlna.util.ByteUtils;
import com.raddle.dlna.video.flv.tag.TagHeader;

/**
 * description: 
 * @author raddle
 * time : 2014年9月21日 下午5:29:44
 */
public class ScriptTagBody {
	private List<ScriptData> scriptDatas = new ArrayList<ScriptData>();
	private int tagLength;
	private int dataLength;

	public static ScriptTagBody readScriptTagBody(TagHeader tagHeader, InputStream inputStream) throws IOException {
		if (tagHeader.getTagType() != 18) {
			throw new IllegalStateException("is not a script tag");
		}
		ScriptTagBody body = new ScriptTagBody();
		ByteArrayOutputStream bos = new ByteArrayOutputStream(tagHeader.getDataLength());
		long read = IOUtils.copyLarge(inputStream, bos, 0, tagHeader.getDataLength());
		if (read != tagHeader.getDataLength()) {
			return null;
		}
		body.setDataLength(tagHeader.getDataLength());
		ByteArrayInputStream tagDataInputStrieam = new ByteArrayInputStream(bos.toByteArray());
		List<ScriptData> list = new ArrayList<ScriptData>();
		while (tagDataInputStrieam.available() > 0) {
			// 最后3个字节是009结束标记
			if (AbstractScriptData.isEndScriptObject(tagDataInputStrieam)) {
				continue;
			}
			ScriptData readElement = AbstractScriptData.readElement(tagDataInputStrieam);
			if (readElement != null) {
				list.add(readElement);
			}
		}
		body.setScriptDatas(list);
		byte[] tagLengthBytes = new byte[4];
		inputStream.read(tagLengthBytes);
		body.setTagLength(ByteUtils.byteToInt(tagLengthBytes));
		return body;
	}

	@SuppressWarnings("unchecked")
	public List<ScriptDataDouble> getFilepositions() {
		List<ScriptDataKeyPair> scriptData = (List<ScriptDataKeyPair>) getScriptDatas().get(1).getValue();
		for (ScriptDataKeyPair scriptDataKeyPair : scriptData) {
			if (scriptDataKeyPair.getKey().getValue().equals("keyframes")) {
				List<ScriptDataKeyPair> keyframes = (List<ScriptDataKeyPair>) scriptDataKeyPair.getValue().getValue();
				for (ScriptDataKeyPair scriptDataKeyPair2 : keyframes) {
					if (scriptDataKeyPair2.getKey().getValue().equals("filepositions")) {
						return (List<ScriptDataDouble>) scriptDataKeyPair2.getValue().getValue();
					}
				}
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public List<ScriptDataDouble> getTimes() {
		List<ScriptDataKeyPair> scriptData = (List<ScriptDataKeyPair>) getScriptDatas().get(1).getValue();
		for (ScriptDataKeyPair scriptDataKeyPair : scriptData) {
			if (scriptDataKeyPair.getKey().getValue().equals("keyframes")) {
				List<ScriptDataKeyPair> keyframes = (List<ScriptDataKeyPair>) scriptDataKeyPair.getValue().getValue();
				for (ScriptDataKeyPair scriptDataKeyPair2 : keyframes) {
					if (scriptDataKeyPair2.getKey().getValue().equals("times")) {
						return (List<ScriptDataDouble>) scriptDataKeyPair2.getValue().getValue();
					}
				}
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public ScriptDataDouble getDuration() {
		List<ScriptDataKeyPair> scriptData = (List<ScriptDataKeyPair>) getScriptDatas().get(1).getValue();
		for (ScriptDataKeyPair scriptDataKeyPair : scriptData) {
			if (scriptDataKeyPair.getKey().getValue().equals("duration")) {
				return (ScriptDataDouble) scriptDataKeyPair.getValue();
			}
		}
		return null;
	}

	public void writeScriptTagBody(OutputStream outputStream) throws IOException {
		// 需要计算长度，所以要缓存
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		for (ScriptData scriptData : scriptDatas) {
			scriptData.write(bos);
		}
		// 写入结束标记,刚好相等就不需要结束标记
		if (dataLength - bos.size() == 3) {
			AbstractScriptData.writeScriptEnd(bos);
		}
		// 计算整个tag长度
		tagLength = bos.size() + 11;
		bos.write(ByteUtils.intToByte(tagLength));
		outputStream.write(bos.toByteArray());
	}

	public List<ScriptData> getScriptDatas() {
		return scriptDatas;
	}

	public void setScriptDatas(List<ScriptData> scriptDatas) {
		this.scriptDatas = scriptDatas;
	}

	public int getTagLength() {
		return tagLength;
	}

	public void setTagLength(int tagLength) {
		this.tagLength = tagLength;
	}

	public int getDataLength() {
		return dataLength;
	}

	public void setDataLength(int dataLength) {
		this.dataLength = dataLength;
	}
}
