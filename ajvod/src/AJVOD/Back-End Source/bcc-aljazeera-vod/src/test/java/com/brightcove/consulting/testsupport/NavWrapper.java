package com.brightcove.consulting.testsupport;

import com.brightcove.consulting.alj.models.NavigationItem;

public class NavWrapper<T extends NavigationItem> {

    private T instance;

    NavWrapper(Class<T> clazz, String key) {
        try {
            instance = clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        instance.setKey(key);
    }
    public NavWrapper<T> name(String name) {
        instance.setName(name);
        return this;
    }
    public NavWrapper<T> add(NavWrapper child) {
        instance.addChild(child.instance.getClass().getSimpleName(), child.instance);
        return this;
    }
    public T instance() {
        return instance;
    }
}
