package com.todotask.rest.auth;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import org.apache.log4j.Logger;
import com.todotask.interceptors.UserEvent;
import com.todotask.json.auth.LogInItem;
import com.todotask.json.auth.UserIdentityItem;
import com.todotask.model.user.User;
import com.todotask.persistence.dao.TokenDAO;
import com.todotask.persistence.dao.UserDAO;
import com.todotask.rest.task.ConstraintResource;

@RequestScoped
public class LogIn {

	private static Logger log = Logger.getLogger(ConstraintResource.class);
	
	@Inject
	private UserDAO userDao;
	
	@Inject
	private TokenDAO tokens;
	
	
	
	public LogIn() {
		super();
	}



	public LogIn(UserDAO userDao, TokenDAO tokens) {
		super();
		this.userDao = userDao;
		this.tokens = tokens;
	}



	@GET
	@Consumes("application/json")
	@Produces("application/json")
	@UserEvent
	public Response login(LogInItem json) {
		
		Response response = null;
		User user = userDao.getOfEmail(json.getUser_email());
		
		if(user == null) {
			log.info("User log in failed (User not present in the system)"+"(input_user_email:"+json.getUser_email()+")");
			throw new ForbiddenException("User email or password are incorrect");
		}
		
		SecurityUtils utils = SecurityUtils.getInstance();
		boolean result = utils.validate(json.getPass().toCharArray(), user.getSalt(), new String(user.getPass()));
		
		if(result) {

			UserIdentityItem item = new UserIdentityItem(user.getUserId(),tokens.getById(user.getUserId()));
			response = Response.ok().entity(item).build();
			
			log.info("User log in successfully "+"(user_email:"+user.getUserEmail()+")" + "[user:"+user.getUserId()+"]");
		}else {
			
			log.info("User log in failed "+"(user_email:"+user.getUserEmail()+")" + "[user:"+user.getUserId()+"]");
			
			throw new ForbiddenException("User email or password are incorrect");
		}
		
		return response;
	}
	
}
