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
import com.todotask.json.auth.SignInItem;

@Provider
public class SignInItemProvider implements MessageBodyReader<SignInItem>{

	@Override
	public boolean isReadable(Class<?> arg0, Type arg1, Annotation[] arg2, MediaType arg3) {
		if(arg0 == SignInItem.class) {
			return true;
		}
		return false;
	}

	@Override
	public SignInItem readFrom(Class<SignInItem> arg0, Type arg1, Annotation[] arg2, MediaType arg3,
			MultivaluedMap<String, String> arg4, InputStream arg5) throws IOException, WebApplicationException {
		SignInItem item = JsonUtils.getObjectFromStream(arg5,SignInItem.class);
		return item;
	}

}
