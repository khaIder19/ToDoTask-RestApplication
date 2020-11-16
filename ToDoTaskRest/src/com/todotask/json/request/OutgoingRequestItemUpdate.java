package com.todotask.json.request;

import javax.validation.constraints.NotNull;
import com.todotask.json.validation.OutgoingRequestValid;
import com.todotask.model.context.ContextPermission;

@OutgoingRequestValid
public class OutgoingRequestItemUpdate {

	@NotNull
	private String to;
	
	private String permission;
	
	public OutgoingRequestItemUpdate(String to_user,ContextPermission permission) {
		this.to = to_user;
		this.permission = permission.name();
	}
	
	public OutgoingRequestItemUpdate() {
		
	}

	public String getTo() {
		return to;
	}

	public String getPermission() {
		return permission;
	}
		
}
