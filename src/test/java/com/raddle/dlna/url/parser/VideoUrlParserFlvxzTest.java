package com.raddle.dlna.url.parser;

import java.io.File;

import org.junit.Test;

public class VideoUrlParserFlvxzTest {

	@Test
	public void testFetchVideoUrls() {
		VideoUrlParser parser = new VideoUrlParser();
		parser.init(new File("parsers/flvxz.js"));
		VideoInfo videoInfo = parser.fetchVideoUrls("http://tv.sohu.com/20140908/n404152615.shtml", "1080P");
		if (videoInfo != null) {
			System.out.println(videoInfo.getName());
			System.out.println(videoInfo.getUrls().size());
		}
		parser.close();
	}

}
