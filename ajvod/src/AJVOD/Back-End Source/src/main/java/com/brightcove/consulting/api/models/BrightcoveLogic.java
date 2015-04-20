package com.brightcove.consulting.api.models;

/**
 * 
 * The Brightcove Api has different boolean logical values. 
 * Each filter may require an additional way of how that
 * filter is applied either 'ALL' videos must contain a
 * filter, 'ANY' video may contain it but not required, or
 * 'NOT' have the given filter for a video.
 * 
 * @author woladehin
 *
 */
public enum BrightcoveLogic {
    ALL, ANY, NOT
}
