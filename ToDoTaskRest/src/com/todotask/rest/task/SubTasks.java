package com.todotask.rest.task;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.transaction.UserTransaction;
import javax.ws.rs.PathParam;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import org.apache.log4j.Logger;
import com.todotask.data.TaskProvider;
import com.todotask.data.TaskRegistry;
import com.todotask.env.impl.TransactionType;
import com.todotask.env.impl.TransactionTypeAttribute;
import com.todotask.json.CollectionResource;
import com.todotask.json.task.TaskItem;
import com.todotask.json.task.TaskItemCollection;
import com.todotask.json.task.TaskItemInsert;
import com.todotask.model.taskcontent.TaskConstraintType;
import com.todotask.model.taskcontent.TaskContent;
import com.todotask.model.taskcontent.TaskType;
import com.todotask.persistence.dao.ContextDAO;
import com.todotask.persistence.dao.StateDAO;
import com.todotask.persistence.dao.TaskContentDAO;
import com.todotask.rest.context.ContextTasks;
import com.todotask.rest.permission.Authorized;
import com.todotask.rest.permission.ContextAuthorized;
import com.todotask.rest.permission.authorize.ReadPermissionAuth;
import com.todotask.rest.permission.authorize.TaskPermissionAuth;
import com.core.model.api.StateObject;
import com.core.model.impl.adjustable.dependent.exc.DependencyException;
import com.core.model.impl.adjustable.dependent.exc.NotSupportedDependency;
import com.core.model.impl.adjustable.dependent.exc.StateException;
import com.core.model.impl.adjustable.dependent.states.DependencyState;
import com.core.tasks.ActivityTask;
import com.core.tasks.ParentActivityTask;
import com.core.tasks.ParentTask;
import com.core.tasks.Task;

@RequestScoped
public class SubTasks {

	private static Logger log = Logger.getLogger(SubTasks.class);
	
	@Inject
	private ContextDAO ctxDao;
	
	@Inject
	private TaskContentDAO taskDao;
	
	@Inject
	private TaskRegistry mapper;
	
	@Inject
	@TransactionTypeAttribute(TransactionType.JOIN)
	private UserTransaction ut;
	
	@Inject
	private TaskProvider taskProv;

	@Context
	private ResourceContext resCtx;
	
	@Inject
	private StateDAO stateDao;
	
	private DependencyInsertion subTaskInsertion;
	
	private DependencyInsertion subActivityInsertion;
	
	

	public SubTasks(ContextDAO ctxDao, TaskContentDAO taskDao, TaskRegistry mapper, UserTransaction ut,
			TaskProvider taskProv, ResourceContext resCtx, StateDAO stateDao, String user_uid) {
		super();
		this.ctxDao = ctxDao;
		this.taskDao = taskDao;
		this.mapper = mapper;
		this.ut = ut;
		this.taskProv = taskProv;
		this.resCtx = resCtx;
		this.stateDao = stateDao;
		this.user_uid = user_uid;
	}

	@HeaderParam("user_uid")
	private String user_uid;
	
	public SubTasks() {
		subTaskInsertion = new DependencyInsertion() {
			
			@Override
			public boolean addDependency(StateObject dependent, StateObject dependency, TaskConstraintType type)
					throws StateException {
				
				boolean result = false;
				
				if(!(dependency instanceof Task)) {
					throw new DependencyException("Parent task supports only TASK chields");
				}else {
					 result = ((ParentTask) dependent).addSubTask((Task) dependency);
				}				
				return result;		
			}
		};
		
		subActivityInsertion = new DependencyInsertion() {
			
			@Override
			public boolean addDependency(StateObject dependent, StateObject dependency, TaskConstraintType type)
					throws StateException {
				
				boolean result = false;
				
				if(!(dependency instanceof ActivityTask)) {
					throw new DependencyException("Parent task supports only ACTIVITY childs");
				}else {
					 result = ((ParentActivityTask) dependent).addSubActivityTask((ActivityTask) dependency);	
				}
				
				return result;			
			}
		};
	}
	
	public SubTasks(String user) {
		this();
		this.user_uid = user;
	}
	
	@GET
	@ContextAuthorized(ReadPermissionAuth.class)
	@Authorized
	@Produces("application/json")
	public Response getSubTasks(@PathParam("t-id")String t_uid) {
		
		if(t_uid == null)
			throw new BadRequestException("Task not found");
		
		List<TaskContent> subTasks = taskDao.findByParent(t_uid);
		com.todotask.model.context.Context ctx = ctxDao.getById(mapper.getContextId(t_uid)).get(0);
		List<TaskItemCollection> coll = new ArrayList<>();		
		
		for(TaskContent tc : subTasks) {
			TaskItemCollection item = new TaskItemCollection(tc.getTaskId(), ctx.getContextId(), tc.getContent());
			coll.add(item);
		}		
		
		return Response.ok().entity(new CollectionResource<TaskItemCollection>(coll)).build();
	}
	
	
	@POST
	@Consumes("application/json")
	@ContextAuthorized(TaskPermissionAuth.class)
	@Authorized
	@Produces("application/json")
	public Response insertSubTask(@PathParam("t-id")String t_uid,TaskItemInsert json) throws Exception {
		Response result = null;
		
		TaskContent tc = taskDao.getById(t_uid).get(0);
		
		if(tc == null)
			throw new BadRequestException("Task not found");
		
		Long stateId = mapper.getStateId(t_uid);
		
		switch(tc.getType()) {		
		case PARENT_ACTIVITY:
			result = addSubTask(taskProv.getStateObject(stateId, TaskType.PARENT_ACTIVITY), json,tc);
			break;
		case PARENT_TASK:
			result = addSubTask(taskProv.getStateObject(stateId,TaskType.PARENT_TASK),json,tc);
			break;	
		case TASK:
		case ACTIVITY:
			throw new NotSupportedDependency();
		}
		return result;		
	}
	
	private Response addSubTask(StateObject parent,TaskItemInsert subJson,TaskContent tc) throws Exception {
		
		if(parent.isStateObjectExtinct())
			throw new TaskExtinctException("This task can not be updated anymore");
					
		String ctxId = mapper.getContextId(tc.getTaskId());
		
		subJson.setParent(tc.getTaskId());
		
		Response result = resCtx.initResource(CDI.current().select(ContextTasks.class)).get().createTask(ctxId,subJson);	
		
		try {		
			ut.begin();
			
			TaskItem item = (TaskItem) result.getEntity();
			Long subStateId = mapper.getStateId(((TaskItem)result.getEntity()).getTask_id());	
			StateObject child = taskProv.getStateObject(subStateId, TaskType.valueOf(item.getType()));
			boolean addResult = false;
			
			
			switch (tc.getType()) {
			
			case PARENT_TASK:
				
				addResult = subTaskInsertion.addDependency(parent, child,null);
				
				tc.addDependecy(UUID.randomUUID().toString(),item.getTask_id(),TaskConstraintType.CtoC);
				break;
				
			case PARENT_ACTIVITY:
				
				addResult = subActivityInsertion.addDependency(parent, child,null);
				
				tc.addDependecy(UUID.randomUUID().toString(),item.getTask_id(),TaskConstraintType.FtoF);
				tc.addDependecy(UUID.randomUUID().toString(),item.getTask_id(),TaskConstraintType.StoS);
				break;
			default:
				break;
			}
			
			if(!addResult)
				throw new DependencyException("Sub task/activity has not been added");
					
			stateDao.update((DependencyState) parent.getStateSet()[0]);
			taskDao.update(tc);
			
			ut.commit();
						
		
		}catch (Exception e) {
			log.error("transaction failed : "+ e.getClass().toString()+"[user:"+user_uid+"]",e);
			throw e;
		}
		return result;
	}
	
}
