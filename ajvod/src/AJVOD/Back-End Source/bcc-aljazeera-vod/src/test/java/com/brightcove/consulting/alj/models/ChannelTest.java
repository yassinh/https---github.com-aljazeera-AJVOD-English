package com.brightcove.consulting.alj.models;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ChannelTest {

	@Test
    public void testChannel( ) {
        
		Channel channel = new Channel();
        assertEquals( channel.getNavigationType(), NavigationTypeEnum.Channel );  
    }
}
