package com.raddle.dlna.event;

import java.io.File;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class DlnaEventParserTest {
	private static DlnaEventParser parser = new DlnaEventParser();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		parser.init(new File("dlna/event.js"));
	}

	@Test
	public void testIsSupportedEvent() {
		Assert.assertEquals(false, parser.isSupportedEvent("dddd"));
		Assert.assertEquals(true, parser.isSupportedEvent("优酷大屏幕"));
	}

	@Test
	public void testParseEvent() {
		Assert.assertEquals(null, parser.parseEvent("优酷大屏幕", "lastChange", "ff"));
		Assert.assertEquals("TRANSITIONING", parser.parseEvent("优酷大屏幕", "lastChange", "TRANSITIONING"));
		Assert.assertEquals("PLAYING", parser.parseEvent("优酷大屏幕", "lastChange", "PLAYING"));
		Assert.assertEquals("PAUSED_PLAYBACK", parser.parseEvent("优酷大屏幕", "lastChange", "PAUSED_PLAYBACK"));
		Assert.assertEquals("STOPPED", parser.parseEvent("优酷大屏幕", "lastChange", "STOPPED"));
	}

}
