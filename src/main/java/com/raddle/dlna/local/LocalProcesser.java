/**
 * 
 */
package com.raddle.dlna.local;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.slf4j.LoggerFactory;

/**
 * @author raddle
 *
 */
public class LocalProcesser {
	private Scriptable topScope;
	private File scriptFile;

	public void init(File scriptFile) {
		if (topScope != null) {
			throw new IllegalStateException("LocalProcesser was initialized ");
		}
		Context context = Context.getCurrentContext();
		if (context == null) {
			context = Context.enter();
		}
		topScope = context.initStandardObjects();
		ScriptableObject.putProperty(topScope, "logger", LoggerFactory.getLogger(scriptFile.getName().replace('.', '_')));
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
	 * 是否本地缓冲
	 * @param deviceName
	 * @return
	 */
	public Boolean isLocalBuffer(String videoUrl) {
		boolean hasCurrentContext = Context.getCurrentContext() != null;
		if (Context.getCurrentContext() == null) {
			// 说明起了多线程
			Context.enter();
		}
		try {
			Function isSupportedEvent = (Function) topScope.get("isLocalBuffer", topScope);
			Boolean result = (Boolean) isSupportedEvent.call(Context.getCurrentContext(), topScope, Context
					.getCurrentContext().newObject(topScope), new Object[] { videoUrl });
			return result;
		} finally {
			if (!hasCurrentContext) {
				Context.exit();
			}
		}
	}

	/**
	 * 是否本拼接
	 * @param deviceName
	 * @return
	 */
	public Boolean isLocalJoin(String videoUrl) {
		boolean hasCurrentContext = Context.getCurrentContext() != null;
		if (Context.getCurrentContext() == null) {
			// 说明起了多线程
			Context.enter();
		}
		try {
			Function isSupportedEvent = (Function) topScope.get("isLocalJoin", topScope);
			Boolean result = (Boolean) isSupportedEvent.call(Context.getCurrentContext(), topScope, Context
					.getCurrentContext().newObject(topScope), new Object[] { videoUrl });
			return result;
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
