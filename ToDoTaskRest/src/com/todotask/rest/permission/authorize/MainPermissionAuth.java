package com.todotask.rest.permission.authorize;

import javax.ws.rs.container.ContainerRequestContext;
import com.todotask.model.context.ContextPermission;

public class MainPermissionAuth extends ReadPermissionAuth{

	@Override
	public boolean authorize(String user_uid,ContainerRequestContext req) throws Exception {
		boolean isRead = super.authorize(user_uid, req);
		
		if(isRead) {
			return (getUserPermission(getContext(req), user_uid) == ContextPermission.MAIN);
		}else {
			return false;
		}
	}

}
