package com.brightcove.consulting.alj.models;

import java.util.ArrayList;

import java.util.List;

import org.json.JSONException;
import org.junit.Test;

import com.brightcove.consulting.api.models.BrightcoveLogic;
import com.brightcove.consulting.api.models.BrightcovePair;
import com.brightcove.consulting.api.models.CustomField;
import com.brightcove.consulting.api.models.SearchTerm;
import com.brightcove.consulting.api.request.SearchApiPageRequest;

import static com.brightcove.commons.catalog.objects.enumerations.SortByTypeEnum.PLAYS_TOTAL;
import static com.brightcove.commons.catalog.objects.enumerations.SortByTypeEnum.START_DATE;
import static com.brightcove.commons.catalog.objects.enumerations.SortOrderTypeEnum.DESC;
import static org.junit.Assert.*;


public class QueryTest {


    @Test
    public void testAQueryCanBeConstructedFromValidJson() {
        String json = "{'command':'search_videos', 'all': ['a:1','b:2','c:3'], 'sortBy': '"+PLAYS_TOTAL+":"+DESC+"', 'search':{'term':'seria','logic':'any'}}";
        try {
            Query query = new Query(json);
            assertEquals("search_videos", query.getCommand());
            assertNotEmpty(query.getAll());
            assertNull(query.getNot());
            assertNull(query.getAny());
            assertNotNull(query.getSearch());
        } catch (JSONException e) {
            fail("Json parsing should have succeeded.");
        }
    }

    @Test
    public void testAQueryCanBeConstructedFromABrightcovePageRequest() throws JSONException {
        List<BrightcovePair> pairs = new ArrayList<BrightcovePair>();
        pairs.add(new CustomField("a", "1", BrightcoveLogic.ANY));
        pairs.add(new CustomField("b", "1", BrightcoveLogic.ALL));
        
        SearchTerm term = new SearchTerm("seria", BrightcoveLogic.ALL);
        SearchApiPageRequest request = new SearchApiPageRequest(1, 10, term, pairs);
        Query query = new Query(request, PLAYS_TOTAL, DESC);

        assertEquals("search_videos", query.getCommand());
        assertNotEmpty(query.getAll());
        assertNull(query.getNot());
        assertNotEmpty(query.getAny());
        assertNotNull(query.getSearch());
    }

    @Test
    public void testQuerriesWithTheSameValuesShouldBeEqual() throws JSONException {
        String json = "{'command':'search_videos', 'all': ['a:1','b:2','c:3'], 'sortBy': '"+PLAYS_TOTAL+":"+DESC+"', 'search':{'term':'seria','logic':'any'}}";
        String json2 = "{'command':'search_videos', 'all': ['a:1','b:2'], 'sortBy': '"+PLAYS_TOTAL+":"+DESC+"', 'search':{'term':'seria','logic':'any'}}";
        Query query1 = new Query(json);
        Query query2 = new Query(json);
        Query query3 = new Query(json2);

        assertEquals(query1, query2);
        assertFalse(query1.equals(query3));

    }

    @Test
    public void testQuerriesShouldNotBeEqualBasedOnSort() throws JSONException {
        String json1 = "{'command':'search_videos', 'all': ['a:1','b:2','c:3'], 'sortBy': '"+PLAYS_TOTAL+":"+DESC+"', 'search':{'term':'seria','logic':'any'}}";
        String json2 = "{'command':'search_videos', 'all': ['a:1','b:2','c:3'], 'sortBy': '"+START_DATE+":"+DESC+"', 'search':{'term':'seria','logic':'any'}}";
        Query query1 = new Query(json1);
        Query query2 = new Query(json2);

        assertEquals(query1, query2);
    }

    @Test
    public void testQuerriesShouldBeEqualEvenIfFieldCriterionAreInDifferentOrder() throws JSONException {
        String json1 = "{'command':'search_videos', 'all': ['c:3','b:2','a:1'], 'sortBy': '"+PLAYS_TOTAL+":"+DESC+"', 'search':{'term':'seria','logic':'any'}}";
        String json2 = "{'command':'search_videos', 'all': ['a:1','b:2','c:3'], 'sortBy': '"+START_DATE+":"+DESC+"', 'search':{'term':'seria','logic':'any'}}";
        Query query1 = new Query(json1);
        Query query2 = new Query(json2);
        
        assertEquals(query1, query2);
    }

    @Test
    public void testQueriesShouldBeEqualRegardlessOfCase() throws JSONException {
        String json1 = "{'command':'search_videos', 'all': ['c:3','b:2','a:1'], 'sortBy': '"+PLAYS_TOTAL+":"+DESC+"', 'search':{'term':'syria','logic':'any'}}";
        String json2 = "{'command':'search_videos', 'all': ['a:1','b:2','c:3'], 'sortBy': '"+START_DATE+":"+DESC+"', 'search':{'term':'Syria','logic':'any'}}";
        Query query1 = new Query(json1);
        Query query2 = new Query(json2);

        assertEquals(query1, query2);
        
    }

    private void assertNotEmpty(List list) {
        assertNotNull(list);
        assertFalse(list.isEmpty());
    }
}
