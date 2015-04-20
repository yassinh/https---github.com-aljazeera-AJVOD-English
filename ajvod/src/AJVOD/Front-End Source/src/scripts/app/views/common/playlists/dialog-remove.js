/**
 * Common remove dialog for both subscriptions
 * playlists. This one powers playlists, subscriptions
 * extends this one.
 */

define([
 "app",
 "views/common/dialog",
 "text!templates/playlists/dialog-remove.html"
], function(app, DialogView, tmplRemove) {
  "use strict";

  return DialogView.extend({
    className: "remove-playlist-dialog",

    id: "remove-playlist-dialog",

    template: _.template(tmplRemove),

    defaults: $.extend({}, DialogView.prototype.defaults, {
      closeWhenOthersOpen: true,
      width: 600
    }),

    events: _.extend({}, DialogView.prototype.events, {
      "click .continue": "_onContinueClick",
      "click .cancel": "_onCancelClick"
    }),

    initialize: function() {
      DialogView.prototype.initialize.call(this);
      this.render();
      this.open();
    },

    render: function() {
      console.log('Rendering unsubscribe dialog', this.options);
      var context = _.extend({}, this.options);
      context.playlistName = context.playlistName || "";
      context.localization = app.localization;
      this.$el.html(this.template(context));
    },

    /**
     * DOM event handlers
     */

    _onCancelClick: function(event) {
      event.preventDefault();
      this.close();
    },

    _onContinueClick: function(event) {
      event.preventDefault();

      // only include options in the params that we want
      var params = {
        videoId: this.options.videoId,
        playlistId: this.options.playlistId,
        videoName: this.options.videoName,
        playlistName: this.options.playlistName
      };

      app.trigger("playlists:remove", params);
    }
  });
});
