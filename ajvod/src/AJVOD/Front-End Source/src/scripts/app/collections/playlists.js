define([
  "app"
], function(app) {
  "use strict";

  return Backbone.Collection.extend({
    url: "/api/playlists",

    sync: function() {
      return $.ajax(this.url + "/get", {
        contentType: "application/json"
      }).fail(function (ex) {
         
        app.trigger("playlists:get:error");
      });
    },

    /**
     * Create a new playlist
     */
    create: function(params) {
      return $.ajax(this.url + "/create", {
        data: params.queryString,
        type: "post"
      }).always(function(resp, textStatus, jqxhr) {
        $.extend(params, resp);
        params.playlistId = params.id;
        console.log("test Playlist");
        console.log(resp);
        console.log(jqxhr);
        switch(resp.status || jqxhr.status) {
          case 201:
            app.trigger("playlists:create:success", params);
            break;
          case 200:
            app.trigger("playlists:create:exists", params);
            break;
          case 409:
            app.trigger("playlists:create:limitreached", params);
            break;
          case 401:
            app.trigger("playlists:create:unauthorized", params);
            break;
          default:
            app.trigger("playlists:create:error", params);
        }
      });
    },

    /**
     * Add a video to a playlist
     */
    add: function(params) {
      params.videoId = params.videoID;
      params.playlistId = params.playlistID;

      return $.ajax(this.url + "/add", {
        data: params,
        type: "post"
      }).always(function (resp, textStatus, jqxhr) {
          console.log("test Playlist");
          console.log(resp);
          console.log(jqxhr);
        switch(resp.status || jqxhr.status) {
          case 200:
            app.trigger("playlists:add:success", params);
            break;
          case 409:
            app.trigger("playlists:add:limitreached", params);
            break;
          default:
            app.trigger("playlists:add:error", params);
        }
      });
    },

    /**
     * Remove a video from a playlist
     * @param {number} videoID - the video ID to remove
     * @param {number} playlistID - the playlist ID
     */
    remove: function(params) {
      return $.ajax(this.url + "/remove", {
        data: params,
        type: "post"
      }).done(function() {
        app.trigger("playlists:remove:success", params);
      }).fail(function() {
        app.trigger("playlists:remove:error", params);
      });
    },

    /**
     * Delete a playlist
     * @param playlistID {number} the playlist ID to delete
     */
    destroy: function(params) {
      return $.ajax(this.url + "/delete", {
        data: params,
        type: "post"
      }).done(function() {
        app.trigger("playlists:delete:success", params);
      }).fail(function() {
        app.trigger("playlists:delete:error", params);
      });
    },

    rename: function(params) {
      return $.ajax(this.url + "/rename", {
        data: params,
        type: "post"
      }).always(function(resp, textStatus, jqxhr) {
        switch(resp.status || jqxhr.status) {
          case 200:
            app.trigger("playlists:rename:success", params);
            break;
          case 409:
            app.trigger("playlists:rename:exists", params);
            break;
          default:
            app.trigger("playlists:rename:error", params);
        }
      });
    },

    reorder: function(params) {
      return $.ajax(this.url + "/reorder", {
        data: { playlistIds: params.ids.join(",") },
        type: "post"
      }).done(function() {
        app.trigger("playlists:reorder:success", params);
      }).fail(function() {
        app.trigger("playlists:reorder:error", params);
      });
    }
  });

});
