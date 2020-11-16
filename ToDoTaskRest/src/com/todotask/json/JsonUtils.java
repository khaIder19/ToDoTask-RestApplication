package com.todotask.json;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {

	
	public static String toStringItem(Object item) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
			return mapper.writeValueAsString(item);
	}
	
	public static <T> T getObjectFromString(String item,Class<T> c) throws JsonMappingException, JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(item,c);
	}
	
	public static <T> T getObjectFromStream(InputStream source,Class<T> c) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(source,c);
	}
	
}
