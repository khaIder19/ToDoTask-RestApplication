package com.todotask.rest.task;

import java.time.Instant;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.transaction.UserTransaction;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
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
import com.todotask.data.TaskProvider;
import com.todotask.data.TaskRegistry;
import com.todotask.env.impl.TransactionType;
import com.todotask.env.impl.TransactionTypeAttribute;
import com.todotask.json.context.UserContextItem;
import com.todotask.json.context.UserContextPair;
import com.todotask.json.request.DelegationItem;
import com.todotask.json.task.RangeItem;
import com.todotask.json.task.StatusItem;
import com.todotask.json.task.TaskItem;
import com.todotask.json.task.TaskItemUpdate;
import com.todotask.json.task.common.RangeUtils;
import com.todotask.model.context.ContextPermission;
import com.todotask.model.context.DelegationEntry;
import com.todotask.model.taskcontent.TaskContent;
import com.todotask.model.taskcontent.TaskType;
import com.todotask.persistence.dao.ContextDAO;
import com.todotask.persistence.dao.StateDAO;
import com.todotask.persistence.dao.TaskContentDAO;
import com.todotask.rest.permission.Authorized;
import com.todotask.rest.permission.ContextAuthorized;
import com.todotask.rest.permission.authorize.DelegatedPermissionAuth;
import com.todotask.rest.permission.authorize.ReadPermissionAuth;
import com.todotask.rest.permission.authorize.TaskPermissionAuth;
import com.core.model.api.BoundedRange;
import com.core.model.api.StateObject;
import com.core.model.impl.adjustable.dependent.states.DependencyState;
import com.core.tasks.ActivityTask;
import com.core.tasks.Task;

@RequestScoped
public class TaskResource {

	private static interface TaskStateUpdate{
		
		public boolean update(TaskItemUpdate json,TaskContent tc,StateObject stateObj);
		
	}
	
	private static Logger log = Logger.getLogger(ConstraintResource.class);
	
	@Inject
	private TaskContentDAO taskDao;
	
	@Inject
	private StateDAO stateDao;
	
	@Inject
	private ContextDAO ctxDao;
	
	@Inject
	private ContextRegistry ctxRegistry;
	
	@Inject
	private TaskRegistry taskRegistry;
	
	@Inject
	private TaskProvider taskProv;
	
	@Context
	private ResourceContext resCtx;
	
	@Inject
	@TransactionTypeAttribute(TransactionType.JOIN)
	private UserTransaction ut;
	
	@HeaderParam("user_uid")
	private String user_uid;
	
	public TaskResource() {
		super();
	}

	public TaskResource(TaskContentDAO taskDao, StateDAO stateDao, ContextDAO ctxDao, ContextRegistry ctxRegistry,
			TaskRegistry taskRegistry, TaskProvider taskProv, ResourceContext resCtx, UserTransaction ut,
			String user_uid) {
		super();
		this.taskDao = taskDao;
		this.stateDao = stateDao;
		this.ctxDao = ctxDao;
		this.ctxRegistry = ctxRegistry;
		this.taskRegistry = taskRegistry;
		this.taskProv = taskProv;
		this.resCtx = resCtx;
		this.ut = ut;
		this.user_uid = user_uid;
	}


	@GET
	@ContextAuthorized(ReadPermissionAuth.class)
	@Authorized
	@Produces("application/json")
	public Response getTask(@PathParam("t-id")String t_uid) {
				
		TaskContent tc = taskDao.getById(t_uid).get(0);
		
		if(tc == null)
			throw new BadRequestException("Task not found");
		
		String ctx_id = taskRegistry.getContextId(t_uid);
		Long stateId = taskRegistry.getStateId(t_uid);
		com.todotask.model.context.Context ctx = ctxDao.getById(ctx_id).get(0);
		
		DelegationEntry delEntry = ctx.getTasks().get(t_uid);
		String creator = delEntry.getFrom();
		ContextPermission creator_perm = ctx.getUserMap().get(creator);
		DelegationItem delItem = new DelegationItem(new UserContextItem(new UserContextPair(creator,ctx_id),creator_perm),delEntry.getTo(),delEntry.getDelegated_at());
		StatusItem statusItem = null;
		DependencyState depState = null;
		
		switch (tc.getType()) {
			
			case ACTIVITY:
			case PARENT_ACTIVITY:
				ActivityTask activity = taskProv.getActivity(stateId);
				depState = (DependencyState) activity.getStateSet()[0];
				statusItem = new StatusItem(activity.isInProgress(), activity.isCompleted());	
		break;
			
			case TASK:
			case PARENT_TASK:
				Task task = taskProv.getTask(stateId);
				depState = (DependencyState) task.getStateSet()[0];
				statusItem = new StatusItem(task.getStatus(),!task.getStatus());		
		break;
			
			default:
		break;

		}
		
		BoundedRange boundRanges = depState.getBoundedRange();
		
		TaskItem item = TaskItem.Builder.create(t_uid, ctx_id, tc.getContent())
				.creator(delEntry.getFrom())
				.delegation(delItem)
				.createdAt(tc.getCreationTime())
				.range(RangeUtils.getRange(depState.getRange().getStart(),depState.getRange().getEnd()))
				.startValidRange(RangeUtils.getRange(boundRanges.getStartSideValidationRanges()[0].getStart(),
						boundRanges.getStartSideValidationRanges()[0].getEnd()))
				.endValidRange(RangeUtils.getRange(boundRanges.getEndSideValidationRanges()[0].getStart(),
						boundRanges.getEndSideValidationRanges()[0].getEnd()))
				.status(statusItem)
				.type(tc.getType())
				.parent(tc.getParent())
				.build();
		
		return Response.ok().entity(item).build();
	}
	
	
	@PUT
	@Consumes("application/json")
	@ContextAuthorized(DelegatedPermissionAuth.class)
	@Authorized
	@Produces("application/json")
	public Response updateTask(@PathParam("t-id")String t_uid,TaskItemUpdate json) throws TaskExtinctException{
		Response result = null;
		
		TaskContent tc = taskDao.getById(t_uid).get(0);
		
		if(tc == null)
			throw new BadRequestException("Task not found");
		
		String delegatedTo = json.getDelegated_to();
		
		if(delegatedTo != null) {
			if(!ctxRegistry.getUserContexts(delegatedTo).containsKey(taskRegistry.getContextId(t_uid)))
				throw new BadRequestException("User delegated not found");
		}
						
		Long stateId = taskRegistry.getStateId(t_uid);
		
		if(json.getContent() != null) {
			try {
				ut.begin();
				
				tc.setContent(json.getContent());
				taskDao.update(tc);
				
					ut.commit();
					result = getTask(t_uid);
					
					log.info("task updated "+"(task_uid:"+tc.getTaskId()+")" + "[user:"+user_uid+"]");
				
			} catch (Exception e) {
				log.error("transaction failed : "+ e.getClass().toString()+"[user:"+user_uid+"]",e);
				throw new ServerErrorException(500);
			}
			
			return result;
		}
		
		switch (tc.getType()) {
			
			case ACTIVITY:
			case PARENT_ACTIVITY:
				
				result = updateStateObject(json, tc, stateId,new TaskStateUpdate() {
					
					@Override
					public boolean update(TaskItemUpdate json, TaskContent tc, StateObject stateObj) {
						
						RangeItem range = json.getRange();
						ActivityTask activity = (ActivityTask) stateObj;
						boolean result = false;
						
						if(range != null) {
							
							if(tc.getType() == TaskType.ACTIVITY) {
								if(range.getStart() != null && range.getEnd() == null) {
									result = activity.setStartTime(Instant.parse(range.getStart()).getEpochSecond(), true);
								}else if(range.getStart() == null) {
									result =activity.setEndTime(Instant.parse(range.getEnd()).getEpochSecond(), true);
								}else {
									result = activity.setTime(Instant.parse(range.getStart()).getEpochSecond(),Instant.parse(range.getEnd()).getEpochSecond());
								}	
							}
						
						}
						
						return result;
					}
				});
				break;
			
			case TASK:
			case PARENT_TASK:
				result = updateStateObject(json,tc,stateId,new TaskStateUpdate() {		
					@Override
					public boolean update(TaskItemUpdate json, TaskContent tc, StateObject stateObj) {
						
						StatusItem status = json.getStatus();
						boolean result = false;
						Task task = (Task) stateObj;
						
						if(status != null) {
							if(tc.getType() == TaskType.TASK) {
								if(status.isCompleted()) {
									result = task.setCompleted();
									stateDao.update((DependencyState)task.getStateSet()[0]);
								}
						}
					}
					return result;
					}
				});
			break;
			
	}
		return result;
	}
	
	
	private Response updateStateObject(TaskItemUpdate json,TaskContent tc,Long stateId,TaskStateUpdate updater) throws TaskExtinctException {
		Response result = null;
		String delegatedTo = json.getDelegated_to();
		
		StateObject state = taskProv.getStateObject(stateId,tc.getType());
		
		if(state.isStateObjectExtinct()) {
			if(delegatedTo != null) {
				throw new TaskExtinctException("This task can not be delegated anymore");
			}
			if(json.getRange() != null || json.getStatus() != null ) {
				throw new TaskExtinctException("This task can not be updated anymore");
			}
		}else {
			try {
				ut.begin();
				updater.update(json, tc, state);
				stateDao.update((DependencyState) state.getStateSet()[0]);
			if(delegatedTo != null) {
				if(!delegate(tc,delegatedTo)) {
					throw new DelegationException("This task is already delegated");
				}
			}
	
				ut.commit();
				result = getTask(tc.getTaskId());
				
				log.info("task updated "+"(task_uid:"+tc.getTaskId()+")" + "[user:"+user_uid+"]");
			
			}catch(Exception e) {
				log.error("transaction failed : "+ e.getClass().toString()+"[user:"+user_uid+"]",e);
				throw new ServerErrorException(500);
			}
		}
		return result;
	}
	
	
	
	private boolean delegate(TaskContent tc,String to) {
		String ctx_uid = taskRegistry.getContextId(tc.getTaskId());
		com.todotask.model.context.Context ctx = ctxDao.getById(ctx_uid).get(0);
		
		boolean result = false;
		
		switch (tc.getType()) {
		
		case TASK:
		case ACTIVITY:
			
			if(ctx.delegateTask(tc.getTaskId(),to)) {
				ctxDao.update(ctx);
				result = true;
			}
			break;
			
		case PARENT_TASK:
		case PARENT_ACTIVITY:
			
			List<TaskContent> subTasks = taskDao.findByParent(tc.getTaskId());
			
			for(TaskContent subTask : subTasks) {
				DelegationEntry sde = ctx.getTaskDataById(subTask.getTaskId());
				if(!sde.getFrom().equals(sde.getTo())) {
					return false;
				}
			}
			result = ctxDao.update(ctx);
			break;
			
		default:
			result = false;
		}
	
		return result;
	}
	
	@DELETE
	@ContextAuthorized(TaskPermissionAuth.class)
	@Authorized
	@Produces("application/json")
	public Response deleteTask(@PathParam("t-id")String t_uid) throws TaskExtinctException {
		Response result = null;
		TaskContent tc = taskDao.getById(t_uid).get(0);
		
		if(tc == null)
			throw new BadRequestException("Task not found");
		
		StateObject state = taskProv.getStateObject(taskRegistry.getStateId(tc.getTaskId()),tc.getType());
		
		if(state.isStateObjectExtinct())
			throw new TaskExtinctException("This task can not updated or delegated anymore");
		
		try {
			
			ut.begin();
			taskDao.delete(tc);
			com.todotask.model.context.Context ctx = ctxDao.getById(taskRegistry.getContextId(tc.getTaskId())).get(0);
			ctx.removeTask(tc.getTaskId());
			ctxDao.update(ctx);
			stateDao.delete((DependencyState) state.getStateSet()[0]);
			
				ut.commit();
				taskRegistry.removeTaskFromRegister(tc.getTaskId());
				result = Response.noContent().build();
				
				log.info("task deleted "+"(task_uid:"+t_uid+")" + "[user:"+user_uid+"]");
				
		} catch (Exception e) {
			log.error("transaction failed : "+ e.getClass().toString()+"[user:"+user_uid+"]",e);
			throw new ServerErrorException(500);
		}
		
		return result;
	}
	
	
	@Path("/constraints")
	public TaskConstraints getConstraints() {
		return resCtx.initResource(CDI.current().select(TaskConstraints.class).get());
	}
	
	@Path("/subtasks")
	public SubTasks getSubTasks() {
		return resCtx.initResource(CDI.current().select(SubTasks.class).get());
	}
	
}
