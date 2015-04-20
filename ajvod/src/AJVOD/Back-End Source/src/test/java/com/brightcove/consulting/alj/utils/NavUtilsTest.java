package com.brightcove.consulting.alj.utils;

import static com.brightcove.consulting.testsupport.NavBuilder.channel;
import static com.brightcove.consulting.testsupport.NavBuilder.show;
import static com.brightcove.consulting.testsupport.NavBuilder.showGenre;
import static com.brightcove.consulting.testsupport.NavBuilder.topic;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.apache.commons.collections.Predicate;
import org.junit.BeforeClass;
import org.junit.Test;

import com.brightcove.consulting.alj.models.Channel;
import com.brightcove.consulting.alj.models.NavigationItem;
import com.brightcove.consulting.alj.models.Topic;
import com.brightcove.consulting.testsupport.NavWrapper;

public class NavUtilsTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /**
     * Given a nested hierarchy of navigation items,
     * when calling findAncestor
     */
    @Test
    public void itShouldFindTheFirstAncestorThatMatchesTheGivenType() {
    	NavWrapper<Topic> subSubTopic = topic("subsub1");

    	NavigationItem parent =
    		channel("parent")
    			.add( topic("1")
    				.add( topic("sub1")
    					.add( subSubTopic))).instance();

        NavigationItem result = NavUtils.findAncestor(subSubTopic.instance(), Channel.class);
        assertNotNull(result);
        assertSame(parent, result);
    }

    @Test
    public void findDescendantShouldReturnTheFirstMatchingNode() {
        NavigationItem parent = 
            channel("1").name("Foo")
                .add( topic("t1").name("documentaries"))
                .add( showGenre("sg1").name("scifi")
                    .add( show("farscape").name("Farscape")
                    		.add( show("foo").name("Foo1")))
                    .add( show("st").name("Star Trek")
                    		.add( show("foo").name("Foo")))
                ).instance();


        Predicate predicate = new Predicate() {
            public boolean evaluate(Object object) {
                return ((NavigationItem)object).getKey() == "foo";
            }
        };
        NavigationItem result = NavUtils.findDescendant(parent, predicate);
        assertNotNull(result);
        assertEquals("foo", result.getKey());
        assertEquals("Foo1", result.getName());
    }

}
