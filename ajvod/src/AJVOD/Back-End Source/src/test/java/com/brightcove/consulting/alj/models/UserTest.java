package com.brightcove.consulting.alj.models;


import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class UserTest {


    @Test
    public void testAUserObjectCanBeDeepCloned() {
        User user = new User();
        user.setUID("abc");
        Map data = new HashMap();
        Map vod = new HashMap();
        data.put("vod", vod);
        vod.put("subscriptions", 1);
        user.setData(data);

        User clone = user.clone();
        clone.setUID("def");
        clone.vod().put("subscriptions", 2);

        assertEquals(1, user.vod().get("subscriptions"));
        assertEquals("abc", user.getUID());
        assertEquals(2, clone.vod().get("subscriptions"));
        assertEquals("def", clone.getUID());
    }

    @Test
    public void testOnlyValidUserPreferencesCanBeAdded() {
    	Map prefs = new HashMap();
    	prefs.put("lang", "en");
    	prefs.put("timezone", "America/Argentina/Tucuman");
    	prefs.put("country", "US");
    	User user = new User();
    	boolean updated = user.setPreferences(prefs);
    	assertTrue(updated);

    	Map preferences = user.preferences();
    	preferences.containsKey("lang");
    	assertEquals("en", preferences.get("lang"));
    	assertEquals("US", preferences.get("country"));
    	assertEquals("America/Argentina/Tucuman", preferences.get("timezone"));
    }

    @SuppressWarnings("unchecked")
	@Test
    public void testIfAnyPreferenceIsInvalidNoneWillBeAdded() {
    	Map prefs = new HashMap();
    	prefs.put("lang", "enfz");
    	prefs.put("timezone", "America/Argentina/Tucuman");
    	prefs.put("country", "US");
    	User user = new User();
    	boolean updated = user.setPreferences(prefs);
    	assertFalse(updated);
    	
    	Map preferences = user.preferences();
    	assertTrue(preferences.isEmpty());
    }

    @SuppressWarnings("unchecked")
	@Test
    public void testPreferenceForTimezoneMayBeGivenAsAStringOrIntegerAndWillBeSavedAsAnInteger() {
    	Map prefs = new HashMap();
    	User user = new User();

    	prefs.put("timezone", "America/Argentina/Tucuman");
    	boolean updated = user.setPreferences(prefs);
    	assertTrue(updated);
    	
    	Map preferences = user.preferences();
    	assertEquals("America/Argentina/Tucuman", preferences.get("timezone"));
    	
    	prefs.put("timezone", "Africa/Freetown");
    	updated = user.setPreferences(prefs);
    	assertTrue(updated);
    	preferences = user.preferences();
    	assertEquals("Africa/Freetown", preferences.get("timezone"));
    	
    }
}
