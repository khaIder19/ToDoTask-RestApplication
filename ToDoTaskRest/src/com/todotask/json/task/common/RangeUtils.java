package com.todotask.json.task.common;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import com.todotask.json.task.RangeItem;
import com.core.model.TimeRange;

public class RangeUtils {

	public static final String DEF_UNDEF_TIME = "0000-00-00T00:00:00Z";
	
	public static RangeItem getRange(long start,long end) {
		Instant endInst = null;
		Instant startInst = null;
		if(end == Long.MAX_VALUE | end > Instant.MAX.getEpochSecond()) {
			endInst = Instant.MAX;
		}else {
			endInst = Instant.ofEpochSecond(end);
		}
		if(start == Long.MIN_VALUE | start < Instant.MIN.getEpochSecond()) {
			startInst = Instant.MIN;
		}else {
			startInst = Instant.ofEpochSecond(start);
		}
		return new RangeItem(startInst.toString(),endInst.toString());
	}
	
	public static TimeRange fromRangeItem(RangeItem range) {
		RangeItem defRange = setDefValues(range);
		return new TimeRange(Instant.parse(defRange.getStart()).getEpochSecond(),Instant.parse(defRange.getEnd()).getEpochSecond());
	}
		
	public static RangeItem setDefValues(RangeItem range) {
		return new RangeItem(setStartDefValue(range.getStart()), setEndDefValue(range.getEnd()));
	}
	
	public static String setStartDefValue(String startval) {
		String start = startval;
		if(isDefUndefinedTime(start)) {
			start = Instant.MIN.toString();
		}
		return start;
	}
	
	public static String setEndDefValue(String endval) {
		String end = endval;
		if(isDefUndefinedTime(end)) {
			end = Instant.MAX.toString();
		}
		return end;
	}
	
	public static RangeItem truncateRange(RangeItem item) {
		ZoneOffset z = ZoneOffset.of("Z");
		String start = item.getStart();
		String end = item.getEnd();
		if(!isDefUndefinedTime(item.getStart())) {
			start = LocalDateTime.ofInstant(Instant.parse(item.getStart()),z).truncatedTo(ChronoUnit.DAYS).toInstant(z).toString();	
		}
		if(!isDefUndefinedTime(item.getEnd())) {
			end = LocalDateTime.ofInstant(Instant.parse(item.getEnd()),z).truncatedTo(ChronoUnit.DAYS).toInstant(z).toString();
		}
		return new RangeItem(start,end);
	}
	
	
	public static boolean isDefUndefinedTime(String param) {
		return (param.contentEquals(DEF_UNDEF_TIME) || param.length() == 0);
	}
	
	public static RangeItem getDefString(RangeItem param) {
		String start = param.getStart();
		String end = param.getEnd();
		if(Instant.MIN.toString().equals(param.getStart())) {
			start = DEF_UNDEF_TIME;
		}
		if(Instant.MAX.toString().equals(param.getEnd())) {
			end = DEF_UNDEF_TIME;
		}
		return new RangeItem(start,end);
	}
	
	public static boolean isBeforeNow(Instant i) {
		return i.isBefore(Instant.now().minus(5000,ChronoUnit.MILLIS));
	}
	
	
}
