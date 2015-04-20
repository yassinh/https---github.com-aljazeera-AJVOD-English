package com.brightcove.consulting.alj.models;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.brightcove.consulting.api.models.BrightcoveVideo;

public class Playlist extends DataStoreObject implements Cloneable {

	private static final long serialVersionUID = -6725590654457629082L;

	private static final Logger logger = LoggerFactory.getLogger(Playlist.class);
	
	private String name;

	private Date createdAt;

	private Date updatedAt;

	private String language;

	private String locale;

	private String uid;

	private List<Video> videos;
	
	private List<BrightcoveVideoWrapper> brightcoveVideos;

	private Integer position;

	public Playlist() {
	}

	public Playlist(String oid) {
		setOid(oid);
	}

	public Playlist(String oid, String uid) {
		setOid(oid);
		setUid(uid);
	}

	public String getId() {
		return getOid();
	}

	public String getName() {
		return name;
	}
	

	public void setName(String name) {
		this.name = name;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}
	
	public List<BrightcoveVideoWrapper> getBrightcoveVideos() {
		if (brightcoveVideos != null) {
			return brightcoveVideos;
		}
		brightcoveVideos = new ArrayList<BrightcoveVideoWrapper>();
		return brightcoveVideos;
	}
	
	public void setBrightcoveVideos(List<BrightcoveVideoWrapper> videos) {
		this.brightcoveVideos = videos;
	}

	public List<Video> getVideos() {
		if (videos != null) {
			return videos;
		}
		videos = new ArrayList<Video>();
		return videos;
	}

	public void setVideos(List<Video> videos) {
		this.videos = videos;
	}

	/**
	 * 
	 * @param video
	 * @return <code>false</code> if a video with the same id exists.
	 */
	public boolean addVideo(Video video) {
		List<Video> videos = getVideos();
		for (Video _video : videos) {
			if (StringUtils.equals(video.getId(), _video.getId())) {
				return false;
			}
		}
		getVideos().add(video);
		return true;
	}

	public Video getVideo(String videoId) {
		List<Video> videos = getVideos();
		for (Video video : videos) {
			if (StringUtils.equals(video.getId(), videoId)) {
				return video;
			}
		}
		return null;
	}

	public boolean removeVideo(String videoId) {
		Video video = this.getVideo(videoId);
		if (video != null) {
			return this.getVideos().remove(video);
		}
		return false;
	}


	/**
	 * Reorders the subscriptions collection based on the given array of ids.
	 * This will skip any id that doesn't match an existing subscription, and
	 * will ensure that all current subscriptions are returned regardless of
	 * what ids are given.
	 *
	 * @param ids Ordered list of subscription ids.
	 * @return <code>true</code> if the subscriptions list was modified.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
    public boolean orderVideos(final String[] ids) {
	    boolean modified = false;

	    List<Video> videos = this.getVideos();
	    // new re-ordered list
	    List<Video> newVideos = new ArrayList<Video>();
	    
	    // for each id given
	    for (int i = 0; i < ids.length; i++) {
	        String id = ids[i].trim();
	        // find the matching subscription with the same id
	        // and add it to our new ordered list
	        for (int j = 0; j < videos.size(); j++) {
                Video video = videos.get(j);
                if (StringUtils.equals(video.getId(), id)) {
                    newVideos.add(video);
                    // if the indexes are different, then we actually did change
                    // the order
                    if (i != j) {
                        modified = true;
                    }
                    break;
                }
            }
        }
	    
	    // if we didn't actually change anything, then there's leave
	    // the subscriptions list alone.
	    if (!modified) {
	        return false;
	    }

	    // if anything leftover, tack onto the end
	    // if the original list and new ordered list are the same length, then
	    // don't bother
	    if (newVideos.size() != videos.size()) {
	        // find all the videos in the original list that don't have
	        // and id in the given ids list
	        Collection missingVideos = CollectionUtils.selectRejected(videos, new Predicate() {
                public boolean evaluate(Object object) {
                    Video video = (Video) object;
                    if (ArrayUtils.indexOf(ids, video.getId()) >= 0) {
                        return true;
                    }
                    return false;
                }
            });
	        if (!missingVideos.isEmpty()) {
	            newVideos.addAll(missingVideos);
	        }
	    }
	    
	    // if we got here, then something was actually changed so go ahead and
	    // update
	    this.videos = newVideos;
	    return true;
	}

	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer position) {
		this.position = position;
	}


	public Playlist clone() {
	    ObjectOutputStream oos = null;
	    ObjectInputStream ois = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            oos.writeObject(this);
            oos.flush();
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            ois = new ObjectInputStream(bis);
            Playlist clone = (Playlist) ois.readObject();
            return clone;
        } catch (IOException e) {
            logger.error("IOException occurred while cloning playlist " + this.getId(), e);
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            logger.error("IOException occurred while cloning playlist " + this.getId(), e);
            throw new RuntimeException(e);
        } finally {
            if (oos != null) {
                try {oos.close();} catch (IOException e) {}
            }
            if (ois != null) {
                try {ois.close();} catch (IOException e) {}
            }
        }
	}
}
