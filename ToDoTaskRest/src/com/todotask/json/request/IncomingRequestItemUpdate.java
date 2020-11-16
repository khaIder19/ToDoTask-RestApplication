package com.todotask.json.request;

import javax.validation.constraints.NotNull;

import com.todotask.json.validation.RequestItemUpdateValid;

@RequestItemUpdateValid
public class IncomingRequestItemUpdate {

	@NotNull
	private String status;
	
	public IncomingRequestItemUpdate(String status) {
		this.status = status;
	}
	
	public IncomingRequestItemUpdate() {
		
	}
	
	public String getStatus() {
		return this.status;
	}
	
}
