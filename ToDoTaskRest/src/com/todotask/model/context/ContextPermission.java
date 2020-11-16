package com.todotask.model.context;

public enum ContextPermission {
	
	MAIN("MAIN"),WRITE("WRITE"),READ("READ");

	private String name;

	private ContextPermission() {
		
	}
	
	private ContextPermission(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
