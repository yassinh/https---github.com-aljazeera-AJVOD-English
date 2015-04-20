package com.brightcove.consulting.alj.tags;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.jstl.core.ConditionalTagSupport;

import com.brightcove.consulting.alj.models.ModelConstants;
import com.brightcove.consulting.alj.models.Playlist;
import com.brightcove.consulting.alj.models.User;
import com.brightcove.consulting.api.models.BrightcoveVideo;

public class IfPlaylistVideoTag extends ConditionalTagSupport {

    private BrightcoveVideo video;
	private String var;
    
    public IfPlaylistVideoTag() {
        super();
        init();
    }

    public void release() {
        super.release();
        init();
    }


    protected boolean condition() throws JspTagException {
    	User user = (User) pageContext.findAttribute(ModelConstants.USER);
    	if (user != null && video != null) {
    		Playlist playlist = user.findPlaylistWithVideo(video);
    		if (playlist != null) {
    			pageContext.setAttribute(var, playlist, PageContext.PAGE_SCOPE);
    			return true;
    		}
    	}
    	pageContext.removeAttribute(var, PageContext.PAGE_SCOPE);
    	return false;
    }


    public void setVar(String var) {
    	this.var = var;
    }

    public void setVideo(BrightcoveVideo video) {
        this.video = video;
    }

    // resets internal state
    private void init() {
        video = null;
        var = "playlist";
    }
}
