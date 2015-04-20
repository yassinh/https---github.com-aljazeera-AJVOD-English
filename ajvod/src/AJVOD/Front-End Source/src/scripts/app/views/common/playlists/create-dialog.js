/**
 * Create a playlist dialog
 * !!! The add dialog extends this one.
 */

define([
  "app",
  "views/common/dialog",
  "text!templates/playlists/create-playlist.html"
], function(app, DialogView, tmplCreatePlaylist) {
  "use strict";

  var SuperProto = DialogView.prototype;

  return DialogView.extend({
    id: "playlists-create-dialog",

    template: _.template(tmplCreatePlaylist),

    defaults: $.extend({}, SuperProto.defaults, {
      width: 500
    }),

    events: _.extend({}, SuperProto.events, {
      "submit form": "_onFormSubmit",
      "click form a": "_onSubmitButtonClick",
      "keyup input": "_onInputKeyup"
    }),

    /**
     * @constructor
     * @param id {Number} video ID
     */
    initialize: function() {
      SuperProto.initialize.call(this);

      app.trigger("spinner:show");

      this.fetchXhr = this._fetch().done(_.bind(function(data) {
        setTimeout(_.bind(function() {
          this._render(data.playlists);
          this.open();
        }, this), 300);
      }, this)).fail(function() {
        app.trigger("notify", {
          style: "error",
          title: app.localization.generic_error_text
        });
      }).always(function() {
        setTimeout(function() {
          app.trigger("spinner:hide");
        }, 300);
      });
    },

    destroy: function() {
      if(this.fetchXhr && this.fetchXhr.state() === "pending") {
        this.fetchXhr.abort();
      }

      SuperProto.destroy.call(this);
    },

    _bindEvents: function() {
      SuperProto._bindEvents.call(this);
      this.listenTo(app, "playlists:create:exists", this._hideInputSpinner);
    },

    _render: function(playlists) {
      var context = {};
      context.localization = app.localization;
      context.reachedMax = playlists.length === app.consts.MAX_PLAYLISTS;
      this.$el.html(this.template(context));
      this._cacheElems();
      this.$input.focus();
    },

    _cacheElems: function() {
      this.$form = this.$("form");
      this.$submit = this.$(".submit");
      this.$input = this.$('input[type="text"]');
    },

    _validate: function() {
      return $.trim(this.$input.val()).length;
    },

    _fetch: function() {
      return this.collection.fetch();
    },

    /**
     * Event handlers
     */

    _onSubmitButtonClick: function(event) {
      event.preventDefault();
      $(event.currentTarget).closest("form").submit();
    },

    _onFormSubmit: function(event) {
      event.preventDefault();

      // If the form is valid, request that the playlist be created.
      if(this._validate()) {
        this.$input.addClass("has-spinner");

        app.trigger("playlists:create", {
          videoId: this.options.videoID,
          videoName: this.options.videoName,
          queryString: this.$form.serialize(),
          playlistName: this.$input.val()
        });
      } else {
        this.$input.focus().removeClass("has-spinner");
      }
    },

    _hideInputSpinner: function() {
      this.$(".has-spinner").removeClass("has-spinner");
    },

    _onInputKeyup: function() {
      this.$submit.toggleClass("is-disabled", !this._validate());
    }
  });

});

