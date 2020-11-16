package com.todotask.json.validation;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import com.todotask.json.task.TaskItemInsert;
import com.todotask.json.task.common.RangeUtils;

public class TaskItemInsertValidator implements ConstraintValidator<TaskItemInsertValid, TaskItemInsert> {

	
	@Override
	public boolean isValid(TaskItemInsert arg0, ConstraintValidatorContext arg1) {

		if(!new RangeItemValidator().isValid(arg0.getRange(), arg1)) {
			return false;
		}
		
		switch(arg0.getType().toUpperCase()) {
		
		case "ACTIVITY":
		case "PARENT_ACTIVITY":	
			
			if(arg0.getRange() == null) {
				arg1.buildConstraintViolationWithTemplate("The start time and the end time must be explict").addConstraintViolation();
				return false;
			}else {
				return (validateStart(arg0.getRange().getStart(),arg1,ZonedDateTime.of(LocalDateTime.of(LocalDate.now(),LocalTime.of(23, 59)), ZoneId.of("Z")).toInstant()) && validateEnd(arg0.getRange().getEnd(),arg1));
			}
					
		case "TASK":
		case "PARENT_TASK":
			if(arg0.getRange() != null) {
				if(RangeUtils.isDefUndefinedTime(arg0.getRange().getStart()) == true) {
					return true;
				}
				if(RangeUtils.isBeforeNow(Instant.parse(arg0.getRange().getStart()))) {
					arg1.buildConstraintViolationWithTemplate("The time specified is before the current time").addConstraintViolation();
					return false;
				}
			}
			break;
			
			default:
				arg1.buildConstraintViolationWithTemplate("Unknown task type").addConstraintViolation();
				return false;
		}
		return false;	
	}
	
	private boolean validateStart(String start,ConstraintValidatorContext vc,Instant notBefore) {
		if(RangeUtils.isDefUndefinedTime(start)) {
			vc.buildConstraintViolationWithTemplate("The start time can not be undefined").addConstraintViolation();
			return false;
		}
		
		if(Instant.parse(start).isBefore(notBefore)) {
			vc.buildConstraintViolationWithTemplate("The start time can not be today").addConstraintViolation();
			return false;
		}
		return true;
	}
	
	private boolean validateEnd(String end,ConstraintValidatorContext vc) {
		if(RangeUtils.isDefUndefinedTime(end)) {
			vc.buildConstraintViolationWithTemplate("The end time can not be undefined").addConstraintViolation();
			return false;
		}
		if(!Instant.parse(end).isBefore(Instant.now().plus(365,ChronoUnit.DAYS))){
			vc.buildConstraintViolationWithTemplate("The end time too large").addConstraintViolation();
			return false;
		}
		return true;
	}
	
}
