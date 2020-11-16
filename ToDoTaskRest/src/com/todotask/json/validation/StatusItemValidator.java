package com.todotask.json.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.todotask.json.task.StatusItem;

public class StatusItemValidator implements ConstraintValidator<StatusItemValid,StatusItem>{

	@Override
	public boolean isValid(StatusItem arg0, ConstraintValidatorContext arg1) {
		if(arg0.isCompleted() & arg0.isProgress()) {
			return false;
		}
		return true;
	}

}
