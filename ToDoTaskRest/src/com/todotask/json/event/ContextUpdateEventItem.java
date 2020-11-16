package com.todotask.json.event;

public class ContextUpdateEventItem {

	private String context_id;
	private String update_type;
	
	public ContextUpdateEventItem(String context_id, String update_type) {
		super();
		this.context_id = context_id;
		this.update_type = update_type;
	}
	
	public ContextUpdateEventItem() {
		super();
	}
	
	public String getContext_id() {
		return context_id;
	}
	
	public String getUpdate_type() {
		return update_type;
	}
		
}
