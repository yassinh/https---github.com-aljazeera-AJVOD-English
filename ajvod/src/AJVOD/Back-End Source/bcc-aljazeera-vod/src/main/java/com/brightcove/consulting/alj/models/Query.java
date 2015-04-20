package com.brightcove.consulting.alj.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.brightcove.commons.catalog.objects.enumerations.SortByTypeEnum;
import com.brightcove.commons.catalog.objects.enumerations.SortOrderTypeEnum;
import com.brightcove.consulting.api.models.BrightcoveLogic;
import com.brightcove.consulting.api.models.BrightcovePair;
import com.brightcove.consulting.api.models.CustomField;
import com.brightcove.consulting.api.models.SearchTerm;
import com.brightcove.consulting.api.request.BrightcovePageRequest;
import com.brightcove.consulting.api.request.SearchApiPageRequest;


/**
 * A Query represents the information used to perform searches against the
 * backend read api. This provides a way to easily marshal between JSON and
 * BrightcovePageRequest representations.
 * 
 * Currently, much of this is geared toward the 'search' api only.
 */
public class Query {

    private BrightcovePageRequest request;
    private SortByTypeEnum sortType;
    private SortOrderTypeEnum sortOrder;

    private JSONObject queryJson;
    private String command;
    private List<String> all;
    private List<String> not;
    private List<String> any;
    private String sortBy;
    @SuppressWarnings("rawtypes")
    private Map search;
    private String channelId;

    public Query(String queryJson) throws JSONException {
        this.queryJson = new JSONObject(queryJson);
        init();
    }

    public Query(BrightcovePageRequest request, SortByTypeEnum sortType, SortOrderTypeEnum sortOrder) {
        this.request = request;
        this.sortType = sortType;
        this.sortOrder = sortOrder;
        try {
            init();
        } catch (JSONException e) {
            // we shouldn't be parsing any json here
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void init() throws JSONException {
        if (queryJson != null) {
            if (queryJson.has("command")) {
                this.command = queryJson.getString("command");
            }
            this.all = this.getConditionsFromJson(BrightcoveLogic.ALL);
            this.not = this.getConditionsFromJson(BrightcoveLogic.NOT);
            this.any = this.getConditionsFromJson(BrightcoveLogic.ANY);
            if (queryJson.has("sortBy")) {
                this.sortBy = queryJson.getString("sortBy");
            }
            if (queryJson.has("search")) {
                JSONObject jsonObject = queryJson.getJSONObject("search");
                this.search = new HashMap();
                if (jsonObject.has("term")) {
                    String term = jsonObject.getString("term");
                    this.search.put("term", term);
                }
                if (jsonObject.has("logic")) {
                    String logic = jsonObject.getString("logic");
                    this.search.put("logic", logic);
                }
            }
        } else {
            if (request instanceof SearchApiPageRequest) { 
                this.command = "search_videos";
                SearchTerm search = ((SearchApiPageRequest)request).getSearch();
                if (search != null) {
                    this.search = new HashMap();
                    this.search.put("term", search.getSearchKey());
                    if (search.getBrightcoveLogic() != null) {
                        this.search.put("logic", search.getBrightcoveLogic().toString().toLowerCase());
                    }
                }
            }
            this.all = this.getConditions(BrightcoveLogic.ALL);
            this.not = this.getConditions(BrightcoveLogic.NOT);
            this.any = this.getConditions(BrightcoveLogic.ANY);
            this.sortBy = sortType.toString() + ":" + sortOrder.toString();
        }
    }

    public String getCommand() {
        return this.command;
    }

    public List<String> getAll() {
        return this.all;
    }
    
    public List<String> getNot() {
    	return this.not;
    }

    public List<String> getAny() {
    	return this.any;
    }

    public String getSortBy() {
        return this.sortBy;
    }

    @SuppressWarnings("rawtypes")
    public Map getSearch() {
        return this.search;
    }

    public String getChannelId() {
        if (this.channelId == null) {
            if (this.all != null) {
                for (String field : this.all) {
                    if (field != null) {
                        String[] values = field.split(":");
                        if (values[0].equals("channel")) {
                            if (values.length > 1) {
                                this.channelId = values[1];
                                break;
                            }
                        }
                    }
                }
            }
            // try looking in the 'any' criterion
            if (this.channelId == null && this.any != null) {
                for (String field : this.all) {
                    if (field != null) {
                        String[] values = field.split(":");
                        if (values[0].equals("channel")) {
                            if (values.length > 1) {
                                this.channelId = values[1];
                                break;
                            }
                        }
                    }
                }
            }
            
        }

        return this.channelId;
    }

    public String toJson() {
        JSONObject json = toJsonObject();
        return json.toString();
    }

    public String toJson(int indenfactor) throws JSONException {
        JSONObject json = toJsonObject();
        return json.toString(indenfactor);
    }

    public JSONObject toJsonObject() {
        JSONObject json = new JSONObject(this);
        if (json.has("channelId")) {
            json.remove("channelId");
        }
        return json;
    }

    public SearchApiPageRequest toPageRequest(int page, int size) {
        List<BrightcovePair> pairs = new ArrayList<BrightcovePair>();
        if (getAll() != null) {
            List<String> all = getAll();
            addPairs(pairs, all, BrightcoveLogic.ALL);
        }
        if (getNot() != null) {
            List<String> not = getNot();
            addPairs(pairs, not, BrightcoveLogic.NOT);
        }
        if (getAny() != null) {
            List<String> any = getAny();
            addPairs(pairs, any, BrightcoveLogic.ANY);
        }
        SearchTerm searchTerm = null;
        if (this.search != null) {
            String logic = (String) this.search.get("logic");
            searchTerm = new SearchTerm((String) this.search.get("term"), BrightcoveLogic.valueOf(logic.toUpperCase()));
        }

        if (searchTerm != null && !pairs.isEmpty()) {
            return new SearchApiPageRequest(page, size, searchTerm, pairs);
        } else if (searchTerm != null) {
            return new SearchApiPageRequest(page, size, searchTerm);
        } else {
            return new SearchApiPageRequest(page, size, pairs);
        }
    }

    private void addPairs(List<BrightcovePair> pairs, List<String> values, BrightcoveLogic logic) {
        for (String string : values) {
            String[] split = string.split(":");
            if (split.length < 2) {
                // bad criterion
                continue;
            }
            String key = split[0];
            String value = split[1];
            CustomField field = new CustomField(key, value, BrightcoveLogic.valueOf(logic.toString().toUpperCase()));
            pairs.add(field);
            
        }
    }

    private List<String> getConditions(BrightcoveLogic logic) throws JSONException {
        List<BrightcovePair> brightcovePairs = ((SearchApiPageRequest)request).getBrightcovePairs(logic);
        if (brightcovePairs != null && !brightcovePairs.isEmpty()) {
            List<String> list = new ArrayList<String>();
            for (BrightcovePair pair : brightcovePairs) {
                list.add(pair.getBrightcoveParameter());
            }
            return list;
        }
        return null;
    }
    private List<String> getConditionsFromJson(BrightcoveLogic logic) throws JSONException {
        String key = logic.toString().toLowerCase();
        if (queryJson.has(key)) {
            JSONArray jsonArray = queryJson.getJSONArray(key);
            int length = jsonArray.length();
            if (length > 0) {
                List<String> list = new ArrayList<String>();
                for (int i = 0; i < length; i ++) {
                    if (!jsonArray.isNull(i)) {
                        String criterion = jsonArray.getString(i);
                        list.add(criterion);
                    }
                }
                if (!list.isEmpty()) {
                    return list;
                }
            }
        }
        return null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((all == null) ? 0 : all.hashCode());
        result = prime * result + ((any == null) ? 0 : any.hashCode());
        result = prime * result + ((command == null) ? 0 : command.hashCode());
        result = prime * result + ((not == null) ? 0 : not.hashCode());
        result = prime * result + ((search == null) ? 0 : search.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Query other = (Query) obj;
        if (all == null) {
            if (other.all != null)
                return false;
        } else if (!matchingLists(all, other.all))
            return false;
        if (any == null) {
            if (other.any != null)
                return false;
        } else if (!matchingLists(any, other.any))
            return false;
        if (command == null) {
            if (other.command != null)
                return false;
        } else if (!command.equals(other.command))
            return false;
        if (not == null) {
            if (other.not != null)
                return false;
        } else if (!matchingLists(not, other.not))
            return false;
        if (search == null) {
            if (other.search != null)
                return false;
        } else {
            if (other.search == null)
                return false;

            String term1 = (String) this.search.get("term");
            String term2 = (String) other.search.get("term");
            if (!StringUtils.equalsIgnoreCase(term1, term2)) {
                return false;
            }
            String logic1 = (String) this.search.get("logic");
            String logic2 = (String) other.search.get("logic");
            if (!StringUtils.equalsIgnoreCase(logic1, logic2)) {
                return false;
            }
        }
        return true;
    }

    
    private boolean matchingLists(List<String> list1, List<String> list2) {
        if (list1 != null && list2 != null) {
            if (list1.size() == list2.size()) {
                List intersection = ListUtils.intersection(list1, list2);
                return intersection.size() == list1.size();
            }
            return false;
        }
        // they match if they're both null
        return (list1 == null && list2 == null);
    }

}