/**
 * 
 */
package com.raddle.dlna.event;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raddle.dlna.util.KeyValue;

/**
 * @author raddle
 *
 */
public class DlnaEventParser {
	private static Logger logger = LoggerFactory.getLogger(DlnaEventParser.class);
	private String name;
	private List<KeyValue<String, String>> videoQualitys;
	private Scriptable topScope;
	private File scriptFile;

	public void init(File scriptFile) {
		if (topScope != null) {
			throw new IllegalStateException("DlnaEventParser was initialized ");
		}
		Context context = Context.getCurrentContext();
		if (context == null) {
			context = Context.enter();
		}
		topScope = context.initStandardObjects();
		ScriptableObject.putProperty(topScope, "logger", LoggerFactory.getLogger(scriptFile.getName()));
		ScriptableObject.putProperty(topScope, "dlnaEventParser", this);
		this.scriptFile = scriptFile;
		try {
			context.evaluateString(topScope, FileUtils.readFileToString(scriptFile, "utf-8"),
					"<" + scriptFile.getName() + ">", 1, null);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public void close() {
		if (Context.getCurrentContext() != null) {
			Context.exit();
		}
	}

	/**
	 * 是否支持dlna事件
	 * @param deviceName
	 * @return
	 */
	public boolean isSupportedEvent(String deviceName) {
		boolean hasCurrentContext = Context.getCurrentContext() != null;
		if (Context.getCurrentContext() == null) {
			// 说明起了多线程
			Context.enter();
		}
		try {
			Function isSupportedEvent = (Function) topScope.get("isSupportedEvent", topScope);
			Boolean result = (Boolean) isSupportedEvent.call(Context.getCurrentContext(), topScope, Context
					.getCurrentContext().newObject(topScope), new Object[] { deviceName });
			if (result != null) {
				return result;
			}
			return false;
		} finally {
			if (!hasCurrentContext) {
				Context.exit();
			}
		}
	}

	public String parseEvent(String deviceName, String varName, String value) {
		boolean hasCurrentContext = Context.getCurrentContext() != null;
		if (Context.getCurrentContext() == null) {
			// 说明起了多线程
			Context.enter();
		}
		try {
			Function parseEvent = (Function) topScope.get("parseEvent", topScope);
			NativeObject result = (NativeObject) parseEvent.call(Context.getCurrentContext(), topScope, Context
					.getCurrentContext().newObject(topScope), new Object[] { deviceName, varName, value });
			if (result != null) {
				return (String) result.get("eventType", topScope);
			}
			return null;
		} finally {
			if (!hasCurrentContext) {
				Context.exit();
			}
		}
	}

	public File getScriptFile() {
		return scriptFile;
	}
}
