package com.brightcove.consulting.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.stereotype.Service;

@Service
public class SpringContext implements BeanFactoryAware {

    private static BeanFactory springContext;

    private static Logger logger = LoggerFactory.getLogger(SpringContext.class);

    public static Object getBean(String name) {
        if (springContext != null) {
            if (springContext.containsBean(name)) {
                return springContext.getBean(name);
            }
        }
        return null;
    }
    public static BeanFactory getBeanFactory() {
        return springContext;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (springContext != null) {
            logger.warn("bean factory already set");
        }
        springContext = beanFactory;
    }

    
}
