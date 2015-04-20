package com.brightcove.consulting.alj.models;

import java.util.List;

import com.brightcove.consulting.alj.utils.EncryptionHandler;
import com.brightcove.consulting.api.models.BrightcoveVideo;
import com.brightcove.consulting.api.models.ItemCollection;
import com.brightcove.consulting.api.request.BrightcovePageRequest;
import com.brightcove.consulting.api.response.BrightcoveResponse;
import com.brightcove.consulting.api.response.VideoResponse;
import com.brightcove.consulting.config.SpringContext;

/**
 * A view model that encompasses related information about the results of a search.
 * In particular, this provides query data used for subscriptions and any subscription
 * that is already associated with the query for the current user.  The queryJson
 * property is what is intended for display in the view and will conditional encrypt
 * the query based on configuration.
 *
 * @author ssayles
 */
public class SearchResult {

    private BrightcoveResponse<? extends ItemCollection> response;
    private List<? extends ItemCollection> results;
    private Subscription subscription;
    private int id;
	private EncryptionHandler encryptionHandler;

    @SuppressWarnings("unchecked")
	public SearchResult(BrightcoveResponse<? extends ItemCollection> response) {
        this.response = response;
        results = BrightcoveVideoWrapper.convert((List<BrightcoveVideo>) response.getBrightcoveItemCollection());
    }

    public List<? extends ItemCollection> getResults() {
        return results;
    }

    public Query getQuery() {
        if (response instanceof VideoResponse) {
            VideoResponse vResponse = (VideoResponse) response;
            BrightcovePageRequest request = vResponse.getBrightcovePageRequest();
            if (request != null) {
                return new Query(request, vResponse.getSortBy(), vResponse.getSortOrder());
            }
        }
        return null;
    }

    //{"command":"search_videos", "all":["someField:something", "someOtherField:something"] "sort_by":"REFERENCE_ID:DESC"}
    public String getQueryJson() {
        Query query = getQuery();
        if (query != null) {
        	EncryptionHandler eh = getEncryptionHandler();
        	if (eh.isUseEncryption()) {
        		return eh.encrypt(query.toJson());
        	} else {
        		return query.toJson();
        	}
        }
        return null;
    }
    
    public int getId() {
        return id;
    }

    public void setId(int Id) {
        this.id = Id;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }

    private EncryptionHandler getEncryptionHandler() {
    	if (encryptionHandler != null) {
    		return encryptionHandler;
    	}

    	encryptionHandler = (EncryptionHandler)
    			SpringContext.getBean("encryptionHandler");

    	return encryptionHandler;
    	
    }
}
