package com.todotask.json.request;

import java.util.List;

import com.todotask.json.KeyValueItem;
import com.todotask.json.user.UserIdentityItemCollection;
import com.todotask.model.request.RequestState;

public class RequestItem {

	private String request_id;
	private UserIdentityItemCollection from;
	private List<KeyValueItem> data;
	private String state;
	private String created_at;
	
	public RequestItem() {
		super();
	}

	public RequestItem(String request_id, UserIdentityItemCollection from,List<KeyValueItem> data,RequestState state,String created_at) {
		super();
		this.request_id = request_id;
		this.from = from;
		this.data = data;
		this.state = state.name();
		this.created_at = created_at;
	}

	public String getRequest_id() {
		return request_id;
	}

	public UserIdentityItemCollection getFrom() {
		return from;
	}

	public List<KeyValueItem> getData() {
		return data;
	}

	public String getState() {
		return state;
	}
	
	public String getSent_at() {
		return created_at;
	}
	
	
}
