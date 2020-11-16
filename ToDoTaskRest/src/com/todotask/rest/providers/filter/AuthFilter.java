package com.todotask.rest.providers.filter;

import java.io.IOException;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import org.apache.log4j.Logger;
import com.todotask.persistence.dao.TokenDAO;
import com.todotask.rest.auth.SecurityUtils;

@Priority(1)
@Provider
public class AuthFilter implements ContainerRequestFilter{

	private static Logger log = Logger.getLogger(AuthFilter.class);
	
	@Context
	private UriInfo uri;
	
	@Inject
	private TokenDAO tokens;
	
	@Override
	public void filter(ContainerRequestContext arg0) throws IOException {
		if(!uri.getPath().contains("/auth")) {

		String header = arg0.getHeaderString("Authorization");	
			
		if(header == null)
			throw new BadRequestException("Authorization credentials missing");
			
		String[] parts = header.split("_");
		
		if(parts.length == 1 || parts.length > 2)
			throw new BadRequestException();
		
		String inputToken = SecurityUtils.getInstance().decrypt(parts[1]);	
		String userToken = SecurityUtils.getInstance().decrypt(tokens.getById(parts[0]));
		
		if(userToken == null || !userToken.equals(inputToken)) {	
			
			log.info("User log in failed " + "[user:"+parts[0]+"]");	
			throw new ForbiddenException("Invalid api-key");		
		}
		arg0.getHeaders().add("user_uid",parts[0]);
	}
	}
}
