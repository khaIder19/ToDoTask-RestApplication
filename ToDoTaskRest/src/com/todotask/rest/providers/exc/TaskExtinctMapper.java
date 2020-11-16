package com.todotask.rest.providers.exc;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.todotask.json.error.ErrorItem;
import com.todotask.rest.task.TaskExtinctException;

@Provider
public class TaskExtinctMapper implements ExceptionMapper<TaskExtinctException>{

	@Override
	public Response toResponse(TaskExtinctException arg0) {
		ErrorItem err = new ErrorItem("400", "Task extinct",arg0.getMessage());
		return Response.status(400).entity(err).build();
	}

}
