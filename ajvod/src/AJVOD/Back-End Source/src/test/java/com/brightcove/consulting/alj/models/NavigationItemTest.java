package com.brightcove.consulting.alj.models;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

public class NavigationItemTest {

    private TestNavItem navItem;

    @Before
    public void setUp() {
        navItem = new TestNavItem();
    }

    /**
     * When adding a child for a particular group
     */
    @Test
    public void itShouldThrowAnExceptionIfTheNavigationItemDoesNotHaveAKey() {
        try {
            NavigationItem child = new TestNavItem();

            navItem.addChild("foo", child);
            fail("Expected an exception when adding a child nav item with no key.");
        } catch (IllegalArgumentException e) {
            //ok
        }
    }

    /**
     * When adding a child for a particular group
     */
    @Test
    public void itShouldStoreThatChildInTheChildMapForThatGroup() {
        NavigationItem child = new TestNavItem();
        child.setKey("child1");

        navItem.addChild("foo", child);
        navItem.addChild("foo", child);
        
        assertNotNull(navItem.getChildren("foo").get(child.getKey()));
        assertSame(child, navItem.getChildren("foo").get(child.getKey()));
    }

    /**
     * When adding a child
     */
    @Test
    public void itShouldSetTheParentOfTheChildToTheInstance() {
        NavigationItem child = new TestNavItem();
        child.setKey("child1");

        navItem.addChild("foo", child);
        
        assertSame(navItem, child.getParent());        
    }
    
    private static class TestNavItem extends BaseNavigationItem {

        @Override
        protected String getDefaultCustomFieldName() {
            return null;
        }

        @Override
        public NavigationTypeEnum getNavigationType() {
            return null;
        }

        @Override
        public String getResourceCollection() {
            return null;
        }
        
        @Override
        public String getMenuDisplayName() {
        	return null;
        }

    }
}
