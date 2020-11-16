package com.todotask.rest.context;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.transaction.UserTransaction;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import org.apache.log4j.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.todotask.data.ContextRegistry;
import com.todotask.env.impl.TransactionType;
import com.todotask.env.impl.TransactionTypeAttribute;
import com.todotask.json.CollectionResource;
import com.todotask.json.context.ContextItem;
import com.todotask.json.context.ContextItemCollection;
import com.todotask.json.context.ContextItemUpdate;
import com.todotask.persistence.dao.ContextDAO;

@RequestScoped
@Path("/contexts")
public class Contexts {

	private static Logger log = Logger.getLogger(Contexts.class);
	
	@Inject
	private ContextDAO contextDao;
	
	@Inject
	private ContextRegistry ctxRegistry;
	
	@Context
	private ResourceContext context;
	
	@Inject
	@TransactionTypeAttribute(TransactionType.JOIN)
	private UserTransaction ut;
	
	
	
	
	public Contexts(ContextDAO contextDao, ContextRegistry ctxRegistry, ResourceContext context, UserTransaction ut) {
		super();
		this.contextDao = contextDao;
		this.ctxRegistry = ctxRegistry;
		this.context = context;
		this.ut = ut;
	}

	public Contexts() {
		super();
	}

	@POST
	@Consumes("application/json")
	@Produces("application/json")
	public Response insertContext(@HeaderParam("user_uid")String user_uid,ContextItemUpdate json) throws JsonProcessingException {
		String ctx_uid = null;
		Response result = null;
		
		try {
			ut.begin();
			ctx_uid = UUID.randomUUID().toString();
			contextDao.insert(new com.todotask.model.context.Context(json.getContent(),user_uid,ctx_uid));
				
				ut.commit();
				com.todotask.model.context.Context ctx = contextDao.getById(ctx_uid).get(0);
				ctxRegistry.getUserContexts(user_uid).put(ctx.getContextId(),ctx.getContent());
				ContextItem ctxJson = new ContextItem(ctx.getContextId(),ctx.getContent(),ctx.getUserMap().get(user_uid).name(),ctx.getCreatorId(),ctx.getCreationTime());
				
				log.info("Context created " + "(context_uid:"+ctx_uid+")" + "[user:"+user_uid+"]");
				
				result = Response.ok().entity(ctxJson).build();
			
		}catch (Exception e) {
		
			log.error("transaction failed : "+ e.getClass().toString()+"[user:"+user_uid+"]",e);
			throw new ServerErrorException(500);
		}
		
		return result ;
	}
	
	@Path("{ctx-id}")
	public ContextResource getContext() {
		ContextResource ctxRes = CDI.current().select(ContextResource.class).get();
		return context.initResource(ctxRes);
	}
	
	@GET
	@Produces("application/json")
	public Response getContexts(@HeaderParam("user_uid")String user_uid){
		List<ContextItemCollection> list = new ArrayList<ContextItemCollection>();
		Set<Map.Entry<String,String>> set = ctxRegistry.getUserContexts(user_uid).entrySet();
		for(Map.Entry<String, String> e : set) {
			list.add(new ContextItemCollection(e.getKey(), e.getValue()));
		}
		CollectionResource<ContextItemCollection> coll = new CollectionResource<ContextItemCollection>(list);
		return Response.ok().entity(coll).build();
	}
	
	
}
