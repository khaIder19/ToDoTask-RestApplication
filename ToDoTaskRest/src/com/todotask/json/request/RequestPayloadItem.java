package com.todotask.json.request;

import com.todotask.model.context.ContextPermission;

public class RequestPayloadItem {

	private String ctxUid;
	private String permission;
	
	public RequestPayloadItem(String ctxUid,ContextPermission permission) {
		super();
		this.ctxUid = ctxUid;
		this.permission = permission.name();
	}
	
	public RequestPayloadItem() {
		super();
	}

	public String getCtxUid() {
		return ctxUid;
	}

	public String getPermission() {
		return permission;
	}
	
	
}
