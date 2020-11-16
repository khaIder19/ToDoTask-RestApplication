package com.todotask.rest.task;

import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.UserTransaction;
import javax.ws.rs.PathParam;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Produces;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.Response;
import org.apache.log4j.Logger;
import com.todotask.data.TaskProvider;
import com.todotask.data.TaskRegistry;
import com.todotask.env.impl.TransactionType;
import com.todotask.env.impl.TransactionTypeAttribute;
import com.todotask.json.task.ConstraintItem;
import com.todotask.model.taskcontent.TaskConstraint;
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
public class ConstraintResource {
	
	private static Logger log = Logger.getLogger(ConstraintResource.class);
	
	@Inject
	private TaskContentDAO taskDao;
	
	@Inject
	@TransactionTypeAttribute(TransactionType.JOIN)
	private UserTransaction ut;
	
	@Inject
	private TaskProvider taskProv;
	
	@Inject
	private TaskRegistry taskRegistry;
	
	@Inject
	private StateDAO stateDao;
	
	@HeaderParam("user_uid")
	private String user_uid;
	
	
	
	public ConstraintResource() {
		super();
	}

	public ConstraintResource(TaskContentDAO taskDao, UserTransaction ut, TaskProvider taskProv,
			TaskRegistry taskRegistry, StateDAO stateDao, String user_uid) {
		super();
		this.taskDao = taskDao;
		this.ut = ut;
		this.taskProv = taskProv;
		this.taskRegistry = taskRegistry;
		this.stateDao = stateDao;
		this.user_uid = user_uid;
	}
	
	@GET
	@ContextAuthorized(ReadPermissionAuth.class)
	@Authorized
	@Produces("application/json")
	public Response getConstraint(@PathParam("t-id")String t_uid,@PathParam("c-id")String c_uid) {
		
		TaskContent tc = taskDao.getById(t_uid).get(0);
		
		if(tc == null)
			throw new BadRequestException("Task not found");
		
		List<TaskConstraint> dependencies = tc.getDependencies();
		ConstraintItem item = null;
		for(TaskConstraint constraint : dependencies) {
			if(constraint.getConstraintUid().equals(c_uid)) {
				item = new ConstraintItem(constraint.getConstraintUid(),t_uid,constraint.getDependencyUid(),constraint.getConstType().name());
			}	
		}
		
		if(item == null)
			throw new BadRequestException("Constraint not found");
		
		return Response.ok().entity(item).build();
	}
	
	
	
	@DELETE
	@ContextAuthorized(TaskPermissionAuth.class)
	@Authorized
	@Produces("application/json")
	public Response deleteConstraint(@PathParam("t-id")String t_uid,@PathParam("c-id")String c_uid) throws StateException {
			
		Response result = null;
		TaskContent tc = taskDao.getById(t_uid).get(0);
		
		
		if(tc == null)
			throw new BadRequestException("Task not found");
		
		ConstraintItem item  = (ConstraintItem) getConstraint(t_uid, c_uid).getEntity();
		
		TaskContent dtc = taskDao.getById(item.getDependency_id()).get(0);
		
		StateObject dependent = taskProv.getStateObject(taskRegistry.getStateId(t_uid),tc.getType());
		StateObject dependency = taskProv.getStateObject(taskRegistry.getStateId(dtc.getTaskId()),dtc.getType());	
		
		removeDependency(dependent, dependency);
			
		try {			
			ut.begin();
			tc.removeDependency(item.getConstraint_id());
			
			taskDao.update(tc);
			stateDao.update((DependencyState) dependent.getStateSet()[0]);	

				ut.commit();
				result = Response.noContent().build();
				
				log.info("Constraint deleted "+"(constraint_uid:"+c_uid+")"+"(task_uid:"+t_uid+")" + "[user:"+user_uid+"]");
			
		}catch (Exception e) {
			log.error("transaction failed : "+ e.getClass().toString()+"[user:"+user_uid+"]",e);
			throw new ServerErrorException(500);
		}		
		return result;
	}
	
	
	
	private void removeDependency(StateObject dependent,StateObject dependency) throws StateException {
	
		boolean result = false;
			
		if(dependent instanceof Task) {
			result = ((Task)dependent).removeDependency(dependency);
		}
		if(dependent instanceof ActivityTask) {
			result = ((ActivityTask)dependent).removeDependency((ActivityTask) dependency);
		}
		
		if(!result) {
			throw new DependencyException("The constraint has not been removed");
		}
	}
	
}
