package com.brightcove.consulting.alj.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.cglib.core.CollectionUtils;
import net.sf.cglib.core.Transformer;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.brightcove.consulting.alj.models.Query;
import com.brightcove.consulting.alj.utils.EncryptionHandler;
import com.brightcove.consulting.api.models.BrightcoveVideo;
import com.brightcove.consulting.api.query.FindVideosBySearchFields;
import com.brightcove.consulting.api.request.SearchApiPageRequest;
import com.brightcove.consulting.api.response.BrightcoveResponse;

@Controller
public class VideoSearch extends BaseController {


	@Autowired
	EncryptionHandler encryptionHandler;

	@NoAuthorization
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @RequestMapping("/api/mediaquery")
    public @ResponseBody Map searchVideos(
            HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value="query", required = true) String queryJson,
            @RequestParam(value="pageSize", required = false, defaultValue = "10") Integer size,
            @RequestParam(value="encrypted", defaultValue = "true", required = false) boolean encrypted) throws JSONException {

    	if (encrypted) {
    		queryJson = encryptionHandler.decrypt(queryJson);
    	}
        Query query = new Query(queryJson);
	if (size < 1) { size = 10; }
        SearchApiPageRequest pageRequest = query.toPageRequest(0, size);

        BrightcoveResponse<BrightcoveVideo> searchResponse = new FindVideosBySearchFields(readToken).query(pageRequest);

        List<BrightcoveVideo> videos = searchResponse.getBrightcoveItemCollection();

        Map<String, Object> json = json(HttpStatus.OK);
        
        Collection videosJson = CollectionUtils.transform(videos, new Transformer() {
            @SuppressWarnings("unchecked")
            @Override
            public Object transform(Object value) {
                BrightcoveVideo video = (BrightcoveVideo) value;
                Map json = new HashMap();
                json.put("id", video.getId());
                json.put("lastModifiedDate", video.getLastModifiedDate());
                return json;
            }
        });

        json.put("videos", new ArrayList(videosJson));
        return json;
    }

}
