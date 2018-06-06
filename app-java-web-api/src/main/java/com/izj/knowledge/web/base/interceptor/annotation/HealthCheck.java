package com.izj.knowledge.web.base.interceptor.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Give this annotation only for health check api!<br>
 * All interceptors will be skipped.
 *
 * @author iz-j
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface HealthCheck {

}
