package com.brightcove.consulting.api.models;

import org.apache.commons.lang.Validate;

/**
 * 
 * The sort parameter provides an opportunity to define
 * which sort property to use within Brightcove along with
 * the direction. The sort parameter will be passed with specific
 * Brightcove api requests. The YBTV framework has it's own
 * set of top level parameters defined in the types {@link SortProperty}
 * and {@link Direction}
 * 
 * @author woladehin
 *
 */
public class Sort {
   
    private SortProperty property;
    private Direction direction;

    
    /**
     * 
     * Default Constructor that expects the type of property/brightcove
     * field to sort by. The direction can be descending or ascending
     * based on the Brightcove api.
     * 
     * @param direction
     * @param property
     *
     */
    public Sort( Direction direction, SortProperty property ) {
        
        this.direction = direction;
        this.property = property;
        validateConstructor( );
    
    }
    
    
    public SortProperty getSortProperty( ) {
        return this.property;
    }
    
    
    public Direction getDirection( ) {
        return this.direction;
    }

    
    private void validateConstructor( ) {
       
        Validate.notNull(property, "Sort property can't be null");
        Validate.notNull(direction, "Sort direction can't be null");
    
    }
    
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((direction == null) ? 0 : direction.hashCode());
        result = prime * result
                + ((property == null) ? 0 : property.hashCode());
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Sort other = (Sort) obj;
        if (direction != other.direction)
            return false;
        if (property != other.property)
            return false;
        return true;
    }


    /**
     * 
     * Enumeration for sort directions. This is modified from spring's implementation
     * of sorting
     * 
     * @woladehin
     *
     */ 
    public static enum Direction {

        ASC, DESC, NATURAL;

        /**
         * Returns the {@link Direction} enum for the given {@link String} value.
         * 
         * @param value
         * @return
         */
        public static Direction fromString( String value ) {

            try {
                return Direction.valueOf( value.toUpperCase( ) );
            } catch ( Exception e ) {
                throw new IllegalArgumentException( String.format(
                        "Invalid value '%s' for sort direction given! Has to be either 'desc', 'asc', or 'natural' (case insensitive).", value), e);
            }
        }
    }
    
    
    public static enum SortProperty {

        DISPLAY_NAME, REFERENCE_ID, PLAYS_TOTAL, PLAYS_TRAILING_WEEK, START_DATE, PUBLISH_DATE, CREATION_DATE, MODIFIED_DATE, NATURAL;

        /**
         * Returns the {@link SortProperty} enum for the given {@link String} value.
         * 
         * @param value
         * @return
         */
        public static SortProperty fromString( String value ) {
            try {
                return SortProperty.valueOf( value.toUpperCase( ) );
            } catch ( Exception e ) {
                throw new IllegalArgumentException(String.format(
                        "Invalid value '%s' for sort direction given! Has to be either 'display_name','reference_id','plays_total'," +
                        "'plays_trailing_week','start_date','publish_date','creation_date','modified_date', 'natural' (case insensitive).", value), e);
            }
        }
    }
}
