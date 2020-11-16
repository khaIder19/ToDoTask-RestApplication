package com.todotask.json.context;

import com.todotask.json.user.UserItem;

public class UserContextDataItem {

	private UserItem user;
	private UserContextItem data;
	
	public UserContextDataItem(UserItem user, UserContextItem data) {
		super();
		this.user = user;
		this.data = data;
	}
	
	public UserItem getUser() {
		return user;
	}
	
	public UserContextItem getData() {
		return data;
	}
	
	
}
