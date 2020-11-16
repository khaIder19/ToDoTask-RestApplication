package com.todotask.rest.permission;

import javax.ws.rs.container.ContainerRequestContext;

import com.todotask.model.context.Context;
import com.todotask.model.context.ContextPermission;

public interface ContextAuthorization {

	public boolean authorize(String user_uid,ContainerRequestContext req) throws Exception;
	
	public default ContextPermission getUserPermission(Context ctx,String user_uid) {
		return ctx.getUserMap().get(user_uid);
	}
	
}
