package com.todotask.rest.permission.authorize;

import javax.ws.rs.container.ContainerRequestContext;


public class DelegatedPermissionAuth extends TaskPermissionAuth {
	
	@Override
	public boolean authorize(String user_uid,ContainerRequestContext req) throws Exception {	
		if(super.authorize(user_uid,req) && de.getTo().equals(user_uid)) {
				return true;
		}else {
			return false;
		}
	}
	
}
