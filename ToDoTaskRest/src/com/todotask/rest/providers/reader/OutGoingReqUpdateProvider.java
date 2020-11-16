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
import com.todotask.json.request.OutgoingRequestItemUpdate;

@Provider
public class OutGoingReqUpdateProvider implements MessageBodyReader<OutgoingRequestItemUpdate> {

	@Inject
	private JsonValidatorSingleton validator;
	
	@Override
	public boolean isReadable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType arg3) {
		if(arg0 == OutgoingRequestItemUpdate.class) {
			return true;
		}
		return false;
	}

	@Override
	public OutgoingRequestItemUpdate readFrom(Class<OutgoingRequestItemUpdate> arg0, Type arg1, Annotation[] arg2,
			MediaType arg3, MultivaluedMap<String, String> arg4, InputStream arg5)
			throws IOException, WebApplicationException {
		OutgoingRequestItemUpdate item = JsonUtils.getObjectFromStream(arg5,OutgoingRequestItemUpdate.class);
		
		validator.validate(item);
		
		return item;
	}

}
