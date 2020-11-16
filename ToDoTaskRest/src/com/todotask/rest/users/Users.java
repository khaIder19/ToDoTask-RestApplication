package com.todotask.rest.users;

import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import com.todotask.json.CollectionResource;
import com.todotask.json.user.UserIdentityItemCollection;
import com.todotask.model.user.User;
import com.todotask.persistence.dao.UserDAO;
import com.todotask.rest.permission.Authorized;
import com.todotask.rest.permission.ContextAuthorized;
import com.todotask.rest.permission.authorize.AdminPermissionAuth;

@Path("/users")
@RequestScoped
public class Users {

	@Inject
	private UserDAO userDao;
	
	@Context
	private ResourceContext resCtx;
	
	@GET
	@Produces("application/json")
	@ContextAuthorized(AdminPermissionAuth.class)
	@Authorized
	public Response getUsers() {
		
		List<User> list = userDao.getAll();
		List<UserIdentityItemCollection> users = new ArrayList<UserIdentityItemCollection>();  
		
		for(User user : list) {
			users.add(new UserIdentityItemCollection(user.getUserId(),user.getUserEmail()));
		}
		
		return Response.ok().entity(new CollectionResource<UserIdentityItemCollection>(users)).build();
	}
	
	
}
