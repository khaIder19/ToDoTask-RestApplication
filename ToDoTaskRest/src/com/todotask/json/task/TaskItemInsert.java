package com.todotask.json.task;

import javax.validation.constraints.NotNull;


import com.todotask.json.validation.TaskItemInsertValid;

@TaskItemInsertValid
public class TaskItemInsert extends TaskItemObj{
	 
	@NotNull
	private String type;
	
	
	public TaskItemInsert() {
		
	}
	
	public TaskItemInsert(String content, StatusItem status, RangeItem range, String type) {
		super(content,status,range);
		this.type = type;
	}
	
	public String getType() {
		return type;
	}
	
		
}
