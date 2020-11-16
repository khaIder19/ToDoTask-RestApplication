package com.todotask.json.request;

import com.todotask.json.context.UserContextItem;

public class DelegationItem {

	private UserContextItem from;
	private String to;
	private String at;
	
	public DelegationItem() {
		super();
	}
	
	public DelegationItem(UserContextItem from, String to, String at) {
		super();
		this.from = from;
		this.to = to;
		this.at = at;
	}
	
	public UserContextItem getFrom() {
		return from;
	}
	public String getTo() {
		return to;
	}
	public String getAt() {
		return at;
	}
	

}
