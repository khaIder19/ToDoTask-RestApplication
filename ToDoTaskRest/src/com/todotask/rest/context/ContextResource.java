package com.todotask.rest.context;

import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.transaction.UserTransaction;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import org.apache.log4j.Logger;
import com.todotask.data.ContextRegistry;
import com.todotask.data.TaskRegistry;
import com.todotask.env.impl.TransactionType;
import com.todotask.env.impl.TransactionTypeAttribute;
import com.todotask.json.context.ContextItem;
import com.todotask.json.context.ContextItemUpdate;
import com.todotask.persistence.dao.ContextDAO;
import com.todotask.persistence.dao.TaskContentDAO;
import com.todotask.rest.permission.Authorized;
import com.todotask.rest.permission.ContextAuthorized;
import com.todotask.rest.permission.authorize.MainPermissionAuth;
import com.todotask.rest.permission.authorize.ReadPermissionAuth;

@RequestScoped
public class ContextResource {

	private static Logger log = Logger.getLogger(ContextResource.class);
	
	@Inject
	private ContextDAO ctxDao;
	
	@Inject
	private ContextRegistry registry;
	
	@Context
	private ResourceContext resContext;
	
	@Inject
	private TaskContentDAO taskDao;
	
	@Inject
	private TaskRegistry taskRegistry;
		
	@Inject
	@TransactionTypeAttribute(TransactionType.JOIN)
	private UserTransaction ut;
		
	private com.todotask.model.context.Context ctx;	
	
	
	
	
	public ContextResource() {
		super();
	}

	public ContextResource(ContextDAO ctxDao, ContextRegistry registry, ResourceContext resContext,
			TaskContentDAO taskDao, TaskRegistry taskRegistry, UserTransaction ut,
			com.todotask.model.context.Context ctx) {
		super();
		this.ctxDao = ctxDao;
		this.registry = registry;
		this.resContext = resContext;
		this.taskDao = taskDao;
		this.taskRegistry = taskRegistry;
		this.ut = ut;
		this.ctx = ctx;
	}

	@GET
	@ContextAuthorized(ReadPermissionAuth.class)
	@Authorized
	@Produces("application/json")
	public Response getContext(@HeaderParam("user_uid")String user_uid,@PathParam("ctx-id")String ctxId) {
		
		ctx = ctxDao.getById(ctxId).get(0);
		
		if(ctx == null)
			throw new BadRequestException("Context not found");
		
		ContextItem ctxItem = new ContextItem(ctx.getContextId(), ctx.getContent(), ctx.getUserMap().get(user_uid).name(), ctx.getCreatorId(), ctx.getCreationTime());
		return Response.ok().entity(ctxItem).build();
	}
	
	@PUT
	@ContextAuthorized(MainPermissionAuth.class)
	@Authorized
	@Produces("application/json")
	public Response updateContent(@HeaderParam("user_uid")String user_uid,@PathParam("ctx-id")String ctxId,ContextItemUpdate json) throws ServerErrorException{
		
		ctx = ctxDao.getById(ctxId).get(0);
		
		if(ctx == null)
			throw new BadRequestException("Context not found");
		
		Response response = null;
			try {
				ut.begin();
				ctx.setContent(json.getContent());
				ctxDao.update(ctx);
				
					ut.commit();
					registry.getUserContexts(user_uid).replace(ctx.getContextId(), json.getContent());
					
					log.info("Context updated " + "(context_uid:"+ctx.getContextId()+")" + "[user:"+user_uid+"]");
					
					response = getContext(user_uid,ctx.getContextId());
			} catch (Exception e) {
				log.error("transaction failed : "+ e.getClass().toString()+"[user:"+user_uid+"]",e);
				throw new ServerErrorException(500);
			}
		return response;
	}
	
	@Path("/users")
	public ContextUsers getUsers() {
		ContextUsers users = CDI.current().select(ContextUsers.class).get();
		return resContext.initResource(users);
	}
	
	@Path("/requests")
	public ContextRequests gerRequests() {
		ContextRequests users = CDI.current().select(ContextRequests.class).get();
		return resContext.initResource(users);
	}
	
	@Path("/tasks")
	public ContextTasks getTasks() {
		ContextTasks tasks = CDI.current().select(ContextTasks.class).get();
		return resContext.initResource(tasks);
	}
	
	@DELETE
	@Authorized
	@ContextAuthorized()
	public Response deleteContext(@HeaderParam("user_uid")String user_uid,@PathParam("ctx-id")String ctxId){
		Response response = null;
		
		ctx = ctxDao.getById(ctxId).get(0);
		
		if(ctx == null)
			throw new BadRequestException("Context not found");
		
			try {
				ut.begin();
				ctxDao.delete(ctx);
				
				List<String> taskIds = taskRegistry.getTasksOfContext(ctx.getContextId());
				taskDao.deleteByIds((String[]) taskIds.toArray(new String[0]));
				
					ut.commit();
					registry.getUserContexts(user_uid).remove(ctx.getContextId());
					
					log.info("Context deleted " + "(context_uid:"+ctx.getContextId()+")" + "[user:"+user_uid+"]");
					
					response = Response.noContent().build();
				
			} catch (Exception e) {
				log.error("transaction failed : "+ e.getClass().toString()+"[user:"+user_uid+"]",e);
				throw new ServerErrorException(500);
			}
		return response;
	}
	

}
