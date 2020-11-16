package com.todotask.rest.providers.reader;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.todotask.json.task.RangeItem;
import com.todotask.json.task.TaskItemObj;
import com.todotask.json.task.TaskItemUpdate;
import com.todotask.json.task.common.RangeUtils;

@Provider
public class TaskItemObjProvider implements MessageBodyReader<TaskItemObj>{

	@Override
	public boolean isReadable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType arg3) {
		return arg0 == TaskItemObj.class;
	}

	@Override
	public TaskItemObj readFrom(Class<TaskItemObj> arg0, Type arg1, Annotation[] arg2, MediaType arg3,
			MultivaluedMap<String, String> arg4, InputStream arg5) throws IOException, WebApplicationException {
		
		final ObjectMapper mapper = new ObjectMapper();
		
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
		
		TaskItemUpdate item = mapper.readValue(arg5,TaskItemUpdate.class);
			
		RangeItem truncatedRange = item.getRange();
		
		if(truncatedRange != null)
			truncatedRange = RangeUtils.setDefValues(truncatedRange);
			truncatedRange = RangeUtils.truncateRange(item.getRange());
		
		
		return new TaskItemObj(item.getContent(), item.getStatus(), truncatedRange);
	}
	
}
