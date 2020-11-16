package com.todotask.persistence.dao;

import java.util.List;

import com.todotask.model.taskcontent.TaskContent;

public interface TaskContentDAO {

	List<TaskContent> getAll();
	List<TaskContent> getById(String... id);
	List<TaskContent> findByParent(String parent_uid);
	List<TaskContent> findDependents(String dependency);
	List<TaskContent> findDependencies(String dependent);
	boolean delete(TaskContent... tk);
	boolean deleteByIds(String... ids);
	boolean update(TaskContent tk);
	boolean insert(TaskContent tk);
	
}
