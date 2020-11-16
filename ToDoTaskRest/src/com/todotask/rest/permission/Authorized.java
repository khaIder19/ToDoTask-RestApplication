package com.todotask.rest.permission;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.ws.rs.NameBinding;

@Retention(RUNTIME)
@Target({ TYPE, METHOD })
@NameBinding
public @interface Authorized {

}
