package com.raddle.dlna.util;

import java.text.ParseException;
import java.util.Date;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;

public class DurationUtils {
	public static String getTrackNRFormat(int second) {
		return DurationFormatUtils.formatDuration(second * 1000, "HH:mm:ss");
	}

	public static int parseTrackNRFormat(String trackNRDuration) {
		String dateStr = DateFormatUtils.format(new Date(), "yyyy-MM-dd");
		String dateZeroTimeStr = dateStr + " 00:00:00";
		String dateTrackTimeStr = dateStr + " " + trackNRDuration;
		try {
			Date dateZeroTime = DateUtils.parseDateStrictly(dateZeroTimeStr, "yyyy-MM-dd HH:mm:ss");
			Date dateTrackTime = DateUtils.parseDateStrictly(dateTrackTimeStr, "yyyy-MM-dd HH:mm:ss");
			return (int) (dateTrackTime.getTime() - dateZeroTime.getTime()) / 1000;
		} catch (ParseException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}
