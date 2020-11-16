package com.todotask.rest.providers.reader;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.Instant;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import com.todotask.json.JsonUtils;
import com.todotask.json.JsonValidatorSingleton;
import com.todotask.json.task.RangeItem;
import com.todotask.json.task.StatusItem;
import com.todotask.json.task.TaskItemInsert;
import com.todotask.json.task.common.RangeUtils;

@Provider
public class TaskItemInsertProvider implements MessageBodyReader<TaskItemInsert> {

	@Inject
	private JsonValidatorSingleton validator ;
	
	@Override
	public boolean isReadable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType arg3) {
		return arg0 == TaskItemInsert.class;
	}

	@Override
	public TaskItemInsert readFrom(Class<TaskItemInsert> arg0, Type arg1, Annotation[] arg2, MediaType arg3,
			MultivaluedMap<String, String> arg4, InputStream arg5) throws IOException, WebApplicationException {

		TaskItemInsert item = JsonUtils.getObjectFromStream(arg5,TaskItemInsert.class);
		
		validator.validate(item);
		
		RangeItem range = item.getRange();
		StatusItem status = item.getStatus();
			
		if(range != null)
			range = RangeUtils.truncateRange(item.getRange());
		
		switch(item.getType()) {
		case "TASK":
		case "PARENT_TASK":
			
			if(range == null || RangeUtils.isDefUndefinedTime(range.getStart())) {
				range = getDefTaskRange();
			}
					
			if(status == null)
				status = getDefTaskStatus();
			
			range = new RangeItem(range.getStart(),Instant.MAX.toString());
			
			break;
			
		}
		
		
		return new TaskItemInsert(item.getContent(), status, range, item.getType());
	}
	
	private RangeItem getDefTaskRange() {
		return new RangeItem(Instant.now().toString(),Instant.MAX.toString());
	}
	
	private StatusItem getDefTaskStatus() {
		return new StatusItem(false, true);
	}
	
}
