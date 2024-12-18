package com.emamagic.annotation;

import com.emamagic.conf.DB;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Entity {
    DB db() default DB.POSTGRESQL;
    String name() default "";
}
