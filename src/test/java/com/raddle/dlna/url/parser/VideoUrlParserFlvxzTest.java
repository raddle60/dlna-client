package com.raddle.dlna.url.parser;

import java.io.File;

import org.junit.Test;

public class VideoUrlParserFlvxzTest {

	@Test
	public void testFetchVideoUrls() {
		VideoUrlParser parser = new VideoUrlParser();
		parser.init(new File("parsers/flvxz.js"));
		VideoInfo videoInfo = parser.fetchVideoUrls("http://www.iqiyi.com/v_19rrn8zpjw.html", "1080P");
		if (videoInfo != null) {
			System.out.println(videoInfo.getName());
			System.out.println(videoInfo.getQualityName());
			System.out.println(videoInfo.getUrls().size());
		}
		parser.close();
	}

}
