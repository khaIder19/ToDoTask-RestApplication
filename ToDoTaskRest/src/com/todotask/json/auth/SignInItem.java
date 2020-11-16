package com.todotask.json.auth;

public class SignInItem {

	private String user_name;
	private String user_email;
	private char[] pass;
	
	public SignInItem() {		
	}
	
	public SignInItem(String user_name, String user_email,String pass) {
		super();
		this.user_name = user_name;
		this.user_email = user_email;
		this.pass = pass.toCharArray();
	}

	public String getUser_name() {
		return user_name;
	}

	public String getUser_email() {
		return user_email;
	}
	
	public String getPass() {
		return new String(pass);
	}
	
}
