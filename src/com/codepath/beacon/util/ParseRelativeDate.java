package com.codepath.beacon.util;

import java.text.SimpleDateFormat;
import java.util.Locale;

import android.net.ParseException;
import android.text.format.DateUtils;


public class ParseRelativeDate {
	//getRelativeTimeAgo("Mon Apr 01 21:16:23 +0000 2014");
	public String getRelativeTimeAgo(String rawJsonDate) throws java.text.ParseException {
		String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
		SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
		sf.setLenient(true);

		String relativeDate = "";
		try {
			long dateMillis = sf.parse(rawJsonDate).getTime();
			relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
					System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return relativeDate;
	}
}
