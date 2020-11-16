package com.todotask.model.user;

public class User {

	private String userId;
	private String userName;
	private String userEmail;
	private String pass;
	private String salt;

	
	public User(String user_id,String userName,String userEmail,String password,String salt) {
		this.userId = user_id;
		this.userName = userName;
		this.userEmail = userEmail;
		this.pass = password;
		this.salt = salt;
	}
	
	
	public String getUserId() {
		return userId;
	}

	
	public String getUserName() {
		return userName;
	}

	
	public String getUserEmail() {
		return userEmail;
	}

	
	public String getPass() {
		return pass;
	}

	
	public String getSalt() {
		return salt;
	}
	
	
}
