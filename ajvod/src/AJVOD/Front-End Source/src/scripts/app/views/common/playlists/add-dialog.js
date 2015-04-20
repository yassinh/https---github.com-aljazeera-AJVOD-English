define([
  "app",
  "views/common/playlists/create-dialog",
  "text!templates/playlists/add-playlist.html"
], function(app, CreateDialogView, tmplAddPlaylist) {
  "use strict";

  /**
   * Extend the create dialog since this dialog
   * also has create functionality.
   *
   * @constructor
   * @param videoID {Number} video ID
   */
  return CreateDialogView.extend({
    id: "playlists-add-dialog",

    template: _.template(tmplAddPlaylist),

    events: _.extend({}, CreateDialogView.prototype.events, {
      "click ul a": "_onPlaylistClick"
    }),

    destroy: function() {
      CreateDialogView.prototype.destroy.apply(this, arguments);

      if(this.fetchDfd) {
        this.fetchDfd.abort();
      }
    },

    _bindEvents: function() {
      CreateDialogView.prototype._bindEvents.call(this);
      this.listenTo(app, "playlists:rename:exists", this._hideInputSpinner);
    },

    _render: function() {
      var context = {};
      context.playlists = this.options.playlists;
      context.videoID = this.options.videoID;
      context.localization = app.localization;
      context.reachedMax = context.playlists.length === app.consts.MAX_PLAYLISTS;
      this.$el.html(this.template(context));
      this._cacheElems();
    },

    _onPlaylistClick: function(event) {
      event.preventDefault();
      var $target = $(event.currentTarget);

      app.trigger("playlists:add", {
        videoID: this.options.videoID,
        playlistID: $target.data("playlist-id"),
        playlistName: $target.text(),
        videoName: this.options.videoName
      });
    },

    /**
     * Ajax methods
     */
    _onFetchFail: function() {
      app.trigger("notify", {
        style: "error",
        title: app.localization.playlist_add_error_text
      });
    }
  });

});
