package com.todotask.json.task;

public class ConstraintItemCollection {

	
	private String constraint_id;
	private String dependency_id;
	
	public ConstraintItemCollection(String constraint_id, String dependency_id) {
		super();
		this.constraint_id = constraint_id;
		this.dependency_id = dependency_id;
	}
	
	public ConstraintItemCollection() {
		super();
	}

	public String getConstraint_id() {
		return constraint_id;
	}
	public String getDependency_id() {
		return dependency_id;
	}
	
}
