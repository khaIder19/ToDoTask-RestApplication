package com.todotask.rest.providers.exc;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.todotask.json.error.ErrorItem;

@Provider
public class ForbiddenMapper implements ExceptionMapper<ForbiddenException>{

	@Override
	public Response toResponse(ForbiddenException arg0) {
		ErrorItem err = new ErrorItem("403", "Authorization error",arg0.getMessage());
		return Response.status(403).entity(err).build();
	}

}
