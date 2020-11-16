package com.todotask.json;

import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ServerErrorException;

@ApplicationScoped
public class JsonValidatorSingleton {

	private Validator validator;
	
	@PostConstruct
	private void init() {
		validator = Validation.buildDefaultValidatorFactory().getValidator();
	}
	
	public <T> void validate(T obj) throws BadRequestException{
		try {
			Set<ConstraintViolation<T>> violations = validator.validate(obj);
			StringBuilder errorStringBuilder = new StringBuilder();
			if(!violations.isEmpty()) {
				for(ConstraintViolation<T> violation : violations) {
					errorStringBuilder.append(violation.getMessage() + "; ");
				}
				throw new BadRequestException(errorStringBuilder.toString());
			}
		}catch(BadRequestException e) {
			throw e;
		}catch(Exception e) {
			e.printStackTrace();
			throw new ServerErrorException(500);
		}
	}
	
	
}
