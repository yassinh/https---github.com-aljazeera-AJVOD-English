package com.brightcove.consulting.alj.services.gigya;

import com.brightcove.consulting.alj.services.ServiceException;
import com.gigya.socialize.GSResponse;

public interface ApiMethod {

    /**
     * Submits the request asynchronously.
     *
     * @param listener
     */
//    TODO: implement async handling for caching first
//    void submit(GSResponseListener listener);

    GSResponse submit() throws ServiceException;

    GSResponse submit(int time) throws ServiceException;

}
