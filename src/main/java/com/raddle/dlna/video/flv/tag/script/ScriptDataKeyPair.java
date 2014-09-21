package com.raddle.dlna.video.flv.tag.script;

/**
 * description: 
 * @author raddle
 * time : 2014年9月21日 下午3:05:38
 */
public class ScriptDataKeyPair {
	private ScriptDataString key;
	private ScriptData value;

	public ScriptDataKeyPair(ScriptDataString key) {
		this.key = key;
	}

	public ScriptDataKeyPair(ScriptDataString key, ScriptData value) {
		this.key = key;
		this.value = value;
	}

	public ScriptDataString getKey() {
		return key;
	}

	public void setKey(ScriptDataString key) {
		this.key = key;
	}

	public ScriptData getValue() {
		return value;
	}

	public void setValue(ScriptData value) {
		this.value = value;
	}
}
