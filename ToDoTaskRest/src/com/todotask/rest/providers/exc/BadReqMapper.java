package com.todotask.rest.providers.exc;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.todotask.json.error.ErrorItem;

@Provider
public class BadReqMapper implements ExceptionMapper<BadRequestException>{

	@Override
	public Response toResponse(BadRequestException arg0) {
		ErrorItem err = new ErrorItem("400", "Bad Request",arg0.getMessage());
		return Response.status(400).entity(err).build();
	}

}
