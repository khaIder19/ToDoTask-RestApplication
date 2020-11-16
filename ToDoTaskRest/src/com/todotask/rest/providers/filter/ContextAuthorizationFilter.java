package com.todotask.rest.providers.filter;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import javax.annotation.Priority;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import org.apache.log4j.Logger;
import com.todotask.rest.permission.Authorized;
import com.todotask.rest.permission.ContextAuthorized;

@Priority(2)
@Provider
@Authorized
public class ContextAuthorizationFilter implements ContainerRequestFilter {
	
	private static Logger log = Logger.getLogger(ContextAuthorizationFilter.class);
	
	private String user_uid;
		
	
	@Override
	public void filter(ContainerRequestContext arg0) throws IOException {
		UriInfo uri = arg0.getUriInfo();
		user_uid = arg0.getHeaderString("user_uid");
		
			
		ContextAuthorized ctxAuthAnn = getMatchedMethodAnnotation(arg0.getMethod(),uri.getMatchedResources().get(0).getClass().getMethods());
			
		try {
			if(!ctxAuthAnn.value().newInstance().authorize(user_uid,arg0)) {
					throw new ForbiddenException("User not authorized for this action");
			}
				
		}catch(ForbiddenException e){
			
			log.info("User not authorized "+"(resource:"+arg0.getUriInfo().getPath()+")" + "[user:"+arg0.getHeaderString("user_uid")+"]");
			
			throw e;
		}catch (Exception e) {
			log.error(e.getClass().toString(),e);
			throw new ServerErrorException(500);
		}
		
	}
	

	public ContextAuthorized getMatchedMethodAnnotation(String method,Method[] methods) {
		for(Method m : methods) {
			for(Annotation mAnn : m.getAnnotations()) {
				if(mAnn.toString().contains(method)) {
					return m.getAnnotation(ContextAuthorized.class);
				}
			}
		}		
		return null;
	}
	
}
