package com.todotask.rest.providers.exc;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import com.todotask.json.error.ErrorItem;
import com.core.model.impl.adjustable.dependent.exc.DependencyException;

@Provider
public class DependencyExcMapper implements ExceptionMapper<DependencyException> {

	@Override
	public Response toResponse(DependencyException arg0) {
		ErrorItem err = new ErrorItem("400", "Constraint error",arg0.getMessage());
		return Response.status(400).entity(err).build();
	}

}
