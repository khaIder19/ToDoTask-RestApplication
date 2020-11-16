package com.todotask.rest.users;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import com.todotask.json.user.UserItem;
import com.todotask.json.user.UserItemCollection;
import com.todotask.model.user.User;
import com.todotask.persistence.dao.UserDAO;

@RequestScoped
@Path("/user")
public class UserResource {

	@Inject
	private UserDAO userDao;
	
	
	
	public UserResource() {
		super();
	}



	public UserResource(UserDAO userDao) {
		super();
		this.userDao = userDao;
	}



	@GET
	@Produces("application/json")
	public Response getUser(@QueryParam("uid")String user_uid,@QueryParam("email")String email) {
		
		User user = null;
		
		if(user_uid != null) {
			user = userDao.getById(user_uid);	
		}else if(email != null) {
			user = userDao.getById(email);
		}
		
		if(user == null)
			throw new BadRequestException("user not found");
		
		UserItemCollection item = new UserItemCollection(user.getUserId(), new UserItem(user.getUserName(), user.getUserEmail()));
		
		return Response.ok().entity(item).build();
	}
	
}
