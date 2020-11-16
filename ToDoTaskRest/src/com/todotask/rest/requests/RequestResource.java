package com.todotask.rest.requests;

import java.util.Arrays;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.transaction.UserTransaction;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.todotask.env.impl.TransactionType;
import com.todotask.env.impl.TransactionTypeAttribute;
import com.todotask.json.JsonUtils;
import com.todotask.json.KeyValueItem;
import com.todotask.json.context.UserContextDataItem;
import com.todotask.json.context.UserContextItem;
import com.todotask.json.context.UserContextPair;
import com.todotask.json.request.IncomingRequestItemUpdate;
import com.todotask.json.request.RequestItem;
import com.todotask.json.request.RequestPayloadItem;
import com.todotask.json.user.UserIdentityItemCollection;
import com.todotask.json.user.UserItem;
import com.todotask.model.context.ContextPermission;
import com.todotask.model.request.RequestEntity;
import com.todotask.model.request.RequestPayload;
import com.todotask.model.request.RequestState;
import com.todotask.model.user.User;
import com.todotask.persistence.dao.RequestDAO;
import com.todotask.persistence.dao.UserDAO;
import com.todotask.rest.context.ContextUsers;
import com.todotask.rest.permission.Authorized;
import com.todotask.rest.permission.ContextAuthorized;
import com.todotask.rest.permission.authorize.DefPermissionAuth;

@RequestScoped
@Path("/requests/{req-id}")
public class RequestResource {

	private static Logger log = Logger.getLogger(RequestResource.class);
	
	@Inject
	private RequestDAO reqDao;
	
	@Inject
	@TransactionTypeAttribute(TransactionType.JOIN)
	private UserTransaction ut;
	
	
	@Context
	private ResourceContext resCtx;
	
	@Inject
	private UserDAO userDao;
	
	
	
	public RequestResource() {
		super();
	}

	public RequestResource(RequestDAO reqDao, UserTransaction ut, ResourceContext resCtx, UserDAO userDao) {
		super();
		this.reqDao = reqDao;
		this.ut = ut;
		this.resCtx = resCtx;
		this.userDao = userDao;
	}

	@GET
	@Produces("application/json")
	public Response getRequest(@PathParam("req-id")String req_uid) throws JsonMappingException, JsonProcessingException {
		RequestEntity req = reqDao.getById(req_uid);
		
		if(req == null)
			throw new BadRequestException("Request not found");
		
		User user = userDao.getById(req.getFrom_user_uid());
		
		RequestItem reqItem = toRequestItem(req,user);
		return Response.ok().entity(reqItem).build();
	}
	
	@PUT
	@Consumes("application/json")
	@ContextAuthorized(DefPermissionAuth.class)
	@Authorized
	@Produces("application/json")
	public Response updateRequest(@PathParam("req-id")String req_uid,@HeaderParam("user_uid")String user_uid,IncomingRequestItemUpdate json) throws JsonMappingException, JsonProcessingException {
		
		Response result = null;
		RequestEntity req = reqDao.getById(req_uid);
		
		if(req == null)
			throw new BadRequestException("Request not found");
		
		
		if(req.getTo_user_uid() == user_uid)
			throw new ForbiddenException("Only the receiver can update this request");
		
		if(!(req.getState() == RequestState.PENDING)) {
			throw new BadRequestException("Request is already satisfied");
		}
		
			try {
				ut.begin();
				req.setState(RequestState.valueOf(json.getStatus()));
				
				if(req.getState() == RequestState.ACCEPTED) {
					RequestPayloadItem pl = getPayload(req);
					User user = userDao.getById(req.getTo_user_uid());
					
					UserContextDataItem userData = new UserContextDataItem(new UserItem(user.getUserName(),user.getUserEmail()),
							new UserContextItem(new UserContextPair(user.getUserId(),pl.getCtxUid()),ContextPermission.valueOf(pl.getPermission())));
				
					resCtx.initResource(CDI.current().select(ContextUsers.class)).get().updateUser(userData);
				}
				
				reqDao.update(req);
				
				
					ut.commit();
					result = getRequest(req_uid);
					
					log.info("Request updated "+"(request_status:"+req.getState().toString()+")" +"(request_uid:"+req.getRequest_uid()+")" + "[user:"+user_uid+"]");
					
			} catch (Exception e) {
				log.error("transaction failed : "+ e.getClass().toString()+"[user:"+user_uid+"]",e);
				throw new ServerErrorException(500);
			}
			
		return result;
	}
	
	
	
	@DELETE
	@Authorized
	@ContextAuthorized(DefPermissionAuth.class)
	@Produces("application/json")
	public Response deleteRequest(@PathParam("req-id")String req_uid,@HeaderParam("user_uid")String user_uid) {
		Response result = null;
		RequestEntity req = reqDao.getById(req_uid);
		
		if(req == null)
			throw new BadRequestException("Request not found");

		try {
			ut.begin();
			
			if(req.getFrom_user_uid().equals(user_uid)) {
				reqDao.delete(req);
				result = Response.status(201).build();
			}else {
				req.setState(RequestState.REFUSED);
				reqDao.update(req);
				result = Response.status(201).build();
			}
			
				ut.commit();
				log.info("Request deleted "+"(request_uid:"+req.getRequest_uid()+")" + "[user:"+user_uid+"]");
				
		} catch (Exception e) {
			log.error("transaction failed : "+ e.getClass().toString()+"[user:"+user_uid+"]",e);
			throw new ServerErrorException(500);
		}
		
		return result;
	}
	
	public static RequestItem toRequestItem(RequestEntity req,User user) throws JsonMappingException, JsonProcessingException {
		RequestPayloadItem rplItem = getPayload(req);
		UserIdentityItemCollection from = new UserIdentityItemCollection(user.getUserId(),user.getUserEmail());
		
		KeyValueItem permissionData = new KeyValueItem("permission",rplItem.getPermission());
		KeyValueItem contextData = new KeyValueItem("context_id",rplItem.getCtxUid());
		
		RequestItem reqItem = new RequestItem(req.getRequest_uid(), from,Arrays.asList(contextData,permissionData),req.getState(),req.getCreated_at());
		
		return reqItem;
	}
	
	public static RequestPayloadItem getPayload(RequestEntity req) throws JsonMappingException, JsonProcessingException {
		RequestPayload rpl = req.getData();
		
		RequestPayloadItem rplItem = JsonUtils.getObjectFromString(new String(rpl.getData()),RequestPayloadItem.class);
		return rplItem;
	}
	
}
