package com.todotask.rest.providers.reader;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import com.todotask.json.JsonUtils;
import com.todotask.json.JsonValidatorSingleton;
import com.todotask.json.task.RangeItem;
import com.todotask.json.task.TaskItemUpdate;
import com.todotask.json.task.common.RangeUtils;

@Provider
public class TaskItemUpdateProvider implements MessageBodyReader<TaskItemUpdate>{

	@Inject
	private JsonValidatorSingleton validator;
	
	
	@Override
	public boolean isReadable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType arg3) {
		return arg0 == TaskItemUpdate.class;
	}

	@Override
	public TaskItemUpdate readFrom(Class<TaskItemUpdate> arg0, Type arg1, Annotation[] arg2, MediaType arg3,
			MultivaluedMap<String, String> arg4, InputStream arg5) throws IOException, WebApplicationException {

		TaskItemUpdate item = JsonUtils.getObjectFromStream(arg5,TaskItemUpdate.class);
		
		validator.validate(item);
		
		RangeItem range = item.getRange();
		
		if(range != null)
			range = RangeUtils.truncateRange(item.getRange());
		
		return new TaskItemUpdate(item.getContent(), item.getStatus(),range, item.getDelegated_to());
	}

}
