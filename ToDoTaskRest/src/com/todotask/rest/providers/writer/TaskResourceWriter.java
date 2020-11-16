package com.todotask.rest.providers.writer;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.todotask.json.task.TaskItem;
import com.todotask.json.task.common.RangeUtils;

@Provider
public class TaskResourceWriter implements MessageBodyWriter<TaskItem>{

	@Override
	public boolean isWriteable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType arg3) {
		return arg0 == TaskItem.class;
	}

	@Override
	public void writeTo(TaskItem arg0, Class<?> arg1, Type arg2, Annotation[] arg3, MediaType arg4,
			MultivaluedMap<String, Object> arg5, OutputStream arg6) throws IOException, WebApplicationException {
		arg0 = new TaskItem(arg0.getTask_id(), arg0.getContext_id(),
				arg0.getContent(), arg0.getStatus(),
				arg0.getType(), arg0.getParent(),
				arg0.getCreated_at(), RangeUtils.getDefString(arg0.getRange()), RangeUtils.getDefString(arg0.getValid_start()),
				RangeUtils.getDefString(arg0.getValid_end()), arg0.getCreator(), arg0.getDelegated_to());
		final ObjectMapper mapper = new ObjectMapper();
		String item = mapper.writeValueAsString(arg0);
		arg6.write(item.getBytes());
	}

}
