package com.raddle.dlna.util;

import java.util.ArrayList;
import java.util.List;

public class MatchedLine {
	private String matchedString;
	private List<String> groups;

	public MatchedLine(String matchedString, int groupCount) {
		this.matchedString = matchedString;
		this.groups = new ArrayList<String>(groupCount);
		for (int i = 0; i < groupCount; i++) {
			groups.add(null);
		}
	}

	public int getGroupCount() {
		return groups.size();
	}

	/**
	 * i start from 1
	 * @param i
	 * @param group
	 */
	public void setGroup(int i, String group) {
		groups.set(i - 1, group);
	}

	/**
	 * i start from 1
	 * @param i
	 * @return
	 */
	public String getGroup(int i) {
		return groups.get(i - 1);
	}

	public String getMatchedString() {
		return matchedString;
	}

	public void setMatchedString(String matchedString) {
		this.matchedString = matchedString;
	}
}
