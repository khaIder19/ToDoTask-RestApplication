package com.todotask.json.error;

public class ErrorItem {

	private String status;
	private String type;
	private String message;

	public ErrorItem(String status, String type, String message) {
		super();
		this.status = status;
		this.type = type;
		this.message = message;
	}
	
	public ErrorItem() {
		
	}
	
	public String getStatus() {
		return status;
	}

	public String getType() {
		return type;
	}

	public String getMessage() {
		return message;
	}
		
}
