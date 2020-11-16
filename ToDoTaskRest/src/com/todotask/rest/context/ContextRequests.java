package com.todotask.rest.context;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.CDI;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.todotask.rest.permission.Authorized;
import com.todotask.rest.permission.ContextAuthorized;
import com.todotask.rest.permission.authorize.MainPermissionAuth;
import com.todotask.rest.requests.UserRequests;

@RequestScoped
public class ContextRequests {

	
	@Context
	private ResourceContext resCtx;
	
	public ContextRequests() {
		super();
	}

	public ContextRequests(ResourceContext resCtx) {
		super();
		this.resCtx = resCtx;
	}

	
	
	@GET
	@ContextAuthorized(MainPermissionAuth.class)
	@Authorized
	@Produces("application/json")
	public Response getRequests(@HeaderParam("user_uid")String user_uid,
			@QueryParam("to")String to,@QueryParam("perm")String perm,@QueryParam("state")String state) throws JsonProcessingException {
		
		return resCtx.initResource(CDI.current().select(UserRequests.class)).get().getRequests(to, state,user_uid);
	}
		
}
