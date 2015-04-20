package com.brightcove.consulting.alj.services.gigya;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the correlating property as being a required 
 * parameter in thh associated api request. 
 * @author ssayles
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Required {

}
