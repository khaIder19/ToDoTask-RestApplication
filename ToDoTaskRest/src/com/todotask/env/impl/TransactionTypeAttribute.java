package com.todotask.env.impl;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

@Retention(RUNTIME)
@Target({ TYPE, FIELD, METHOD })
@Qualifier
public @interface TransactionTypeAttribute {

	public TransactionType value() default TransactionType.DEF;
	
}
