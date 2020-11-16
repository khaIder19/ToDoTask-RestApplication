package com.todotask.json.context;

public class ContextItemCollection {

	private String context_id;
	private String content;
	
	public ContextItemCollection() {
		
	}
	
	public ContextItemCollection(String context_id,String content) {
		this.context_id = context_id;
		this.content = content;
	}

	public String getContext_id() {
		return context_id;
	}

	public String getContent() {
		return content;
	}
	
}
