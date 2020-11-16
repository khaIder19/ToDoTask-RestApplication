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
import com.todotask.json.auth.UserIdentityItem;

@Provider
public class UserIdentityItemWriter implements MessageBodyWriter<UserIdentityItem>{

	@Override
	public boolean isWriteable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType arg3) {
		return (arg0 == UserIdentityItem.class);
	}

	@Override
	public void writeTo(UserIdentityItem arg0, Class<?> arg1, Type arg2, Annotation[] arg3, MediaType arg4,
			MultivaluedMap<String, Object> arg5, OutputStream arg6) throws IOException, WebApplicationException {
		final ObjectMapper mapper = new ObjectMapper();
		String item = mapper.writeValueAsString(arg0);
		arg6.write(item.getBytes());	
	}

}
