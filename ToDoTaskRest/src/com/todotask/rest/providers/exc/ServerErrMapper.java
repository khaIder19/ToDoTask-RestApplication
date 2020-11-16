package com.todotask.rest.providers.exc;

import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import com.todotask.json.error.ErrorItem;

@Provider
public class ServerErrMapper implements ExceptionMapper<ServerErrorException> {

	@Override
	public Response toResponse(ServerErrorException arg0) {
		ErrorItem err = new ErrorItem("500", "Server error",arg0.getMessage());
		return Response.status(500).entity(err).build();
	}

}
