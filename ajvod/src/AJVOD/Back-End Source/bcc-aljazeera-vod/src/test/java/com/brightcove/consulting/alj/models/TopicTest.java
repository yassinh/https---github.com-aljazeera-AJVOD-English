package com.brightcove.consulting.alj.models;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TopicTest {

	@Test
    public void testTopic( ) {
        
		Topic topic = new Topic();
        assertEquals( topic.getNavigationType(), NavigationTypeEnum.Topic );  
    }
}
