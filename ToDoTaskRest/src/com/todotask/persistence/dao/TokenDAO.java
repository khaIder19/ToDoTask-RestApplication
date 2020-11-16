package com.todotask.persistence.dao;

import java.util.List;

public interface TokenDAO {

	List<String> getAll();
	String getById(String id);
	boolean delete(String id);
	boolean insert(String id,String token);
	
}
