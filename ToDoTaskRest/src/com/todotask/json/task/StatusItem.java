package com.todotask.json.task;

import com.todotask.json.validation.StatusItemValid;

@StatusItemValid
public class StatusItem {

	private boolean completed;
	private boolean progress;
	
	public StatusItem() {
		
	}
	
	public StatusItem(boolean completed, boolean progress) {
		super();
		this.completed = completed;
		this.progress = progress;
	}
	
	public boolean isCompleted() {
		return completed;
	}
	
	public boolean isProgress() {
		return progress;
	}
	
	

}
