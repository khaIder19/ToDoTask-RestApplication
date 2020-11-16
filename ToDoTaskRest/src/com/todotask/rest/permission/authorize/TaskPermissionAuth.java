package com.todotask.rest.permission.authorize;

import javax.ws.rs.container.ContainerRequestContext;
import com.todotask.model.context.DelegationEntry;

public class TaskPermissionAuth extends WritePermissionAuth{

	protected DelegationEntry de;
	
	@Override
	public boolean authorize(String user_uid,ContainerRequestContext req) throws Exception {
		boolean isWrite = super.authorize(user_uid,req);
		
		boolean result = false;
		
		if(isWrite) {
			String task_uid = req.getUriInfo().getPathParameters().get("t-id").get(0);
			de = getContext(req).getTaskDataById(task_uid);
			result = de.getFrom().equals(user_uid);		
		}
		return result;		
	}
	
}
