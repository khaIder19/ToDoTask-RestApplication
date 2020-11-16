package com.todotask.rest.permission.authorize;

import javax.annotation.Resource;
import javax.ws.rs.container.ContainerRequestContext;
import com.todotask.rest.permission.ContextAuthorization;

public class AdminPermissionAuth implements ContextAuthorization{

	@Resource(name = "java:/adminUid")
	private String adminUid;
	
	public AdminPermissionAuth() {
		
	}
	
	
	 public AdminPermissionAuth(String adminUid) {
		this.adminUid = adminUid;
	}
	
	@Override
	public boolean authorize(String user_uid, ContainerRequestContext req) throws Exception {
		return (user_uid.equals(adminUid));
	}

	
	public void setAdminUid(String uid) {
		this.adminUid = uid;
	}
	
}
