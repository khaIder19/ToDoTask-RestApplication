package com.todotask.json.validation;

import java.time.Instant;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import com.todotask.json.task.RangeItem;
import com.todotask.json.task.common.RangeUtils;
import com.core.model.TimeRange;

public class RangeItemValidator implements ConstraintValidator<RangeItemValid,RangeItem>{

	@Override
	public boolean isValid(RangeItem arg0, ConstraintValidatorContext arg1) {
		
		try {
			
			Instant start = validateInstant(arg0.getStart());
			
			if(start == null)
				start = Instant.MIN;
			
			Instant end = validateInstant(arg0.getEnd());
			
			if(end == null)
				end = Instant.MAX;
			
			new TimeRange(start.getEpochSecond(), end.getEpochSecond());
			
		}catch(RuntimeException e) {
			arg1.buildConstraintViolationWithTemplate("Invalid timestamp format").addConstraintViolation();
			return false;
		}
		return true;
	}
	
	
	private Instant validateInstant(String toParse) {
		
		if(RangeUtils.isDefUndefinedTime(toParse)) {
			return null;
		}
		
		return Instant.parse(toParse);
	}


}
