package com.todotask.model.request;

public class RequestEntity {

	private String request_uid;
	private String from_user_uid;
	private String to_user_uid;
	private RequestState state;
	private String created_at;
	private RequestPayload data;
	
	public RequestEntity(String uid,String from,String to,RequestState state,RequestPayload data) {
		this.request_uid = uid;
		this.from_user_uid = from;
		this.to_user_uid = to;
		this.state = state;
		this.data = data;
	}
	
	public void setCreationTime(String timestamp) {
		this.created_at = timestamp;
	}
	
	public void setState(RequestState state) {
		this.state = state;
	}

	public String getRequest_uid() {
		return request_uid;
	}

	public String getFrom_user_uid() {
		return from_user_uid;
	}

	public String getTo_user_uid() {
		return to_user_uid;
	}

	public RequestState getState() {
		return state;
	}

	public String getCreated_at() {
		return created_at;
	}

	public RequestPayload getData() {
		return data;
	}
	
	
}
