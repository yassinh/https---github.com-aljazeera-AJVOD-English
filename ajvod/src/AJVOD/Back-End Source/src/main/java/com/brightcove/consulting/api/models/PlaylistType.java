package com.brightcove.consulting.api.models;

/**
 * Brightcove Api has several playlist types. Each type
 * describes whether this video is manual or dynamic.
 * If the video is dynamic, the type of sort for that video
 * is described as its type.
 * 
 * @author woladehin
 *
 */
public enum PlaylistType {
    EXPLICIT, OLDEST_TO_NEWEST, NEWEST_TO_OLDEST, ALPHABETICAL, PLAYS_TOTAL, PLAYS_TRAILING_WEEK
}
