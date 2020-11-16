package com.todotask.json.context;

public class UserContextPair {

	private String user_id;
	private String ctx_id;
	
	public UserContextPair(String user_id, String ctx_id) {
		super();
		this.user_id = user_id;
		this.ctx_id = ctx_id;
	}
	
	public String getUser_id() {
		return user_id;
	}
	
	public String getCtx_id() {
		return ctx_id;
	}
	
}
