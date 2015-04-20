package com.brightcove.consulting.alj.api;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates that an api method does not require authorization.
 *
 * @author ssayles
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface NoAuthorization {

}
