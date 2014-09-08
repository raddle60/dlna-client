/**
 * 
 */
package com.raddle.dlna.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author raddle
 *
 */
public class DurationUtilsTest {

	/**
	 * Test method for {@link com.raddle.dlna.util.DurationUtils#parseTrackNRFormat(java.lang.String)}.
	 */
	@Test
	public void testParseTrackNRFormat() {
		String trackNRFormat = DurationUtils.getTrackNRFormat(3730);
		Assert.assertEquals("01:02:10", trackNRFormat);
		int seconds = DurationUtils.parseTrackNRFormat("01:02:10");
		Assert.assertEquals(3730, seconds);
	}
}
