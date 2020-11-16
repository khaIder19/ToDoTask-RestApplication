package com.todotask.rest.providers.exc;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.todotask.json.error.ErrorItem;
import com.todotask.rest.task.DelegationException;

@Provider
public class DelegationExcMapper implements ExceptionMapper<DelegationException>{

	@Override
	public Response toResponse(DelegationException arg0) {
		ErrorItem err = new ErrorItem("400", "Delegation error",arg0.getMessage());
		return Response.status(400).entity(err).build();
	}

}
