package com.todotask.json.task;

import com.todotask.json.validation.TaskItemUpdateValid;

@TaskItemUpdateValid
public class TaskItemUpdate extends TaskItemObj{

	private String delegated_to;
	
	public TaskItemUpdate(String content, StatusItem status, RangeItem range, String delegated_to) {
		super(content,status,range);
		this.delegated_to = delegated_to;
	}
	
	public TaskItemUpdate() {
		
	}
	
	
	public String getDelegated_to() {
		return delegated_to;
	}

	
}
