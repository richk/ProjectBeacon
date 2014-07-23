package com.codepath.beacon.util;

import java.text.SimpleDateFormat;
import java.util.Locale;

import android.text.format.DateUtils;

import com.parse.ParseException;

public class ParseRelativeDate {
	//getRelativeTimeAgo("Mon Apr 01 21:16:23 +0000 2014");
	public String getRelativeTimeAgo(String rawJsonDate) throws java.text.ParseException {
		String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
		SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
		sf.setLenient(true);

		String relativeDate = "";
		long dateMillis = sf.parse(rawJsonDate).getTime();
		relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
				System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();

		return relativeDate;
	}
}
