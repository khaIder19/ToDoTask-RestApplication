package com.todotask.json.task;

import javax.validation.Valid;
import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class TaskItemObj {

	@Length(min = 10, max = 50, message = "Poor content description")
	private String content;
	
	@Valid
	private StatusItem status;
	
	private RangeItem range;
	
	@JsonIgnore
	private String parent;
	
	public TaskItemObj() {
		
	}
	
	public TaskItemObj(String content, StatusItem status, RangeItem range) {
		super();
		this.content = content;
		this.status = status;
		this.range = range;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public String getParent() {
		return parent;
	}
	
	public String getContent() {
		return content;
	}

	public StatusItem getStatus() {
		return status;
	}

	public RangeItem getRange() {
		return range;
	}
	
	
}
