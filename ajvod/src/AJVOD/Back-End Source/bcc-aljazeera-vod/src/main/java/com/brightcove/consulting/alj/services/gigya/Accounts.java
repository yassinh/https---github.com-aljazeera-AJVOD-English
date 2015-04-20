package com.brightcove.consulting.alj.services.gigya;



/**
 * Factory for gigya accounts related operations.
 *
 * @author ssayles
 *
 */
@GigyaService("accounts")
public interface Accounts {

	GetAccountInfo GetAccountInfo();

	GetSchema GetSchema();

	GetSessionInfo GetSessionInfo();

    Login Login();
    
    Logout Logout();

    SetAccountInfo SetAccountInfo();

    SetSchema SetSchema();

    /**
     * Requires uid or regToken. 
     */
//    @CacheConfig(ttl=1800)
    interface GetAccountInfo extends ApiMethod {
    	void setUid(String uid);
    	void setRegToken(String token);
    	void setInclude(String includes);
    }

    interface GetSchema extends ApiMethod {
    }

    interface GetSessionInfo extends ApiMethod, RequireUid {
    	@Required
    	void setProvider(String providerName);
    }
    
    interface Login extends ApiMethod {
    }

    interface Logout extends ApiMethod, RequireUid {
    }

    interface SetAccountInfo extends ApiMethod, RequireUid {
        void setData(Object object);
        void setOldPassword(String oldPassword);
        void setNewPassword(String newPassword);
        void setUsername(String username);
        void setAddLoginEmails(String emails);
        void setRemoveLoginEmails(String emails);
        void setSecretQuestion(String question);
        void setSecretAnswer(String answer);
        void setIsActive(boolean active);
    }

    interface SetSchema extends ApiMethod {
    	void setProfileSchema(Object profileSchema);
    	void setDataSchema(Object dataSchema);
    }
}
