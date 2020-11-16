package com.todotask.rest.context;

import java.util.UUID;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.transaction.UserTransaction;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import org.apache.log4j.Logger;
import com.todotask.json.task.common.RangeUtils;
import com.todotask.model.taskcontent.TaskContent;
import com.todotask.model.taskcontent.TaskType;
import com.todotask.persistence.dao.ContextDAO;
import com.todotask.persistence.dao.StateDAO;
import com.todotask.persistence.dao.TaskContentDAO;
import com.todotask.data.TaskRegistry;
import com.todotask.env.impl.StateTimerService;
import com.todotask.env.impl.TransactionType;
import com.todotask.env.impl.TransactionTypeAttribute;
import com.todotask.json.task.TaskItemInsert;
import com.todotask.rest.permission.Authorized;
import com.todotask.rest.permission.ContextAuthorized;
import com.todotask.rest.permission.authorize.ReadPermissionAuth;
import com.todotask.rest.permission.authorize.WritePermissionAuth;
import com.todotask.rest.task.TaskResource;
import com.todotask.rest.task.Tasks;
import com.core.model.TimeRange;
import com.core.model.impl.adjustable.dependent.states.DependencyState;
import com.core.tasks.ActivityTask;
import com.core.tasks.ParentActivityTask;
import com.core.tasks.Task;

@RequestScoped
public class ContextTasks {

	private static Logger log = Logger.getLogger(ContextTasks.class);
	
	@Inject
	private StateDAO stateDao;
	
	@Inject
	private TaskContentDAO taskCntDao;
	
	@Inject
	private TaskRegistry mapper;
	
	@Inject
	private StateTimerService timer;
	
	@Context
	private ResourceContext resCtx;
	
	@Inject
	private ContextDAO ctxDao;
	
	private com.todotask.model.context.Context ctx;
	
	@Inject
	@TransactionTypeAttribute(TransactionType.JOIN)
	private UserTransaction ut;
	
	@HeaderParam("user_uid")
	private String user_uid;
	
	
	public ContextTasks() {
		super();
	}


	public ContextTasks(StateDAO stateDao, TaskContentDAO taskCntDao, TaskRegistry mapper, StateTimerService timer,
			ResourceContext resCtx, ContextDAO ctxDao, com.todotask.model.context.Context ctx, UserTransaction ut,
			String user_uid) {
		super();
		this.stateDao = stateDao;
		this.taskCntDao = taskCntDao;
		this.mapper = mapper;
		this.timer = timer;
		this.resCtx = resCtx;
		this.ctxDao = ctxDao;
		this.ctx = ctx;
		this.ut = ut;
		this.user_uid = user_uid;
	}


	@GET
	@ContextAuthorized(ReadPermissionAuth.class)
	@Authorized
	@Produces("application/json")
	public Response getTasks(@PathParam("ctx-id")String ctxId) {
		
		ctx = ctxDao.getById(ctxId).get(0);
		
		if(ctx == null)
			throw new BadRequestException("Context not found");
		
		return resCtx.initResource(CDI.current().select(Tasks.class)).get().getTasks(null,null,null, ctx.getContextId());
	}

	
	@POST
	@ContextAuthorized(WritePermissionAuth.class)
	@Authorized
	@Consumes("application/json")
	@Produces("application/json")
	public Response createTask(@PathParam("ctx-id")String ctxId,TaskItemInsert json){
		Response response = null;
			
		ctx = ctxDao.getById(ctxId).get(0);
		
		if(ctx == null)
			throw new BadRequestException("Context not found");
		
		try {			
			
			ut.begin();
						
			String task_uid = UUID.randomUUID().toString();
			TaskContent taskContent = new TaskContent(task_uid,json.getContent(),TaskType.valueOf(json.getType()));
			taskContent.setParent(json.getParent());
			DependencyState dp = null;
			taskCntDao.insert(taskContent);
			TaskType type = TaskType.valueOf(json.getType());
			
			switch(type) {
			
				case TASK:
				case PARENT_TASK:
					Task task = new Task(RangeUtils.fromRangeItem(json.getRange()).getStart());
					dp = (DependencyState) task.getStateSet()[0];
					
			break;
				case ACTIVITY:				
					TimeRange tm = RangeUtils.fromRangeItem(json.getRange());
					ActivityTask activity = ActivityTask.getStaticActivity(tm.getStart(),tm.getEnd());
					dp = (DependencyState) activity.getStateSet()[0];
			
			break;
				case PARENT_ACTIVITY:
					ParentActivityTask parentActivity = ActivityTask.getParentActivityTask();
					dp = (DependencyState) parentActivity.getStateSet()[0];
			break;
			
			}
			
			ctx.insertTask(taskContent.getTaskId(),user_uid);
			ctxDao.update(ctx);
			Long stateId = stateDao.insert(dp);
								
			ut.commit();
			
			if(type == TaskType.ACTIVITY || type == TaskType.PARENT_TASK) {
				timer.insertState(dp);
			}
			
			mapper.insert(task_uid.toString(),stateId,ctx.getContextId());				
			log.info("task created : "+ "(task_uid"+task_uid+")"+"[user:"+user_uid+"]");
			
			response = resCtx.initResource(CDI.current().select(TaskResource.class)).get().getTask(task_uid.toString());
			
		} catch (Exception e) {
			log.error("transaction failed : "+ e.getClass().toString()+"[user:"+user_uid+"]",e);
			throw new ServerErrorException(500);
		}	
		
		return response;
	}
	
}
