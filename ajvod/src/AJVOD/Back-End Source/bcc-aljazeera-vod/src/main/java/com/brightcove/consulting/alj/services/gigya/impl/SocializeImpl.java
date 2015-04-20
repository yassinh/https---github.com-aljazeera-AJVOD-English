package com.brightcove.consulting.alj.services.gigya.impl;

import org.springframework.stereotype.Component;

import com.brightcove.consulting.alj.services.gigya.Socialize;

@Component
public class SocializeImpl extends DynamicService implements Socialize {

    @Override
    public GetUserInfo GetUserInfo() {
        return createApiMethod(GetUserInfo.class);
    }

    @Override
    public SetUserInfo SetUserInfo() {
        return createApiMethod(SetUserInfo.class);
    }

}
