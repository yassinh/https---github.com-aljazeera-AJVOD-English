package com.brightcove.consulting.config;

public class GigyaConfig {

    private String language;
    private String token;
    private String channel;

    public GigyaConfig() {
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language.trim().toLowerCase();
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token.trim();
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel.trim();
    }


}
