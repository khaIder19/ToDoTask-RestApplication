package com.todotask.json.validation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Retention(RUNTIME)
@Target({ TYPE, FIELD, METHOD, PARAMETER, ANNOTATION_TYPE })
@Constraint(validatedBy = StatusItemValidator.class)
public @interface StatusItemValid {

	String message() default "Invalid combination of states";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
	
}
