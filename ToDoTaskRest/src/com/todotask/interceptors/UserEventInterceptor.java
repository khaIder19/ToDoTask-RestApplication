package com.todotask.interceptors;

import javax.annotation.Resource;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;
import com.todotask.json.JsonUtils;
import com.todotask.json.user.UserIdentityItemCollection;
import com.todotask.model.user.User;

@UserEvent
@Interceptor
public class UserEventInterceptor {
	
	private static Logger log = Logger.getLogger(UserEventInterceptor.class);
	
	@Resource(mappedName = "java:/ToDoTaskConnectionFactory")
	private ConnectionFactory conn;
	
	@Resource(mappedName =  "java:/jms/queue/ToDoTaskUserSignInQueue")
	private Queue userSignInQueue;
	
	private Connection connection;
	
	
	public UserEventInterceptor() {
		super();
	}


	public UserEventInterceptor(ConnectionFactory conn, Queue userSignInQueue) {
		super();
		this.conn = conn;
		this.userSignInQueue = userSignInQueue;
	}




	@AroundInvoke
	public Object interceptUserEvent(InvocationContext invCtx) throws Exception {
		Object returnedObj = invCtx.proceed();
		Boolean updated = (Boolean) returnedObj;
		Object param = invCtx.getParameters()[0];
		User user = null;
		if(param instanceof User) {
			user = (User) param;
		}else {
			return returnedObj;
		}
		
		if(conn == null || userSignInQueue == null) {
			return returnedObj;
		}
		
		
		if(updated) {
			
			UserIdentityItemCollection item = new UserIdentityItemCollection(user.getUserId(), user.getUserEmail());
			
			if(connection == null) {
				try {
					connection = conn.createConnection();
					}catch(JMSException e) {
						log.error("Queue connection error",e);
						return returnedObj;
					}
			}
			
			try(Session s = connection.createSession(false,Session.AUTO_ACKNOWLEDGE)){
				System.out.println("session "+s);
				MessageProducer producer = s.createProducer(userSignInQueue);
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
