package com.brightcove.consulting.alj.services.gigya.impl;

import org.springframework.stereotype.Component;

import com.brightcove.consulting.alj.services.gigya.Accounts;

@Component(value="gigyaAccountsApi")
public class AccountsImpl extends DynamicService implements Accounts {

    @Override
    public GetAccountInfo GetAccountInfo() {
        return createApiMethod(GetAccountInfo.class);
    }

    @Override
    public GetSchema GetSchema() {
        return createApiMethod(GetSchema.class);
    }
    
    @Override
    public GetSessionInfo GetSessionInfo() {
    	return createApiMethod(GetSessionInfo.class);
    }

    @Override
    public Login Login() {
        return createApiMethod(Login.class);
    }
    
    @Override
    public Logout Logout() {
        return createApiMethod(Logout.class);
    }

	@Override
	public SetAccountInfo SetAccountInfo() {
		return createApiMethod(SetAccountInfo.class);
	}

	@Override
	public SetSchema SetSchema() {
		return createApiMethod(SetSchema.class);
	}


}
