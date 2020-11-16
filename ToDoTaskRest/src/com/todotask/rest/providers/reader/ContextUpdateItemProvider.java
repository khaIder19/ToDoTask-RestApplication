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
import com.todotask.json.JsonUtils;
import com.todotask.json.context.ContextItemUpdate;

@Provider
public class ContextUpdateItemProvider implements MessageBodyReader<ContextItemUpdate>{
	
	
	@Override
	public boolean isReadable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType arg3) {
		if(arg0 == ContextItemUpdate.class) {
			return true;
		}
		return false;
	}

	@Override
	public ContextItemUpdate readFrom(Class<ContextItemUpdate> arg0, Type arg1, Annotation[] arg2, MediaType arg3,
			MultivaluedMap<String, String> arg4, InputStream arg5) throws IOException, WebApplicationException {
		ContextItemUpdate item = JsonUtils.getObjectFromStream(arg5, ContextItemUpdate.class);
		return item;
	}
	
}
