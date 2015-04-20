package com.brightcove.consulting.alj.services.gigya.impl;

import org.springframework.stereotype.Component;


import com.brightcove.consulting.alj.services.gigya.DataStore;

@Component(value="gigyaDataStoreApi")
public class DataStoreImpl extends DynamicService implements DataStore {

    @Override
    public GetSchema GetSchema() {
        return createApiMethod(GetSchema.class);
    }

	@Override
	public SetSchema SetSchema() {
		return createApiMethod(SetSchema.class);
	}

	@Override
	public Store Store() {
		return createApiMethod(Store.class);
	}

	@Override
	public Search Search() {
		return createApiMethod(Search.class);
	}

	@Override
	public Get Get() {
		return createApiMethod(Get.class);
	}

	@Override
	public Delete Delete() {
		return createApiMethod(Delete.class);
	}


}
