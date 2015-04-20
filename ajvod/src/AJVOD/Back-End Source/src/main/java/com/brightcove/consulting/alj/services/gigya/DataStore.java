package com.brightcove.consulting.alj.services.gigya;



/**
 * Factory for gigya accounts related operations.
 *
 * @author ssayles
 *
 */
@GigyaService("ds")
public interface DataStore {

    SetSchema SetSchema();

    GetSchema GetSchema();

    Store Store();

    Search Search();

    Get Get();

    Delete Delete();

    interface Delete extends ApiMethod {
        @Required
        void setOid(String oid);
        @Required
    	void setType(String type);
        
        void setUID(String uid);
    }

    interface Search extends ApiMethod {
    	@Required
    	void setQuery(String query);
    }

    interface Store extends ApiMethod {
    	@Required
    	void setData(Object data);
    	@Required
    	void setType(String type);
    	@Required
    	void setOid(String oid);
    	
    	void setUid(String uid);

    	void setUpdateBehavior(String updateBehavior);
    }

    interface Get extends ApiMethod {
    	@Required
    	void setOid(String oid);
    	@Required
    	void setType(String type);

    	void fields(String fields);

    	void setUid(String uid);
    }
    
    interface GetSchema extends ApiMethod {
    	@Required
    	void setType(String type);
    }

    interface SetSchema extends ApiMethod {
    	@Required
    	void setType(String type);
    	@Required
    	void setDataSchema(Object dataSchema);
    }
}
