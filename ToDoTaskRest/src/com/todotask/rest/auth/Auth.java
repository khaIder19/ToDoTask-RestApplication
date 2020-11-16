package com.todotask.rest.auth;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.CDI;
import javax.ws.rs.Path;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;

@RequestScoped
@Path("/auth")
public class Auth {

	@Context
	private ResourceContext rsContext;
	
	@Path("/sign-in")
	public SignIn singIn() {
		return rsContext.initResource(CDI.current().select(SignIn.class).get());
	}
	
	
	@Path("/log-in")
	public LogIn logIn() {
		return rsContext.initResource(CDI.current().select(LogIn.class).get());
	}
	
}
