package com.todotask.rest.context;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.transaction.UserTransaction;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.todotask.data.ContextRegistry;
import com.todotask.env.impl.TransactionType;
import com.todotask.env.impl.TransactionTypeAttribute;
import com.todotask.json.CollectionResource;
import com.todotask.json.context.UserContextDataItem;
import com.todotask.json.request.OutgoingRequestItemUpdate;
import com.todotask.json.user.UserItem;
import com.todotask.json.user.UserItemCollection;
import com.todotask.model.context.ContextPermission;
import com.todotask.model.user.User;
import com.todotask.persistence.dao.ContextDAO;
import com.todotask.persistence.dao.UserDAO;
import com.todotask.rest.permission.Authorized;
import com.todotask.rest.permission.ContextAuthorized;
import com.todotask.rest.permission.authorize.AdminPermissionAuth;
import com.todotask.rest.permission.authorize.ReadPermissionAuth;
import com.todotask.rest.requests.UserRequests;

@RequestScoped
public class ContextUsers {
	
	private static Logger log = Logger.getLogger(ContextUsers.class);
	
	@Inject
	private UserDAO userDao;
	
	@Context
	private ResourceContext resCtx;
	
	@Inject
	private ContextRegistry ctxRegistry;
	
	
	private com.todotask.model.context.Context ctx;
	
	@Inject
	private ContextDAO ctxDao;
	
	@Inject
	@TransactionTypeAttribute(TransactionType.JOIN)
	private UserTransaction ut;
	
	
	
	
	public ContextUsers() {
		super();
	}

	public ContextUsers(UserDAO userDao, ResourceContext resCtx, ContextRegistry ctxRegistry,
			com.todotask.model.context.Context ctx, ContextDAO ctxDao, UserTransaction ut) {
		super();
		this.userDao = userDao;
		this.resCtx = resCtx;
		this.ctxRegistry = ctxRegistry;
		this.ctx = ctx;
		this.ctxDao = ctxDao;
		this.ut = ut;
	}

	@GET
	@ContextAuthorized(ReadPermissionAuth.class)
	@Authorized
	@Produces("application/json")
	public Response getUsers(@HeaderParam("user_uid")String user_uid,@PathParam("ctx-id")String ctxId,@QueryParam("permission")String perm) {
		
		ctx = ctxDao.getById(ctxId).get(0);
		
		if(ctx == null)
			throw new BadRequestException("Context not found");
			
		
		Set<Map.Entry<String,ContextPermission>> entrySet = ctx.getUserMap().entrySet();
		List<UserItemCollection> list = new ArrayList<UserItemCollection>();
		
		for(Map.Entry<String,ContextPermission> e : entrySet) {
			
			if(perm != null) {
				if(e.getValue() == ContextPermission.valueOf(perm)) {
					list.add(new UserItemCollection(e.getKey(),getUserItem(e.getKey())));	
				}
			}else {
				list.add(new UserItemCollection(e.getKey(),getUserItem(e.getKey())));	
			}
		}
		
		CollectionResource<UserItemCollection> coll = new CollectionResource<UserItemCollection>(list);
		
		return Response.ok().entity(coll).build();	
	}
	
	@Path("/{u-id}")
	public ContextUserResource getUser() {
		ContextUserResource userRes = CDI.current().select(ContextUserResource.class).get();
		return resCtx.initResource(userRes);
	}
	
	@POST
	@Consumes("application/json")
	@Produces("application/json")
	public Response insertUser(@HeaderParam("user_uid")String from,@PathParam("ctx-id")String ctxId,OutgoingRequestItemUpdate json) throws JsonProcessingException {
		
		ctx = ctxDao.getById(ctxId).get(0);
		
		if(ctx == null)
			throw new BadRequestException("Context not found");
		
		return resCtx.initResource(CDI.current().select(UserRequests.class).get().insertRequest(from, ctx.getContextId(), json));
	}
	
	
	@PUT
	@Consumes("application/json")
	@ContextAuthorized(AdminPermissionAuth.class)
	@Produces("application/json")
	public Response updateUser(UserContextDataItem json) {
		
		ctx = ctxDao.getById(json.getData().getUser().getCtx_id()).get(0);
			
		if(ctx == null)
			throw new BadRequestException("Context not found");
		
		Response result = null;
		
		String user_uid = json.getData().getUser().getUser_id();
		
		try {
			ut.begin();
			
			ctx.insertUser(user_uid,ContextPermission.valueOf(json.getData().getPermission()));
			ctxDao.update(ctx);
			
			
				ut.commit();
				ctxRegistry.getUserContexts(user_uid).put(ctx.getContextId(),ctx.getContent());
				
				log.info("User inserted " + "(user_uid:"+user_uid+")" + "[user:"+"admin"+"]");
				
				result = resCtx.initResource(CDI.current().select(ContextUserResource.class)).get().getUser(user_uid,ctx.getContextId());
			
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
