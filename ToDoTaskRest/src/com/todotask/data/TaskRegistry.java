package com.todotask.data;

import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.sql.DataSource;
import org.apache.log4j.Logger;
import com.todotask.model.context.Context;
import com.todotask.persistence.dao.ContextDAO;

@ApplicationScoped
public class TaskRegistry {

	private static Logger log = Logger.getLogger(TaskRegistry.class);
	
	private static Properties p;
	
	private static final String ENV_TASK_REGISTRY_PROP = "env.var.prop.taskregistry";
	
	static {
		p = new Properties();
		try {
			p.load(new FileReader(System.getenv(ENV_TASK_REGISTRY_PROP)));
		} catch (Exception e) {
			log.error("Error on properties load");
		}
	}
	
	private static final String TASK_STATE = p.getProperty("map.taskregistry");
	private static final String TASK_STATE_query = p.getProperty("map.taskregistry.query");
	private static final String TASK_STATE_insert = p.getProperty("map.taskregistry.insert");
	private static final String TASK_STATE_task_id = p.getProperty("map.taskregistry.task_id");
	private static final String TASK_STATE_state_id = p.getProperty("map.taskregistry.state_id");
	
	@Inject
	private ContextDAO ctxDao;
	
	@Resource(lookup="java:/ToDoTaskDS")
	private DataSource ds;
	
	//task(key)state(value)
	private Map<String,Long> taskState;
	
	//task(key)context(value)
	private Map<String,String> taskContext;
	
	public TaskRegistry() {
		taskState = new ConcurrentHashMap<>();
		taskContext = new ConcurrentHashMap<>();
	}
	
	@PostConstruct
	public void init() {
		List<Context> contexts = ctxDao.getAll();
		for(Context ctx : contexts) {
			Iterator<String> iterator = ctx.getTasks().keySet().iterator();
			while(iterator.hasNext()) {
				taskContext.put(iterator.next(), ctx.getContextId());
			}
		}
		
		try {
			initTaskStateMap();
		} catch (SQLException e) {
			log.error("task registry initialization error",e);
		}
	}
	
	@PreDestroy
	public void store() {
		storeTasks(taskState);
	}
	
	private void storeTasks(Map<String,Long> map) {
		try(Connection c = ds.getConnection() ; PreparedStatement ps = c.prepareStatement(TASK_STATE_insert)){
			for(Map.Entry<String, Long> e : taskState.entrySet()) {
				ps.setString(1,e.getKey());
				ps.setLong(2, e.getValue());
				ps.addBatch();
			}
			ps.executeBatch();
		} catch (SQLException e1) {
			log.error("Error on task-state map insertion",e1);
		}
	}
	
	private void initTaskStateMap() throws SQLException {
		try(Connection c = ds.getConnection() ; Statement s = c.createStatement()){
			ResultSet rs = s.executeQuery(TASK_STATE_query);
			while(rs.next()) {
				taskState.put(rs.getString(TASK_STATE_task_id),rs.getLong(TASK_STATE_state_id));
			}
		}
	}
	
	public void removeTaskFromRegister(String task_uid) {
			taskState.remove(task_uid);
			taskContext.remove(task_uid);
	}
	
	public void insert(String t_uid,Long s_id,String ctx_uid) {
		taskState.put(t_uid,s_id);
		taskContext.put(t_uid,ctx_uid);
		insertTaskState(t_uid, s_id);
	}
	
	public Long getStateId(String t_uid) {
		return taskState.get(t_uid);
	}
	
	public String getContextId(String t_uid) {
		return taskContext.get(t_uid);
	}
	
	public List<String> getTasksOfContext(String ctx_uid){
		List<String> list = new LinkedList<>();
		for(Map.Entry<String, String> e : taskContext.entrySet()) {
			if(e.getValue().equals(ctx_uid)) {
				list.add(e.getKey());
			}
		}
		return list;
	}
	
	private void insertTaskState(String t_uid,Long s_id) {
		Map<String,Long> temp = new HashMap<String, Long>();
		temp.put(t_uid, s_id);
		storeTasks(temp);
	}
}
