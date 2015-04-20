package com.brightcove.consulting.alj.tags;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import com.brightcove.consulting.alj.models.ModelConstants;
import com.brightcove.consulting.alj.models.Playlist;
import com.brightcove.consulting.alj.models.User;
import com.brightcove.consulting.api.models.BrightcoveVideo;


public class GetPlaylistTag extends TagSupport {


    //*********************************************************************
    // 'Private' state (implementation details)

    private String var;			// stores EL-based property
    private BrightcoveVideo video;			// stores EL-based property


    //*********************************************************************
    // Constructor

    public GetPlaylistTag() {
        super();
        init();
    }


    //*********************************************************************
    // Tag logic

    // evaluates expression and chains to parent
    public int doStartTag() throws JspException {
    	User user = (User) pageContext.findAttribute(ModelConstants.USER);
    	if (user != null && video != null) {
    		Playlist playlist = user.findPlaylistWithVideo(video);
    		if (playlist != null) {
    			pageContext.setAttribute(var, playlist, PageContext.PAGE_SCOPE);
    		} else {
    			pageContext.removeAttribute(var, PageContext.PAGE_SCOPE);
    		}
    	}
    	return super.doStartTag();
    }


    // Releases any resources we may have (or inherit)
    public void release() {
        super.release();
        init();
    }


    //*********************************************************************
    // Accessor methods

    public void setValue(String var) {
        this.var = var;
    }

    public void setVideo(BrightcoveVideo video) {
        this.video = video;
    }


    //*********************************************************************
    // Private (utility) methods

    // (re)initializes state (during release() or construction)
    private void init() {
    	var = "playlist";
    	video = null;
    }

}
