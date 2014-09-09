/**
 * 
 */
package com.raddle.dlna.url.parser;

import java.util.List;

/**
 * @author raddle
 *
 */
public class VideoInfo {
	private String name;
	private String qualityName;
	private List<String> urls;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getUrls() {
		return urls;
	}

	public void setUrls(List<String> urls) {
		this.urls = urls;
	}

	public String getQualityName() {
		return qualityName;
	}

	public void setQualityName(String qualityName) {
		this.qualityName = qualityName;
	}
}
