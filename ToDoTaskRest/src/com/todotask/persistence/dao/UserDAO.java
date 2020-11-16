package com.todotask.persistence.dao;

import java.util.List;

import com.todotask.model.user.User;

public interface UserDAO {

	List<User> getAll();
	User getById(String id);
	User getOfEmail(String email);
	boolean delete(User user);
	boolean insert(User user);
	
}
