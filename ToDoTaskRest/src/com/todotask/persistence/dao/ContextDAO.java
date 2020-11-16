package com.todotask.persistence.dao;

import java.util.List;

import com.todotask.model.context.Context;

public interface ContextDAO {

	List<Context> getAll();
	List<Context> getById(String... id);
	List<Context> getByUser(String user_uid);
	boolean delete(Context ctx);
	boolean update(Context ctx);
	boolean insert(Context ctx);
	
}
