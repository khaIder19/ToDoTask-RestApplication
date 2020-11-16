package com.todotask.rest.permission.authorize;

import java.util.List;

import javax.enterprise.inject.spi.CDI;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedMap;

import com.todotask.data.TaskRegistry;
import com.todotask.model.context.Context;
import com.todotask.persistence.dao.ContextDAO;
import com.todotask.rest.permission.ContextAuthorization;

public class ReadPermissionAuth implements ContextAuthorization{
	
	private interface ContextIdGetter{
		
		public String getContextId(MultivaluedMap<String, String> params);
		
	}
	
	private ContextIdGetter[] idGetters;
	
	private Context ctx ;
	
	public ReadPermissionAuth() {
		idGetters = new ContextIdGetter[2];
		
		idGetters[0] = new ContextIdGetter() {
			
			@Override
			public String getContextId(MultivaluedMap<String, String> params) {
				List<String> p = params.get("ctx-id");
				String ctx = null;
				if(p != null) {
					ctx = (String) p.get(0);
				}
				return ctx;
		}
		
		};
		
		idGetters[1] = new ContextIdGetter() {
			
			@Override
			public String getContextId(MultivaluedMap<String, String> params) {
				List<String> p = params.get("t-id");
				String tid = null;
				String ctx = null;
				if(p != null) {
					tid = (String) p.get(0);
					ctx = CDI.current().select(TaskRegistry.class).get().getContextId(tid);
				}
				return ctx;
			}
		};
	}
	
	public ReadPermissionAuth(ContextIdGetter[] getters) {
		
	}
	
	
	@Override
	public boolean authorize(String user_uid,ContainerRequestContext req) throws Exception {
		ctx = getContext(req);
		
		if(ctx == null) {
			return false;
		}
		
		return (ctx.getUserMap().containsKey(user_uid));
	}

	protected Context getContext(ContainerRequestContext req) {
		
		if(ctx != null) {
			return ctx;
		}
		
		String ctxId = null;
		
		for(ContextIdGetter getter : idGetters) {
			ctxId = getter.getContextId(req.getUriInfo().getPathParameters());
			if(ctxId != null)
				break;
		}
		
		if(ctxId == null)
			return null;

		ctx =  (Context) CDI.current().select(ContextDAO.class).get().getById(ctxId).get(0);
		return ctx;
	}
	

}
