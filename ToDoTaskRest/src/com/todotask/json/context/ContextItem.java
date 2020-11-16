package com.todotask.json.context;

public class ContextItem {

	private String context_id;
	private String content;
	private String permission;
	private String creator;
	private String created_at;
	
	
	public ContextItem() {
		
	}

	public ContextItem(String context_id, String content, String permission, String creator, String created_at) {
		this.context_id = context_id;
		this.content = content;
		this.permission = permission;
		this.creator = creator;
		this.created_at = created_at;
	}

	public String getContext_id() {
		return context_id;
	}

	public String getContent() {
		return content;
	}

	public String getPermission() {
		return permission;
	}

	public String getCreator() {
		return creator;
	}

	public String getCreated_at() {
		return created_at;
	}
	
	
}
