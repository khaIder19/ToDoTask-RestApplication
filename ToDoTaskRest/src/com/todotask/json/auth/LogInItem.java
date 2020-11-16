package com.todotask.json.auth;

public class LogInItem {

	private String user_email;
	private char[] pass;
	
	
	public LogInItem() {
		
	}
	
	public LogInItem(String user_email, String pass) {
		this.user_email = user_email;
		this.pass = pass.toCharArray();
	}
		
	public String getUser_email() {
		return user_email;
	}

	public String getPass() {
		return new String(pass);
	}
	
}
