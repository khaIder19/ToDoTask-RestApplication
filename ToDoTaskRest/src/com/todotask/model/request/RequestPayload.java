package com.todotask.model.request;

public class RequestPayload {

	private String type;
	private String data;
	
	public RequestPayload(RequestType type,String data) {
		this.type = type.name();
		this.data = data;
	}

	public String getType() {
		return type;
	}

	public String getData() {
		return data;
	}
	
	
	
}
