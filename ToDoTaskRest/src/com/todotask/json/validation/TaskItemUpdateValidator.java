package com.todotask.json.validation;

import java.time.Instant;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import com.todotask.json.task.RangeItem;
import com.todotask.json.task.TaskItemUpdate;
import com.todotask.json.task.common.RangeUtils;

public class TaskItemUpdateValidator implements ConstraintValidator<TaskItemUpdateValid, TaskItemUpdate>{

	@Override
	public boolean isValid(TaskItemUpdate arg0, ConstraintValidatorContext arg1) {
		int count = 0;
		
		if(arg0.getContent() != null) count++;
		
		if(arg0.getDelegated_to()!= null) count++;
		
		if(arg0.getStatus() != null)
			count++;
		
		
		if(arg0.getRange() != null) {
			count++;
			
				try {
					validateDate(arg0.getRange());
				}catch(Exception e){
					arg1.buildConstraintViolationWithTemplate("Invalid timestamp format").addConstraintViolation();
					return false;
				}
			
				if(!(validateStart(arg0.getRange().getStart(), arg1) && validateEnd(arg0.getRange().getEnd(), arg1)))
					return false;
		}
		
		
		if(count > 1 || count == 0) {
			System.out.println("count update : "+count);
			arg1.buildConstraintViolationWithTemplate("One field at time can be updated").addConstraintViolation();
			return false;
		}
				
		return true;
	}
	
	private void validateDate(RangeItem item) {
		Instant.parse(item.getStart());
		Instant.parse(item.getEnd());
	}
	
	private boolean validateStart(String start,ConstraintValidatorContext vc) {
		if(RangeUtils.isDefUndefinedTime(start)) {
			vc.buildConstraintViolationWithTemplate("The start time can not be undefined").addConstraintViolation();
			return false;
		}
		
		if(RangeUtils.isBeforeNow(Instant.parse(start))) {
			vc.buildConstraintViolationWithTemplate("The start time is before the current time").addConstraintViolation();
			return false;
		}
		return true;
	}
	
	private boolean validateEnd(String end,ConstraintValidatorContext vc) {
		if(RangeUtils.isDefUndefinedTime(end)) {
			vc.buildConstraintViolationWithTemplate("The end time can not be undefined").addConstraintViolation();
			return false;
		}
		return true;
	}
}
