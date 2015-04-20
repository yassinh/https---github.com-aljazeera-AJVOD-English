package com.brightcove.consulting.api.models;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class BrightcoveCustomFieldTest {

    @Test
    public void testBasicCustomField( ) {
        CustomField brightcoveCustomField = new CustomField( "name","key" , BrightcoveLogic.ALL);
        assertEquals( "The Brightcove custom field group does not match", brightcoveCustomField.getBrightcoveParameter(),"name:key" );
        assertEquals( "The brightcove logic does not match",brightcoveCustomField.getBrightcoveLogic(), BrightcoveLogic.ALL );
    }
    
    
    @Test( expected=IllegalArgumentException.class )
    public void testNullCustomFieldName( ) {
        new CustomField( "customFieldInternalName", null , BrightcoveLogic.ALL );
    }
    
    
    @Test( expected=IllegalArgumentException.class )
    public void testNullCustomFieldKey( ) {
        new CustomField( null, "customFieldValue", BrightcoveLogic.ALL );
    }
    
    
    @Test( expected=IllegalArgumentException.class )
    public void testNulLogicField( ) {
        new CustomField( "customFieldInternalName","customFieldValue", null );
    }
    
    
}
