/**
 * 
 */
package com.raddle.dlna.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author raddle
 *
 */
public class RegexUtils {
	/**
	 * 搜索匹配的行
	 * @param regex
	 * @param content
	 * @return
	 */
	public static List<MatchedLine> search(String regex, String content) {
		return search(Pattern.compile(regex), content);
	}

	/**
	 * 搜索匹配的行
	 * @param regex
	 * @param content
	 * @return
	 */
	public static List<MatchedLine> search(Pattern regex, String content) {
		List<MatchedLine> matchedLines = new ArrayList<MatchedLine>();
		if (content != null) {
			Matcher matcher = regex.matcher(content);
			while (matcher.find()) {
				MatchedLine line = new MatchedLine(matcher.group(), matcher.groupCount());
				for (int i = 1; i <= matcher.groupCount(); i++) {
					String group = matcher.group(i);
					line.setGroup(i, group);
				}
				matchedLines.add(line);
			}
		}
		return matchedLines;
	}
}
