package com.brightcove.consulting.alj.models;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ShowGenreTest {

	@Test
    public void testShowGenre( ) {
        
		ShowGenre showGenre = new ShowGenre();
        assertEquals( showGenre.getNavigationType(), NavigationTypeEnum.ShowGenre );  
    }
}
