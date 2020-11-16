package com.todotask.json.user;

public class UserItemCollection {

	private String user_id;
	private UserItem user_info;
	
	public UserItemCollection(String user_id, UserItem data) {
		super();
		this.user_id = user_id;
		this.user_info = data;
	}
	
	public String getUser_id() {
		return user_id;
	}
	
	public UserItem getData() {
		return user_info;
	}
	
	

}
