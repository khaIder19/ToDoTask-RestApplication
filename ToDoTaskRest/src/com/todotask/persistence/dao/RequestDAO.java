package com.todotask.persistence.dao;

import java.util.List;

import com.todotask.model.request.RequestEntity;
import com.todotask.model.request.RequestType;

public interface RequestDAO {

	List<RequestEntity> getAll();
	RequestEntity getById(String uid);
	List<RequestEntity> getBy(String from,String to,RequestType type);
	boolean insert(RequestEntity req);
	boolean delete(RequestEntity req);
	boolean update(RequestEntity req);
	
}
