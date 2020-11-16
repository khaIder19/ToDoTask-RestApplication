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
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import org.apache.log4j.Logger;
import com.todotask.json.JsonUtils;
import com.todotask.json.request.RequestItem;
import com.todotask.model.request.RequestEntity;
import com.todotask.model.user.User;
import com.todotask.persistence.dao.UserDAO;
import com.todotask.rest.requests.RequestResource;

@RequestUpdate
@Interceptor
public class RequestUpdateInterceptor {

	private static Logger log = Logger.getLogger(RequestUpdateInterceptor.class);
	
	@Resource(mappedName = "java:/ToDoTaskConnectionFactory")
	private ConnectionFactory conn;
	
	@Resource(mappedName =  "java:/jms/topic/ToDoTaskRequestUpdateTopic")
	private Topic requestUpdateTopic;
	
	@Inject
	private UserDAO userDao;
	
	private Connection connection;
	
	
	public RequestUpdateInterceptor() {
		super();
	}



	public RequestUpdateInterceptor(ConnectionFactory conn, Topic requestUpdateTopic, UserDAO userDao) {
		super();
		this.conn = conn;
		this.requestUpdateTopic = requestUpdateTopic;
		this.userDao = userDao;
	}
	


	@AroundInvoke
	public Object contextUpdatedEvent(InvocationContext invCtx) throws Exception {
		Object returnedObj  = invCtx.proceed();
		Boolean updated = (Boolean) returnedObj;
		Object param = invCtx.getParameters()[0];
		RequestEntity request = null;
		if(param instanceof RequestEntity) {
			request = (RequestEntity) param;
		}else {
			return returnedObj;
		}
		
		if(conn == null || requestUpdateTopic == null) {
			return returnedObj;
		}
		
	
		User user = userDao.getById(request.getFrom_user_uid());
			
		if(updated) {
					
			RequestItem item = RequestResource.toRequestItem(request, user);
			
			if(connection == null) {
				try {
					connection = conn.createConnection();
					}catch(JMSException e) {
						log.error("Topic connection error",e);
						return returnedObj;
					}
			}
			
			try(Session s = connection.createSession(false,Session.AUTO_ACKNOWLEDGE)){
				MessageProducer producer = s.createProducer(requestUpdateTopic);
				producer.setDeliveryMode(DeliveryMode.PERSISTENT);
				TextMessage mex = s.createTextMessage();
				mex.setText(JsonUtils.toStringItem(item));
				producer.send(mex);
			} catch (JMSException e) {
				
				log.error(e.getClass().toString(),e);
				
				throw e;
			}
		}
		
		return returnedObj;
	}
	
}
