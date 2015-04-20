package com.brightcove.consulting.alj.services.gigya.impl;

import java.lang.reflect.Proxy;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;

import com.brightcove.consulting.alj.services.gigya.ApiMethod;
import com.brightcove.consulting.alj.services.gigya.GigyaService;

public abstract class DynamicService implements ApplicationContextAware {

	private ApplicationContext appContext;

    @SuppressWarnings("unchecked")
    protected <T extends ApiMethod>T createApiMethod(Class<T> clazz) {
    	ApiInvocationHandler handler = new ApiInvocationHandler(clazz);
    	if (appContext != null) {
    		appContext.getAutowireCapableBeanFactory().autowireBean(handler);
    	}
        return (T) Proxy.newProxyInstance(
                Thread.currentThread().getContextClassLoader(),
                new Class[] {clazz},
                handler);
    }

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.appContext = applicationContext;
	}


	/**
	 * Given an ApiMethod sub type, this will attempt to determine what the actual
	 * gigya REST api method name should be.
	 *
	 * @param apiMethod
	 * @return
	 */
    public static String getApiMethodName(Class<? extends ApiMethod> apiMethod) {
        String serviceName = getBaseServiceName(apiMethod);
        String operationName = apiMethod.getSimpleName();
        operationName = operationName.substring(0,1).toLowerCase() + operationName.substring(1);
        return serviceName + '.' + operationName; 
    }

    @SuppressWarnings("rawtypes")
	public static String getBaseServiceName(Class<? extends ApiMethod> apiMethod) {
        Class enclosingClass = apiMethod.getEnclosingClass();
        GigyaService serviceAnnotation = AnnotationUtils.findAnnotation(enclosingClass, GigyaService.class);
        if (serviceAnnotation == null) {
            throw new IllegalStateException("ApiMethod '" + apiMethod.getClass().getName()
                    + "' is not contained within a class annotated as a GigyaService.");
        }
        return serviceAnnotation.value();
    }
}
