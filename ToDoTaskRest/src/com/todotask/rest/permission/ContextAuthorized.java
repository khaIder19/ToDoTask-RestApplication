package com.todotask.rest.permission;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.todotask.rest.permission.authorize.DefPermissionAuth;

@Retention(RUNTIME)
@Target({ TYPE, METHOD })
public @interface ContextAuthorized {

	public Class<? extends ContextAuthorization> value() default DefPermissionAuth.class;

}
