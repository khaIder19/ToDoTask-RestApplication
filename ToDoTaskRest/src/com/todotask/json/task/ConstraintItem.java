package com.todotask.json.task;

public class ConstraintItem {

	private String constraint_id;
	private String dependent;
	private String dependency_id;
	private String type;
	
	public ConstraintItem(String constraint_id, String dependent, String dependency_id, String type) {
		super();
		this.constraint_id = constraint_id;
		this.dependent = dependent;
		this.dependency_id = dependency_id;
		this.type = type;
	}
	
	public ConstraintItem() {
		
	}

	public String getConstraint_id() {
		return constraint_id;
	}

	public String getDependenct() {
		return dependent;
	}

	public String getDependency_id() {
		return dependency_id;
	}

	public String getType() {
		return type;
	}
	
	
}
