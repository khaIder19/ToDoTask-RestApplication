package com.todotask.json.context;

import com.todotask.model.context.ContextPermission;

public class UserContextItem {

	private UserContextPair user_data;
	private String permission;
	
	public UserContextItem() {
		super();
	}
	
	public UserContextItem(UserContextPair user,ContextPermission permission) {
		super();
		this.user_data = user;
		this.permission = permission.name();
	}

	public UserContextPair getUser() {
		return user_data;
	}

	public String getPermission() {
		return permission;
	}

}
