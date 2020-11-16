package com.todotask.rest.task;

import com.core.model.api.StateObject;
import com.core.model.impl.adjustable.dependent.exc.StateException;
import com.todotask.model.taskcontent.TaskConstraintType;

public interface DependencyInsertion {

	public boolean addDependency(StateObject dependent,StateObject dependency,TaskConstraintType type) throws StateException;
	
}
