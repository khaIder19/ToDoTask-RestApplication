package com.todotask.rest.permission.authorize;

import javax.ws.rs.container.ContainerRequestContext;
import com.todotask.model.context.ContextPermission;


public class WritePermissionAuth extends ReadPermissionAuth{

	
	@Override
	public boolean authorize(String user_uid,ContainerRequestContext req) throws Exception {
		boolean isRead = super.authorize(user_uid, req);
		boolean result = false;
		if(isRead) {
			ContextPermission userPerm = getUserPermission(getContext(req), user_uid);
			result = (userPerm == ContextPermission.WRITE || userPerm == ContextPermission.MAIN);
		}
		return result;
	}
	
}
