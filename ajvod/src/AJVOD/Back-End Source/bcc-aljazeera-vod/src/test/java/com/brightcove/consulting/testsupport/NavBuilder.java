package com.brightcove.consulting.testsupport;

import com.brightcove.consulting.alj.models.Channel;

import com.brightcove.consulting.alj.models.Show;
import com.brightcove.consulting.alj.models.ShowGenre;
import com.brightcove.consulting.alj.models.Topic;

public class NavBuilder {
	public static NavWrapper<Channel> channel(String key) {
        return new NavWrapper<Channel>(Channel.class, key);
    }
    public static NavWrapper<Topic> topic(String key) {
        return new NavWrapper<Topic>(Topic.class, key);
    }
    public static NavWrapper<ShowGenre> showGenre(String key) {
        return new NavWrapper<ShowGenre>(ShowGenre.class, key);
    }
    public static NavWrapper<Show> show(String key) {
        return new NavWrapper<Show>(Show.class, key);
    }
}
