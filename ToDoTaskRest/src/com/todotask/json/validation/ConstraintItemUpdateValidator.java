package com.todotask.json.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import com.todotask.json.task.ConstraintItemUpdate;

public class ConstraintItemUpdateValidator  implements ConstraintValidator<ConstraintItemUpdateValid,ConstraintItemUpdate>{

	@Override
	public boolean isValid(ConstraintItemUpdate arg0, ConstraintValidatorContext arg1) {
		switch (arg0.getType().toLowerCase()) {
		case "ctoc":	
			break;
		case "ftof":	
			break;
		case "stos":	
			break;
		case "stof":
			break;
		default:
			arg1.buildConstraintViolationWithTemplate("Invalid constraint type ").addConstraintViolation();
			return false;
		}
		return true;
	}

}
