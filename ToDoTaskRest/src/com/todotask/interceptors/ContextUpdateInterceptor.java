package com.todotask.interceptors;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;
import org.apache.log4j.Logger;

import com.todotask.data.TaskRegistry;
import com.todotask.json.JsonUtils;
import com.todotask.json.event.ContextUpdateEventItem;
import com.todotask.model.context.Context;
import com.todotask.model.taskcontent.TaskContent;

@ContextUpdate
@Interceptor
public class ContextUpdateInterceptor {

	private static Logger log = Logger.getLogger(ContextUpdateInterceptor.class);
	
	@Resource(mappedName = "java:/ToDoTaskConnectionFactory")
	private ConnectionFactory conn;
	
	@Resource(mappedName = "java:/jms/topic/ToDoTaskContextUpdateTopic")
	private Topic ctxUpdateTopic;
	
	@Inject
	private TaskRegistry taskRegistry;
	
	private Connection connection;
	
	public ContextUpdateInterceptor() {
		super();
	}


	public ContextUpdateInterceptor(ConnectionFactory conn, Topic ctxUpdateTopic, TaskRegistry taskRegistry) {
		super();
		this.conn = conn;
		this.ctxUpdateTopic = ctxUpdateTopic;
		this.taskRegistry = taskRegistry;
	}




	@AroundInvoke
	public Object contextUpdatedEvent(InvocationContext invCtx) throws Exception{
		Object returnedObj  = invCtx.proceed();
		Boolean updated = (Boolean) returnedObj;
		Object param = invCtx.getParameters()[0];
		String contextId = null;
		
		if(param instanceof Context) {
			contextId = ((Context)param).getContextId();
		} else if(param instanceof TaskContent) {
			contextId = taskRegistry.getContextId(((TaskContent)param).getTaskId());
		}else {
			return returnedObj;
		}
		
		if(conn == null || ctxUpdateTopic == null) {
			return returnedObj;
		}
		
		if(updated) {
			
			String updateType = invCtx.getMethod().getAnnotation(ContextUpdate.class).value();
			
			ContextUpdateEventItem item = new ContextUpdateEventItem(contextId,updateType);
			
			if(connection == null) {
				try {
				connection = conn.createConnection();
				}catch(JMSException e) {
					log.error("Topic connection error",e);
					return returnedObj;
				}
			}
			
			try(Session s = connection.createSession(false,Session.AUTO_ACKNOWLEDGE)){
				MessageProducer producer = s.createProducer(ctxUpdateTopic);
				producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
				Message mex = s.createTextMessage(JsonUtils.toStringItem(item));
				producer.send(mex);
			} catch (JMSException e) {
				
				log.error(e.getClass().toString(),e);
				
				throw e;
			}
		}
		
		return returnedObj;
	}
	
}
