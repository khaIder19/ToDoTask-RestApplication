package com.todotask.json;

public class KeyValueItem {

	private String name;
	private String value;
	
	public KeyValueItem(String name, String value) {
		super();
		this.name = name;
		this.value = value;
	}
	
	public KeyValueItem() {
		super();
	}
	
	public String getName() {
		return name;
	}
	
	public String getValue() {
		return value;
	}
	
}
