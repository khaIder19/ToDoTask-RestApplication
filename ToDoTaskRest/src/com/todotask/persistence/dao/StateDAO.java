package com.todotask.persistence.dao;

import java.util.List;

import com.todotask.persistence.dao.model.StateLoadType;

import com.core.model.impl.Side;
import com.core.model.impl.adjustable.dependent.states.DependencyState;

public interface StateDAO {

	DependencyState getById(Long id);
	List<DependencyState> getAll();
	List<DependencyState> find(Long[] ids);
	List<DependencyState> get(Long id,Side side,StateLoadType type);
	Long insert(DependencyState state);
	boolean update(DependencyState... states);
	boolean delete(DependencyState state);
	
}
