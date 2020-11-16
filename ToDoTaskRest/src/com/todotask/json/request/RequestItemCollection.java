package com.todotask.json.request;

public class RequestItemCollection {

	private String request_id;
	
	private String from_user;
	
	private String to_user;
	
	
	public RequestItemCollection() {
		super();
	}
	
	public RequestItemCollection(String request_id, String from_user, String to_user) {
		super();
		this.request_id = request_id;
		this.from_user = from_user;
		this.to_user = to_user;
	}

	public String getRequest_id() {
		return request_id;
	}

	public String getFrom_user() {
		return from_user;
	}

	public String getTo_user() {
		return to_user;
	}
	
}
