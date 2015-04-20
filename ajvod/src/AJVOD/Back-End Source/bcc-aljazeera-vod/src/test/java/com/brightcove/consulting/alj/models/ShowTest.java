package com.brightcove.consulting.alj.models;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ShowTest {

	@Test
    public void testShow( ) {
        
		Show show = new Show();
        assertEquals( show.getNavigationType(), NavigationTypeEnum.Show );  
    }
}
