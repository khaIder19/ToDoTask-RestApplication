package com.todotask.json.task;

import javax.validation.constraints.NotNull;

import com.todotask.json.validation.RangeItemValid;

@RangeItemValid
public class RangeItem {
	
	private String start;
	
	private String end;
	
	public RangeItem(@NotNull(message = "start range can't be null") String start,@NotNull(message = "end range can't be null") String end) {
		this.start = start;
		this.end = end;
	}
	
	public RangeItem() {
		
	}
	
	public String getStart() {
		return start;
	}
	
	public String getEnd() {
		return end;
	}
}
