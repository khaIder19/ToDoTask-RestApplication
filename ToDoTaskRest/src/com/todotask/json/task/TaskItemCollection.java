package com.todotask.json.task;

public class TaskItemCollection {

	private String task_id;
	private String ctx_id;
	private String content;
	
	public TaskItemCollection() {
		super();
	}

	public TaskItemCollection(String task_id, String context_id, String content) {
		super();
		this.task_id = task_id;
		this.ctx_id = context_id;
		this.content = content;
	}
	
	public String getTask_id() {
		return task_id;
	}
	
	public String getContext_id() {
		return ctx_id;
	}
	
	public String getContent() {
		return content;
	}
	
}
