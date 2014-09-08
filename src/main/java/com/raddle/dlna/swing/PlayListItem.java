/**
 * 
 */
package com.raddle.dlna.swing;

import com.raddle.dlna.url.parser.VideoInfo;

/**
 * @author raddle
 *
 */
public class PlayListItem {
	private VideoInfo videoInfo;
	private String videoUrl;

	public PlayListItem(VideoInfo videoInfo, String videoUrl) {
		this.videoInfo = videoInfo;
		this.videoUrl = videoUrl;
	}

	public VideoInfo getVideoInfo() {
		return videoInfo;
	}

	public void setVideoInfo(VideoInfo videoInfo) {
		this.videoInfo = videoInfo;
	}

	public String getVideoUrl() {
		return videoUrl;
	}

	public void setVideoUrl(String videoUrl) {
		this.videoUrl = videoUrl;
	}
}
