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
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import org.apache.log4j.Logger;
import com.todotask.data.TaskProvider;
import com.todotask.data.TaskRegistry;
import com.todotask.env.impl.TransactionType;
import com.todotask.env.impl.TransactionTypeAttribute;
import com.todotask.json.CollectionResource;
import com.todotask.json.task.ConstraintItem;
import com.todotask.json.task.ConstraintItemCollection;
import com.todotask.json.task.ConstraintItemUpdate;
import com.todotask.json.task.TaskItemCollection;
import com.todotask.model.taskcontent.TaskConstraint;
import com.todotask.model.taskcontent.TaskConstraintType;
import com.todotask.model.taskcontent.TaskContent;
import com.todotask.persistence.dao.StateDAO;
import com.todotask.persistence.dao.TaskContentDAO;
import com.todotask.rest.permission.Authorized;
import com.todotask.rest.permission.ContextAuthorized;
import com.todotask.rest.permission.authorize.ReadPermissionAuth;
import com.todotask.rest.permission.authorize.TaskPermissionAuth;
import com.core.model.api.StateObject;
import com.core.model.impl.adjustable.dependent.exc.DependencyException;
import com.core.model.impl.adjustable.dependent.exc.StateException;
import com.core.model.impl.adjustable.dependent.states.DependencyState;
import com.core.tasks.ActivityTask;
import com.core.tasks.Task;

@RequestScoped
public class TaskConstraints {
	
	private static Logger log = Logger.getLogger(TaskConstraints.class);

	@Inject
	private TaskContentDAO taskDao;
	
	@Inject
	private TaskRegistry taskRegistry;
	
	@Inject
	private TaskProvider taskProv;
	
	@Inject
	private StateDAO stateDao;
	
	@Inject
	@TransactionTypeAttribute(TransactionType.JOIN)
	private UserTransaction ut;

	@Context
	private ResourceContext resCtx;
	
	private DependencyInsertion taskDependencyInsertion;
	
	private DependencyInsertion activityDependencyInsertion;
	
	private TaskContent depTc;
	
	@HeaderParam("user_uid")
	private String user_uid;
	

	public TaskConstraints(TaskContentDAO taskDao, TaskRegistry taskRegistry, TaskProvider taskProv, StateDAO stateDao,
			UserTransaction ut, ResourceContext resCtx, String user_uid) {
		super();
		this.taskDao = taskDao;
		this.taskRegistry = taskRegistry;
		this.taskProv = taskProv;
		this.stateDao = stateDao;
		this.ut = ut;
		this.resCtx = resCtx;
		this.user_uid = user_uid;
	}


	public TaskConstraints() {
		
		taskDependencyInsertion = new DependencyInsertion() {
			
			@Override
			public boolean addDependency(StateObject dependent, StateObject dependency, TaskConstraintType type)
					throws StateException {
				Task dependentTask = (Task) dependent;				
				return dependentTask.completedIfCompleted(dependency);
			}
		};
		
		activityDependencyInsertion = new DependencyInsertion() {
			
			@Override
			public boolean addDependency(StateObject dependent, StateObject dependency, TaskConstraintType type)
					throws StateException {		
				ActivityTask activityDependent = (ActivityTask) dependent;
				ActivityTask activityDependency = (ActivityTask) dependency;				
				boolean result = false;
				switch(type) {				
				case StoF:
					result = activityDependent.progressIfCompleted(activityDependency);
					break;				
				case StoS:
					result = activityDependent.progressIfProgress(activityDependency);
					break;				
				case FtoF:
					result = activityDependent.completedIfCompleted(activityDependency);
					break;
				default:
					break;			
				}
				return result;
			}
		};
	}
	
	
	@GET
	@ContextAuthorized(ReadPermissionAuth.class)
	@Authorized
	@Produces("application/json")
	public Response getConstraints(@PathParam("t-id")String t_uid) {
		List<ConstraintItemCollection> list = new ArrayList<>();

		TaskContent tc = taskDao.getById(t_uid).get(0);
		
		if(tc == null)
			throw new BadRequestException("Task not found");
		
		for(TaskConstraint constraint : tc.getDependencies()) {
			ConstraintItemCollection item = new ConstraintItemCollection(constraint.getConstraintUid(), constraint.getDependencyUid());
			list.add(item);	
		}
		return Response.ok().entity(new CollectionResource<ConstraintItemCollection>(list)).build();
	}
	
	
	
	@POST
	@Consumes("application/json")
	@ContextAuthorized(TaskPermissionAuth.class)
	@Authorized
	@Produces("application/json")
	public Response insertDependency(@PathParam("t-id")String t_uid,ConstraintItemUpdate json) throws StateException, TaskExtinctException {
		TaskContent tc = taskDao.getById(t_uid).get(0);
		depTc = taskDao.getById(json.getDependency_id()).get(0);
		StateObject state = null;
		
		if(tc == null)
			throw new BadRequestException("Task not found");
		if(depTc == null)
			throw new BadRequestException("Dependency Task not found");
		
		
		switch (tc.getType()) {
		case TASK:
			state = taskProv.getTask(taskRegistry.getStateId(t_uid));	
			addDependency(state, json, taskRegistry.getStateId(json.getDependency_id()),taskDependencyInsertion);
			break;
		case ACTIVITY:
		case PARENT_ACTIVITY:			
			state = taskProv.getActivity(taskRegistry.getStateId(t_uid));
			addDependency(state, json, taskRegistry.getStateId(json.getDependency_id()),activityDependencyInsertion);			
		break;
		case PARENT_TASK:			
			CollectionResource<TaskItemCollection> resColl  = (CollectionResource) resCtx.initResource(CDI.current().select(SubTasks.class).get()).getSubTasks(json.getDependency_id()).getEntity();
			List<TaskItemCollection> subTasks = resColl.getData();				
			for(TaskItemCollection subTask : subTasks) {
				insertDependency(subTask.getTask_id(), new ConstraintItemUpdate(json.getDependency_id(),TaskConstraintType.CtoC.name()));	
			}		
			break;
		default:
			throw new ServerErrorException(500);
		}
		
		Response result = null;
		
		
		
		try {
			ut.begin();
			
			String constraint_uid = UUID.randomUUID().toString();
			tc.addDependecy(constraint_uid,depTc.getTaskId(),TaskConstraintType.valueOf(json.getType()));
						
			taskDao.update(tc);
			stateDao.update((DependencyState)state.getStateSet()[0]);
						
			ConstraintResource constraintResource = null;
			
				constraintResource = resCtx.initResource(CDI.current().select(ConstraintResource.class).get());
				result = constraintResource.getConstraint(t_uid, constraint_uid);
				
				log.info("Constraint created "+"(constraint_uid:"+((ConstraintItem)result.getEntity()).getConstraint_id()+")"+"(task_uid:"+tc.getTaskId()+")" + "[user:"+user_uid+"]");
				
				ut.commit();
				
		} catch (Exception e) {
			log.error("transaction failed : "+ e.getClass().toString()+"[user:"+user_uid+"]",e);
			throw new ServerErrorException(500);
		}		
		return result;	
	}
	
			
	private void addDependency(StateObject state,ConstraintItemUpdate depJson,Long depStateId,DependencyInsertion switcher) throws StateException, TaskExtinctException{
		
		if(state.isStateObjectExtinct())
			throw new TaskExtinctException("This task can not be updated anymore");
		
		boolean result = false;
		
		StateObject depState = null;		
		switch(depTc.getType()) {	
		case TASK:
		case PARENT_TASK:
			depState = taskProv.getTask(depStateId);
			result = switcher.addDependency(state, depState,TaskConstraintType.valueOf(depJson.getType()));
			break;		
		case ACTIVITY:
		case PARENT_ACTIVITY:
			depState= taskProv.getActivity(depStateId);
			result = switcher.addDependency(state, depState,TaskConstraintType.valueOf(depJson.getType()));			
			break;
		}
		
		if(!result) {
			throw new DependencyException("Dependency has not been added");
		}
	}
	
	@Path("{c-id}")
	public ConstraintResource getConstraint() {
		return resCtx.initResource(CDI.current().select(ConstraintResource.class)).get();
	}
		
}
