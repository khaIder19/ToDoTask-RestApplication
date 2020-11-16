package com.todotask.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.todotask.model.context.Context;
import com.todotask.model.user.User;
import com.todotask.persistence.dao.ContextDAO;
import com.todotask.persistence.dao.UserDAO;

@ApplicationScoped
public class ContextRegistry {
	
	private static Logger log = Logger.getLogger(ContextRegistry.class);
	
	@Inject
	private ContextDAO dao;
	
	@Inject
	private UserDAO userDao;
	
	private Map<String,Map<String,String>> map;
	
	public ContextRegistry() {
		map = new ConcurrentHashMap<>();
	}
	
	@PostConstruct
	public void initMap() {
		List<User> usertList = userDao.getAll();
		
		for(User u : usertList) {
			List<Context> ctx_list = dao.getByUser(u.getUserId());
			Map<String,String> ctx_map = new HashMap<>();
			
			for(Context ctx : ctx_list) {
				ctx_map.put(ctx.getContextId(), ctx.getContent());
			}
			
			map.put(u.getUserId(),ctx_map);
		}
	}
	
	public Map<String,String> getUserContexts(String user_uid){
		return map.get(user_uid);
	}
	
	public String[] getUserContextIds(String user_uid) {
		Set<String> ids = getUserContexts(user_uid).keySet();
		String[] array = new String[ids.size()];
		ids.toArray(array);
		return array;
	}
}
