package com.todotask.json.auth;

public class UserIdentityItem {

	private String user_id;
	private String token;
	
	public UserIdentityItem() {
		
	}
	
	public String getUser_id() {
		return user_id;
	}

	public String getApi_key() {
		return token;
	}

	public UserIdentityItem(String user_id, String token) {
		this.user_id = user_id;
		this.token = token;
	}
	
}
