package com.raddle.dlna.url.parser;

import java.io.File;

import org.junit.Test;

public class VideoUrlParserFlvcdTest {

	@Test
	public void testFetchVideoUrls() {
		VideoUrlParser parser = new VideoUrlParser();
		parser.init(new File("parsers/flvcd.js"));
		VideoInfo videoInfo = parser.fetchVideoUrls("http://v.youku.com/v_show/id_XNzc1MjAyMjIw.html", "super");
		System.out.println(videoInfo.getName());
		System.out.println(videoInfo.getUrls().size());
		parser.close();
	}

}
