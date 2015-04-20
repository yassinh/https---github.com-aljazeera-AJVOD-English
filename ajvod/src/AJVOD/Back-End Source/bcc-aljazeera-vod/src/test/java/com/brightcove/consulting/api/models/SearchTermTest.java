package com.brightcove.consulting.api.models;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

public class SearchTermTest {

    @Test
    public void testSearchKeyIsNotNull( ) {
        SearchTerm searchTerm = new SearchTerm( "This is my awesome search" );
        assertEquals( "Search key should be equal", searchTerm.getSearchKey( ), "This is my awesome search" );
    }
    
    @Test
    public void testDefaultSearchLogicIsNotNull( ) {
        SearchTerm searchTerm = new SearchTerm( "This is my awesome search" );
        assertEquals( "Search key should be equal", searchTerm.getBrightcoveLogic( ), SearchTerm.DEFAULT_BRIGHTCOVE_SEARCH_LOGIC );
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testNullSearchValue( ) {
        new SearchTerm( null );
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testNullSearchValueWithBoolean( ) {
        new SearchTerm( null,BrightcoveLogic.ALL );
    }

    @Test(expected=IllegalArgumentException.class)
    public void testNullBooleanWithSearch( ) {
        new SearchTerm( "search", null );
    }
    
    
    @Test
    public void testSearchParameters( ) {
        
        SearchTerm searchTerm = new SearchTerm( "This is my awesome search" );
        allWordsAreReturned( searchTerm.getSearchParameters( ) );
        assertEquals("Search key should be equal", searchTerm.getSearchKey( ), "This is my awesome search");
    }

    /**
     * Compares each individual element in the list with the expected term that it should match
     * 
     * @param searchParameters an array list of split search words
     */
    private void allWordsAreReturned(List<String> searchParameters) {
        assertEquals("First word in Search Parameters should be 'This' ", searchParameters.get(0), "This");
        assertEquals("First word in Search Parameters should be 'is' ", searchParameters.get(1), "is");
        assertEquals("First word in Search Parameters should be 'my' ", searchParameters.get(2), "my");
        assertEquals("First word in Search Parameters should be 'awesome' ", searchParameters.get(3), "awesome");
        assertEquals("First word in Search Parameters should be 'search' ", searchParameters.get(4), "search");
    }
}
