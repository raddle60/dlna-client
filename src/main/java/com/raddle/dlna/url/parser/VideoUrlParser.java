/**
 * 
 */
package com.raddle.dlna.url.parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.raddle.dlna.http.HttpHelper;
import com.raddle.dlna.util.KeyValue;

/**
 * @author raddle
 *
 */
public class VideoUrlParser {
	private static Logger logger = LoggerFactory.getLogger(VideoUrlParser.class);
	private String name;
	private List<KeyValue<String, String>> videoQualitys;
	private Scriptable topScope;
	private File scriptFile;

	public static List<VideoUrlParser> getVideoUrlParser(File parserDir) {
		List<VideoUrlParser> list = new ArrayList<VideoUrlParser>();
		if (parserDir.isDirectory()) {
			Collection<File> listFiles = FileUtils.listFiles(parserDir, new String[] { "js" }, true);
			for (File file : listFiles) {
				VideoUrlParser videoUrlParser = new VideoUrlParser();
				try {
					videoUrlParser.init(file);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				} finally {
					videoUrlParser.close();
				}
				list.add(videoUrlParser);
			}
		} else {
			logger.error("dir[{}] not exist or not a dir", parserDir);
		}
		return list;
	}

	public void init(File scriptFile) {
		if (topScope != null) {
			throw new IllegalStateException("VideoUrlParser was initialized ");
		}
		Context context = Context.getCurrentContext();
		if (context == null) {
			context = Context.enter();
		}
		topScope = context.initStandardObjects();
		ScriptableObject.putProperty(topScope, "logger", LoggerFactory.getLogger(scriptFile.getName()));
		ScriptableObject.putProperty(topScope, "httpclient", new HttpHelper());
		ScriptableObject.putProperty(topScope, "videoUrlParser", this);
		this.scriptFile = scriptFile;
		try {
			context.evaluateString(topScope, FileUtils.readFileToString(scriptFile, "utf-8"),
					"<" + scriptFile.getName() + ">", 1, null);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		// 获取名称
		NativeObject parserInfo = (NativeObject) topScope.get("parserInfo", topScope);
		name = (String) parserInfo.get("name", topScope);
		// 获取清晰度
		NativeArray qualitys = (NativeArray) parserInfo.get("qualitys", topScope);
		videoQualitys = new ArrayList<KeyValue<String, String>>();
		for (Object quality : qualitys) {
			NativeObject qualityObj = (NativeObject) quality;
			videoQualitys.add(new KeyValue<String, String>((String) qualityObj.get("key", topScope),
					(String) qualityObj.get("value", topScope)));
		}
	}

	public void close() {
		if (Context.getCurrentContext() != null) {
			Context.exit();
		}
	}

	/**
	 * 解析url地址，返回视频地址
	 * @param url 视频地址或网页地址
	 * @param videoQuality 视频画质
	 * @return
	 */
	public VideoInfo fetchVideoUrls(String url, String videoQuality) {
		boolean hasCurrentContext = Context.getCurrentContext() != null;
		if (Context.getCurrentContext() == null) {
			// 说明起了多线程
			Context.enter();
		}
		try {
			Function fetchVideoUrls = (Function) topScope.get("fetchVideoUrls", topScope);
			NativeObject result = (NativeObject) fetchVideoUrls.call(Context.getCurrentContext(), topScope, Context
					.getCurrentContext().newObject(topScope), new Object[] { url, videoQuality });
			if (result != null) {
				VideoInfo videoInfo = new VideoInfo();
				videoInfo.setName((String) result.get("name", topScope));
				NativeArray urlsObject = (NativeArray) result.get("urls", topScope);
				List<String> urls = new ArrayList<String>();
				for (Object string : urlsObject) {
					urls.add(string + "");
				}
				videoInfo.setUrls(urls);
				return videoInfo;
			}
			return null;
		} finally {
			if (!hasCurrentContext) {
				Context.exit();
			}
		}
	}

	public KeyValue<String, String> getVideoQualityByValue(String value) {
		if (videoQualitys != null) {
			for (KeyValue<String, String> keyValue : videoQualitys) {
				if (keyValue.getValue().equals(value)) {
					return keyValue;
				}
			}
		}
		return null;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<KeyValue<String, String>> getVideoQualitys() {
		return videoQualitys;
	}

	public void setVideoQualitys(List<KeyValue<String, String>> videoQualitys) {
		this.videoQualitys = videoQualitys;
	}

	public File getScriptFile() {
		return scriptFile;
	}
}
