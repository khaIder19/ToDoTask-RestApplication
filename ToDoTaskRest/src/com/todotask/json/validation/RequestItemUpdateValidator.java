package com.todotask.json.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import com.todotask.json.request.IncomingRequestItemUpdate;

public class RequestItemUpdateValidator  implements ConstraintValidator<RequestItemUpdateValid,IncomingRequestItemUpdate>{

	@Override
	public boolean isValid(IncomingRequestItemUpdate arg0, ConstraintValidatorContext arg1) {
		switch (arg0.getStatus()) {
		case "ACCEPTED":	
			break;
		case "REFUSED":	
			break;
		default:
			arg1.buildConstraintViolationWithTemplate("Invalid status type specified").addConstraintViolation();
			return false;
		}
		return true;
	}

}
