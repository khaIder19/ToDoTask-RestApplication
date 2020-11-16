package com.todotask.json.task;

import javax.validation.constraints.NotNull;
import com.todotask.json.validation.ConstraintItemUpdateValid;

@ConstraintItemUpdateValid
public class ConstraintItemUpdate {

	@NotNull
	private String dependency_id;
	
	@NotNull
	private String type;
	
	public ConstraintItemUpdate(String dependency_id, String type) {
		super();
		this.dependency_id = dependency_id;
		this.type = type;
	}
	

	public ConstraintItemUpdate() {
		
	}
	
	public String getDependency_id() {
		return dependency_id;
	}
	public String getType() {
		return type;
	}
	
}
