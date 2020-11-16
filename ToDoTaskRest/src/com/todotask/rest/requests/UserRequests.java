package com.todotask.rest.requests;

import java.util.ArrayList;
import java.util.List;
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
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.todotask.env.impl.TransactionType;
import com.todotask.env.impl.TransactionTypeAttribute;
import com.todotask.json.CollectionResource;
import com.todotask.json.JsonUtils;
import com.todotask.json.request.OutgoingRequestItemUpdate;
import com.todotask.json.request.RequestItemCollection;
import com.todotask.json.request.RequestPayloadItem;
import com.todotask.model.context.ContextPermission;
import com.todotask.model.request.RequestEntity;
import com.todotask.model.request.RequestPayload;
import com.todotask.model.request.RequestState;
import com.todotask.model.request.RequestType;
import com.todotask.persistence.dao.RequestDAO;
import com.todotask.persistence.dao.UserDAO;
import com.todotask.rest.permission.Authorized;
import com.todotask.rest.permission.ContextAuthorized;
import com.todotask.rest.permission.authorize.AdminPermissionAuth;

@RequestScoped
@Path("/requests")
public class UserRequests {

	private static Logger log = Logger.getLogger(UserRequests.class);
	
	@Inject
	private RequestDAO reqDao;
	
	@Inject
	private UserDAO userDao;
	
	@Context
	private ResourceContext resCtx;
	
	@Inject
	@TransactionTypeAttribute(TransactionType.JOIN)
	private UserTransaction ut;
	
	
	

	public UserRequests() {
		super();
	}

	public UserRequests(RequestDAO reqDao, UserDAO userDao, ResourceContext resCtx, UserTransaction ut) {
		super();
		this.reqDao = reqDao;
		this.userDao = userDao;
		this.resCtx = resCtx;
		this.ut = ut;
	}

	@GET
	@Produces("application/json")
	public Response getRequests(@QueryParam("to")String to,@QueryParam("state")String state,@HeaderParam("user_uid")String user) throws JsonMappingException, JsonProcessingException{
		
		List<RequestEntity> list = null;
		
		if(to != null) {
			if(userDao.getById(to) == null) {
				throw new BadRequestException("User receiver not found");
			}
		}else {
			to = user;
		}
		
		if(userDao.getById(user) == null)
			throw new BadRequestException("User sender not found");
	
		list = reqDao.getBy(user, to, RequestType.CTX);
		
		if(list != null && to.equals(user))
			list.removeIf(r->r.getTo_user_uid().equals(user) && r.getState() == RequestState.REFUSED);
		
		
		List<RequestEntity> filteredList = filter(list, state);
		CollectionResource<RequestItemCollection> coll = new CollectionResource<RequestItemCollection>(toCollectionResource(filteredList));
		return Response.ok().entity(coll).build();
		
	}
	
	@POST
	@ContextAuthorized(AdminPermissionAuth.class)
	@Consumes("application/json")
	@Produces("application/json")
	@Authorized
	public Response insertRequest(@HeaderParam("user_uid")String user_uid,@QueryParam("ctx-id")String ctx_uid,OutgoingRequestItemUpdate json) throws JsonProcessingException {
		Response result = null;
		
		if(userDao.getById(json.getTo()) == null)
			throw new BadRequestException("User receiver not found");
		
		if(json.getPermission().equals(ContextPermission.MAIN.name()))
			throw new BadRequestException("The MAIN permission is reserved for only one user in the context");
		
			UUID req_uid = UUID.randomUUID();
			RequestPayload req_data = new RequestPayload(RequestType.CTX,createContextRequestData(ctx_uid,ContextPermission.valueOf(json.getPermission())));
			RequestEntity request = new RequestEntity(req_uid.toString(),user_uid,json.getTo(),RequestState.PENDING,req_data);
			
			try {
				ut.begin();
				
				reqDao.insert(request);
				
				
					ut.commit();
					result = resCtx.initResource(CDI.current().select(RequestResource.class).get()).getRequest(req_uid.toString());
					
					log.info("Request sent " +"(sender_user_uid:"+user_uid+")" + "(receiver_user_uid:"+json.getTo()+")");
						
			} catch(Exception e) {
				log.error("transaction failed : "+ e.getClass().toString()+"[user:"+user_uid+"]",e);
				throw new ServerErrorException(500);
			}
		return result;
	}
	
	
	private List<RequestItemCollection> toCollectionResource(List<RequestEntity> list){
		List<RequestItemCollection> result = new ArrayList<>();
			for(RequestEntity e : list) {
				result.add(new RequestItemCollection(e.getRequest_uid(), userDao.getById(e.getFrom_user_uid()).getUserEmail(), userDao.getById(e.getTo_user_uid()).getUserEmail()));
			}
		return result;
	}
	
	public List<RequestEntity> filter(List<RequestEntity> list,String state) throws JsonMappingException, JsonProcessingException {
		List<RequestEntity> result = new ArrayList<>();
		if(state != null) {
			for(RequestEntity e : result) {
				if(e.getState().name().equals(state)) {
					result.add(e);
				}
			}
		}else {
			result = list;
		}		
		return result;
	}
	
	private String createContextRequestData(String ctx,ContextPermission p) throws JsonProcessingException {
		return new String(JsonUtils.toStringItem(new RequestPayloadItem(ctx,p)));
	}
}
