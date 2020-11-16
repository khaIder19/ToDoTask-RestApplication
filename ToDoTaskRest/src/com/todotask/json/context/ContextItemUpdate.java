package com.todotask.json.context;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

public class ContextItemUpdate {

	@NotNull
	@Length(min = 10, max = 50, message = "Content ...")
	private String content;
	
	public ContextItemUpdate() {
	}
	
	public ContextItemUpdate(String content) {
		this.content = content;
	}
	
	public String getContent() {
		return content;
	}
	
}
