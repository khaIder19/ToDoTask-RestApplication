package com.todotask.json;

import java.util.List;

public class CollectionResource<T> {

	private List<T> data;
	
	public CollectionResource(List<T> data) {
		this.data = data;
	}
	
	public CollectionResource() {
		
	}
	
	public List<T> getData(){
		return data;
	}
	
}
