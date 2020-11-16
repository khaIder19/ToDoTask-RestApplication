package com.todotask.json.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import com.todotask.json.request.OutgoingRequestItemUpdate;

public class OutgoingItemValidator  implements ConstraintValidator<OutgoingRequestValid,OutgoingRequestItemUpdate>{

	@Override
	public boolean isValid(OutgoingRequestItemUpdate arg0, ConstraintValidatorContext arg1) {
		switch (arg0.getPermission()) {
		case "MAIN":	
			break;
		case "WRITE":	
			break;
		case "READ":	
			break;
		default:
			arg1.buildConstraintViolationWithTemplate("Invalid permission type ").addConstraintViolation();
			return false;
		}
		return true;
	}

}
