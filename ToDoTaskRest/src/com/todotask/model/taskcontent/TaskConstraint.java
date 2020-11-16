package com.todotask.model.taskcontent;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "taskconstraint")
public class TaskConstraint implements Serializable{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="constraint_uid")
	private String constraintUid;
	
	@Column(name="dependency_uid")
	private String dependencyUid;
	
	@Enumerated(EnumType.STRING)
	private TaskConstraintType constType;
		
	
	private TaskConstraint() {
		
	}
	
	public TaskConstraint(String constraintUid,String dependencyUid,TaskConstraintType constType) {
		super();
		this.constraintUid = constraintUid;
		this.dependencyUid = dependencyUid;
		this.constType = constType;
	}

	
	
	public String getConstraintUid() {
		return constraintUid;
	}

	
	
	public String getDependencyUid() {
		return dependencyUid;
	}


	
	public TaskConstraintType getConstType() {
		return constType;
	}
	
	
}
