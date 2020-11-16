package com.todotask.rest.permission.authorize;

import javax.ws.rs.container.ContainerRequestContext;
import com.todotask.rest.permission.ContextAuthorization;

public class DefPermissionAuth implements ContextAuthorization {

	@Override
	public boolean authorize(String user_uid,ContainerRequestContext req) {
		return true;
	}

}
