package com.todotask.rest.context;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.UserTransaction;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.Response;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.todotask.env.impl.TransactionType;
import com.todotask.env.impl.TransactionTypeAttribute;
import com.todotask.json.context.UserContextDataItem;
import com.todotask.json.context.UserContextItem;
import com.todotask.json.context.UserContextPair;
import com.todotask.json.user.UserItem;
import com.todotask.model.context.Context;
import com.todotask.model.user.User;
import com.todotask.persistence.dao.ContextDAO;
import com.todotask.persistence.dao.UserDAO;
import com.todotask.rest.permission.Authorized;
import com.todotask.rest.permission.ContextAuthorized;
import com.todotask.rest.permission.authorize.DefPermissionAuth;
import com.todotask.rest.permission.authorize.ReadPermissionAuth;

@RequestScoped
public class ContextUserResource {

	private static Logger log = Logger.getLogger(ContextUserResource.class);
	
	@Inject
	private ContextDAO ctxDao;
	
	@Inject
	private UserDAO userDao;
	
	@Inject
	@TransactionTypeAttribute(TransactionType.JOIN)
	private UserTransaction ut;
	
	private com.todotask.model.context.Context ctx;
	
	
	public ContextUserResource(ContextDAO ctxDao, UserDAO userDao, UserTransaction ut, Context ctx) {
		super();
		this.ctxDao = ctxDao;
		this.userDao = userDao;
		this.ut = ut;
		this.ctx = ctx;
	}


	public ContextUserResource() {
		super();
	}


	@GET
	@ContextAuthorized(ReadPermissionAuth.class)
	@Authorized
	@Produces("application/json")
	public Response getUser(@PathParam("u-id")String u_id,@PathParam("ctx-id")String ctxId){
		
		ctx = ctxDao.getById(ctxId).get(0);
		
		if(ctx == null)
			throw new BadRequestException("Context not found");
		
		if(!ctx.getUserMap().containsKey(u_id))
			throw new BadRequestException("User not found");
		
		UserContextPair ctx_user = new UserContextPair(u_id,ctx.getContextId());
		UserContextItem ctx_user_data = new UserContextItem(ctx_user, ctx.getUserMap().get(u_id));
		UserContextDataItem contextItem = new UserContextDataItem(getUserItem(u_id),ctx_user_data);
		return Response.ok().entity(contextItem).build();
		
	}
	
	
	@DELETE
	@ContextAuthorized(DefPermissionAuth.class)
	@Produces("application/json")
	public Response deleteUser(@HeaderParam("user_uid")String user_uid,@PathParam("u-id")String user_uid_param,@PathParam("ctx-id")String ctxId) throws JsonProcessingException {
		Response result = null;
		
		ctx = ctxDao.getById(ctxId).get(0);
		
		if(ctx == null) 
			throw new BadRequestException("Context not found");
		
		if(ctx.getUserMap().containsKey(user_uid_param))
			throw new BadRequestException("User not found");
		
			try {
				ut.begin();
				ctx.removeUser(user_uid_param);
				ctxDao.update(ctx);
				
					ut.commit();
					
					log.info("User deleted " + "(user_uid:"+user_uid_param+")" + "[user:"+user_uid+"]");
					
					result = Response.ok().build();
				
			} catch (Exception e) {
				log.error("transaction failed : "+ e.getClass().toString()+"[user:"+user_uid+"]",e);
				throw new ServerErrorException(500);
			}
		return result;
		
	}
	
	
	
	
	private UserItem getUserItem(String user_uid) {
		User user = userDao.getById(user_uid);
		UserItem userItem = new UserItem(user.getUserName(), user.getUserEmail());
		return userItem;
	}
}
