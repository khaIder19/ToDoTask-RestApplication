package com.todotask.data;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.todotask.model.taskcontent.TaskType;
import com.todotask.persistence.dao.StateDAO;
import com.core.model.api.StateObject;
import com.core.model.impl.adjustable.dependent.states.DependencyState;
import com.core.tasks.ActivityTask;
import com.core.tasks.ParentActivityTask;
import com.core.tasks.ParentTask;
import com.core.tasks.Task;

@ApplicationScoped
public class TaskProvider {

	private static Logger log = Logger.getLogger(TaskProvider.class);
	
	@Inject
	private StateDAO stateDao;
	
	public Task getTask(Long id) {
		DependencyState ds = stateDao.getById(id);
		Task t = getStateObject(Task.class, ds, "completedState",false);
		return t;
	}
		
	public ParentTask getParentTask(Long id) {
		DependencyState ds = stateDao.getById(id);
		ParentTask t = getStateObject(ParentTask.class, ds, "completedState",true);
		return t;
	}
	
	public ActivityTask getActivity(Long id) {
		DependencyState ds = stateDao.getById(id);
		ActivityTask at = getStateObject(ActivityTask.class, ds,"progressState",false);
		return at;
	}
	
	public ParentActivityTask getParentActivity(Long id) {
		DependencyState ds = stateDao.getById(id);
		ParentActivityTask at = getStateObject(ParentActivityTask.class, ds,"progressState",true);
		return at;
	}
	
	
	public StateObject getStateObject(Long id,TaskType type) {
		StateObject result = null;
			
		switch(type) {
			case PARENT_ACTIVITY:
				result = getParentActivity(id);
			break;
			case ACTIVITY:
				result = getActivity(id);
				break;
			case TASK:
				result = getTask(id);
				break;
			case PARENT_TASK:
				result = getParentTask(id);
				break;
			default:
				break;
		}
		
		return result;
	}
	
	
	private <T> T getStateObject(Class<T> type,DependencyState ds,String stateFieldName,boolean parent) {
		T obj = null;
			try {
				Constructor<T> c = (Constructor<T>) type.getDeclaredConstructor();
				c.setAccessible(true);
				obj =  c.newInstance();
				Field state = null;
				if(parent) {
				state = type.getSuperclass().getDeclaredField(stateFieldName);
				}else {
				state = type.getDeclaredField(stateFieldName);
				}
				state.setAccessible(true);
				state.set(obj, ds);
			} catch (NoSuchMethodException |SecurityException | InstantiationException |
					IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchFieldException e) {
				log.error(e);
			}		
		return obj;
	}
	

	public static Long getDependencyStateId(DependencyState state) {	
		Long id = -1L;
		Field idField;
		try {
			idField = state.getClass().getDeclaredField("id");
			idField.setAccessible(true);
			id = (Long) idField.get(state);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			log.error(e);
		}
			return id;
	}
	
	
}
