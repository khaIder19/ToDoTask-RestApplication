package com.todotask.rest.auth;

import java.util.UUID;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.UserTransaction;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.Response;
import org.apache.log4j.Logger;
import com.todotask.json.auth.SignInItem;
import com.todotask.model.user.User;
import com.todotask.persistence.dao.TokenDAO;
import com.todotask.persistence.dao.UserDAO;
import com.todotask.rest.auth.SecurityUtils.PassSalt;
import com.todotask.rest.task.ConstraintResource;

@RequestScoped
public class SignIn {

	private static Logger log = Logger.getLogger(ConstraintResource.class);
	
	@Inject
	private UserDAO userDao;
	
	@Inject
	private TokenDAO tokens;
	
	@Inject
	private UserTransaction ut;
	
	
	
	public SignIn() {
		super();
	}



	public SignIn(UserDAO userDao, TokenDAO tokens, UserTransaction ut) {
		super();
		this.userDao = userDao;
		this.tokens = tokens;
		this.ut = ut;
	}



	@POST
	@Consumes("application/json")
	@Produces("application/json")
	public Response signIn(SignInItem json) {
		UUID id = UUID.randomUUID();
		SecurityUtils utils = SecurityUtils.getInstance();
		PassSalt ps = utils.hashPassword(json.getPass().toCharArray());
		
		User user = new User(id.toString(),json.getUser_name(),json.getUser_email(),ps.getPass(),ps.getSalt());
		char[] token_key = utils.randomPassword(10,15).toCharArray();
		
		String token = utils.encryptString(token_key);
		
		try {
			ut.begin();
			userDao.insert(user);
			
				ut.commit();
				tokens.insert(id.toString(),token);
				
				log.info("User signed "+"(user_email:"+user.getUserEmail()+")" + "[user:"+user.getUserId()+"]");
			
		} catch (Exception e) {
			log.error("transaction failed : "+ e.getClass().toString(),e);
			throw new ServerErrorException(500);
		}
		
		return Response.ok().build();
	}
	
}
