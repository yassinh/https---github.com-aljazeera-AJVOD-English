package com.brightcove.consulting.alj.services.gigya;

import com.brightcove.consulting.alj.models.User;


@GigyaService("socialize")
public interface Socialize {


    GetUserInfo GetUserInfo();

    SetUserInfo SetUserInfo();

    interface GetUserInfo extends ApiMethod, RequireUid {
    }

    interface SetUserInfo extends ApiMethod, RequireUid {
        @Required
        void setUserInfo(User user);
    }

}
