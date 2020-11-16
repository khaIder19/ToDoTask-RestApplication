package com.todotask.json.user;

public class UserIdentityItemCollection {

	private String user_id;
	private String user_email;
	
	public UserIdentityItemCollection(String user_id, String user_email) {
		super();
		this.user_id = user_id;
		this.user_email = user_email;
	}
	
	public UserIdentityItemCollection() {
		super();
	}
	
	public String getUser_id() {
		return user_id;
	}
	
	public String getUser_email() {
		return user_email;
	}
		
}
