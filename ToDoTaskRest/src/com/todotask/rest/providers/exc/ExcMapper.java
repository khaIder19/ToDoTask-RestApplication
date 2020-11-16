 package com.todotask.rest.providers.exc;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import com.todotask.json.error.ErrorItem;

@Provider
public class ExcMapper implements ExceptionMapper<Exception>{

	@Override
	public Response toResponse(Exception arg0) {
		arg0.printStackTrace();
		ErrorItem err = new ErrorItem("500", "Server error","Something is gone wrong in the system !");
		return Response.status(500).entity(err).build();
	}

}
