package com.todotask.rest.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import org.apache.log4j.Logger;
import com.todotask.data.ContextRegistry;
import com.todotask.data.TaskRegistry;
import com.todotask.json.CollectionResource;
import com.todotask.json.task.TaskItemCollection;
import com.todotask.model.context.DelegationEntry;
import com.todotask.model.taskcontent.TaskContent;
import com.todotask.persistence.dao.ContextDAO;
import com.todotask.persistence.dao.TaskContentDAO;
import com.todotask.rest.permission.Authorized;
import com.todotask.rest.permission.ContextAuthorized;
import com.todotask.rest.permission.authorize.DefPermissionAuth;

@RequestScoped
@Path("/tasks")
public class Tasks {
	
	private static Logger log = Logger.getLogger(Tasks.class);
	
	@Context
	private ResourceContext resCtx;
	
	@Inject
	private ContextDAO ctxDao;
	
	@Inject
	private TaskContentDAO taskDao;
	
	@Inject
	private ContextRegistry ctxRegistry;
	
	@Inject
	private TaskRegistry taskRegistry;
	
	@HeaderParam("user_uid")
	private String user_uid;
	
	
	
	public Tasks() {
		super();
	}


	public Tasks(ResourceContext resCtx, ContextDAO ctxDao, TaskContentDAO taskDao, ContextRegistry ctxRegistry,
			TaskRegistry taskRegistry, String user_uid) {
		super();
		this.resCtx = resCtx;
		this.ctxDao = ctxDao;
		this.taskDao = taskDao;
		this.ctxRegistry = ctxRegistry;
		this.taskRegistry = taskRegistry;
		this.user_uid = user_uid;
	}


	@GET
	@ContextAuthorized(DefPermissionAuth.class)
	@Authorized
	@Produces("application/json")
	public Response getTasks(@QueryParam("by")String by,@QueryParam("del")String del,
			@QueryParam("type")String type,@QueryParam("ctx")String ctx) {
			
		Map<String,DelegationEntry> taskFinded = new HashMap<String, DelegationEntry>();
		List<com.todotask.model.context.Context> fromCtxs = null;
		
		if(ctx == null) {
			String[] ids = ctxRegistry.getUserContextIds(user_uid);
			fromCtxs = ctxDao.getById(ids);
			for(com.todotask.model.context.Context ctxE : fromCtxs) {
				taskFinded.putAll(ctxE.getTasks());
			}
		}else {
			List<com.todotask.model.context.Context> list = ctxDao.getById(ctx);
			if(!list.isEmpty()) {
				taskFinded = list.get(0).getTasks();
			}else {
				throw new BadRequestException("Context not found");
			}
		}
			
		Map<String,DelegationEntry> filteredCt = TaskUtils.filterContextTasks(taskFinded,del,by);
		
		String[] ids = new String[filteredCt.keySet().size()];
		filteredCt.keySet().toArray(ids);
		
		List<TaskContent> filteredTc = taskDao.getById(ids);
		filteredTc = TaskUtils.filterTasksContent(filteredTc, type);
		
		List<TaskItemCollection> result = new ArrayList<>();
		
		for(TaskContent tc : filteredTc) {
			TaskItemCollection item = new TaskItemCollection(tc.getTaskId(), taskRegistry.getContextId(tc.getTaskId()), tc.getContent());
			result.add(item);
		}
		
		return Response.ok().entity(new CollectionResource<TaskItemCollection>(result)).build();
	}
	
	
	@Path("{t-id}")
	public TaskResource getTask() {
		return resCtx.initResource(CDI.current().select(TaskResource.class)).get();
	}
	
	
}
