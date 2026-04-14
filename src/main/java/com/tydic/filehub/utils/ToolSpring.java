package com.tydic.filehub.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.stereotype.Service;

@Service("ToolSpring")
public final class ToolSpring extends ApplicationObjectSupport {
    private static ApplicationContext applicationContext;

    @Override
    protected void initApplicationContext(ApplicationContext context) throws BeansException {
        super.initApplicationContext(context);
        if (ToolSpring.applicationContext == null) {
            ToolSpring.applicationContext = context;
        }
    }

    public static ApplicationContext getAppContext() {
        return applicationContext;
    }

    public static <T> T getBean(Class<T> requiredType) throws BeansException {
        return applicationContext.getBean(requiredType);
    }

    public static Object getBean(String name) {
        return getAppContext().getBean(name);
    }
}
