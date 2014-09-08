package com.raddle.dlna.util;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class RegexUtilsTest {

	@Test
	public void testSearchStringString() {
		List<MatchedLine> list = RegexUtils.search("(a)", "bbabaaab");
		Assert.assertEquals(4, list.size());
		Assert.assertEquals(1, list.get(0).getGroupCount());
		Assert.assertEquals("a", list.get(0).getGroup(1));
	}

}
