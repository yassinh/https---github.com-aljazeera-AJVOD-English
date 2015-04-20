/*
 * Playlists mediator
 *
 * This view acts as the mediator in between the add, edit, and create
 * dialogs and the underlying DOM. Dialogs sent commands in the form of
 * events to the mediator, the mediator talks to the collection to perform
 * any ajax, then a) fires events back out to the dialogs, and b) updates
 * the underlying page that the mediator is on.
 */

define([
  "app",
  "collections/playlists",
  "views/common/gigya",
  "views/common/playlists/add-dialog",
  "views/common/playlists/edit-dialog",
  "views/common/playlists/create-dialog",
  "views/common/playlists/delete-dialog",
  "views/common/playlists/dialog-remove"
], function(
  app,
  PlaylistsCollection,
  GigyaView,
  AddDialogView,
  EditDialogView,
  CreateDialogView,
  DeleteDialogView,
  RemoveDialogView
) {
  "use strict";

  var BaseView = app.views.BaseView;

  // Map dialog views so we can dynamically invoke them
  var dialogs = {
    create: CreateDialogView,
    destroy: DeleteDialogView,
    add: AddDialogView,
    edit: EditDialogView,
    remove: RemoveDialogView
  };

  return BaseView.extend({
    el: document.body,

    // Event listeners
    events: {
      "click .do-playlist-edit": "_onEditPlaylistClick",
      "click .do-playlist-create": "_onCreatePlaylistClick",
      "click .do-playlist-delete": "_onDeleteClick",
      "click .do-playlist-add": "_onAddToPlaylistClick",
      "click .do-playlist-remove": "_onRemoveFromPlaylistClick"
    },

    initialize: function() {
      BaseView.prototype.initialize.apply(this, arguments);
      this.collection = new PlaylistsCollection();
      this.dialogs = {};
    },

    _bindEvents: function() {
      this.listenTo(app, "tabs:activate", this._onTabsActivate);

      this.listenTo(app, "playlists:get:error", this._onFetchError);

      this.listenTo(app, "playlists:remove", this._onRemove);
      this.listenTo(app, "playlists:remove:error", this._onRemoveError);
      this.listenTo(app, "playlists:remove:success", this._onRemoveSuccess);

      this.listenTo(app, "playlists:create", this._onCreate);
      this.listenTo(app, "playlists:create:error", this._onCreateError);
      this.listenTo(app, "playlists:create:exists", this._onCreateExists);
      this.listenTo(app, "playlists:create:limitreached", this._onCreateLimitReached);
      this.listenTo(app, "playlists:create:success", this._onCreateSuccess);

      this.listenTo(app, "playlists:delete", this._onDelete);
      this.listenTo(app, "playlists:delete:error", this._onDeleteError);
      this.listenTo(app, "playlists:delete:success", this._onDeleteSuccess);

      this.listenTo(app, "playlists:add", this._onAdd);
      this.listenTo(app, "playlists:add:error", this._onAddError);
      this.listenTo(app, "playlists:add:success", this._onAddSuccess);
      this.listenTo(app, "playlists:add:limitreached", this._onAddLimitReached);

      this.listenTo(app, "playlists:rename", this._onRename);
      this.listenTo(app, "playlists:rename:error", this._onRenameError);
      this.listenTo(app, "playlists:rename:exists", this._onRenameExists);

      this.listenTo(app, "playlists:reorder", this._onReorder);
      this.listenTo(app, "playlists:reorder:error", this._onReorderError);

      this.listenTo(app, "api:addToPlaylist", function(videoID, videoName) {
        this._addToPlaylist({ videoID: videoID, videoName: videoName });
      });
    },

    _addToPlaylist: function(video) {
      this._requireLogin(_.bind(function() {
        this.collection.fetch().done(_.bind(function(data) {
          this._createDialog("add", {
            videoID: video.videoID,
            videoName: video.videoName,
            playlists: data.playlists,
            collection: this.collection
          });
        }, this));
      }, this));
    },

    /*****************************
     * DOM event handlers
     *****************************/

    _onAddToPlaylistClick: function(event) {
      event.preventDefault();

      var $item = $(event.currentTarget).closest(".item");
      var videoID = $item.data("video-id");

      if(!videoID) {
        app.trigger("notify", {
          style: "error",
          title: "Unable to find an ID for this video"
        });

        return;
      }

      this._addToPlaylist({
        videoID: videoID,
        videoName: $item.find(".title").text()
      });
    },

    /**
     * Remove video from playlist
     */
    _onRemoveFromPlaylistClick: function(event) {
      event.preventDefault();

      this._requireLogin(function() {
        var $item = $(event.currentTarget).closest(".item");

        var params = {
          videoId: $item.data("video-id"),
          videoName: $item.find(".title").text(),
          playlistId: $item.data("playlist-id"),
          playlistName: $item.data("playlist-name")
        };

        this._createDialog("remove", params);
      });
    },

    /**
     * Delete a playlist
     */
    _onDeleteClick: function(event) {
      event.preventDefault();

      this._requireLogin(function() {
        var $link = $(event.currentTarget);
        var playlistId = $link.closest("[data-playlist-id]").data("playlist-id");
        var title = this._createTitle($link);

        this._createDialog("destroy", {
          playlistId: playlistId,
          title: title
        });
      });
    },

    _onCreatePlaylistClick: function(event) {
      event.preventDefault();

      this._requireLogin(function() {
        this._createDialog("create", {
          collection: this.collection
        });
      });
    },

    _onEditPlaylistClick: function(event) {
      event.preventDefault();

      this._requireLogin(function() {
        this._createDialog("edit", {
          collection: this.collection
        });
      });
    },

    /*****************************
     * Event emitter handlers
     *****************************/

    _onFetchError: function() {
      this._notify("error", app.localization.playlist_fetch_error_text);
    },

    /**
     * Fires when a tab changes. Currently this is only on
     * the your channel page.
     */
    _onTabsActivate: function() {
      console.warn('New tab activated; closing all tabs');
      this._closeAllDialogs();
    },

    _onRemove: function(params) {
      this.collection.remove(params);
    },
    _onRemoveSuccess: function(params) {
      this._closeDialog("remove");
      this._toggleIcon(params.videoId);
    },
    _onRemoveError: function() {
      this._notify("error", app.localization.playlist_remove_error_text);
    },


    _onAdd: function(params) {
      this.collection.add(params);
    },
    _onAddSuccess: function(params) {
      this._closeDialog("add");
      this._toggleIcon(params.videoId, params);
      this._notify(
        app.localization.playlist_add_success
          .replace('{0}', params.videoName)
          .replace('{1}', params.playlistName)
      );
    },
    _onAddLimitReached: function(params) {
      this._notify(app.localization.playlist_add_max);
    },
    _onAddError: function() {
      this._notify("error", app.localization.playlist_add_error_text);
    },


    _onCreate: function(params) {
      this.collection.create(params);
    },
    _onCreateSuccess: function(params) {
      this._closeDialog("add", "create");

      // If the playlist was created through the add dialog..
      if(params.videoId) {
        this._toggleIcon(params.videoId, params);

        this._notify(
          app.localization.playlist_add_success
            .replace('{0}', params.videoName)
            .replace('{1}', params.playlistName)
        );
      }
    },
    _onCreateError: function() {
      this._notify("error", app.localization.playlist_create_error_text);
    },
    _onCreateExists: function() {
      this._notify(app.localization.playlist_create_exists);
    },
    _onCreateLimitReached: function() {
      this._notify(app.localization.playlist_max);
    },


    _onDelete: function(params) {
      this.collection.destroy(params);
    },
    _onDeleteSuccess: function() {
      this._closeDialog("destroy");
    },
    _onDeleteError: function() {
      this._notify("error", app.localization.playlist_delete_error_text);
    },


    _onRename: function(params) {
      this.collection.rename(params);
    },
    _onRenameError: function(params) {
      this._notify("error", app.localization.playlist_rename_error_text);
    },
    _onRenameExists: function() {
      this._notify(app.localization.playlist_rename_exists);
    },


    _onReorder: function(params) {
      this.collection.reorder(params);
    },
    _onReorderError: function() {
      this._notify("error", app.localization.playlist_reorder_error_text);
    },

    /*****************************
     * Utils
     *****************************/

    _createTitle: function($elem) {
      return $elem.closest(".carousel, [data-query]").find(".heading .title").text();
    },

    _notify: function(style, title) {
      if(arguments.length === 1) {
        title = style;
        style = "success";
      }

      app.trigger("notify", { style: style, title: title });
    },

    /**
     * Close all dialogs that are currently open. The `this.dialogs` property
     * only contains references to open dialogs.
     */
    _closeAllDialogs: function() {
      _.each(this.dialogs, function(dialog) {
        dialog.close();
      });
    },

    /**
     * Close a dialog by name.
     * @param {string} name - name of the dialog to lcose
     */
    _closeDialog: function() {
      for(var i = 0; i < arguments.length; i++) {
        var name = arguments[i];

        if(this.dialogs[name]) {
          this.dialogs[name].close();
        }
      }
    },

    /**
     * Helper function to create a dialog, and automatically clean it
     * up when it closes.
     * @param {string} name - name of dialog (add, edit, create, or destroy)
     * @param {object} options - options to send to the dialog
     */
    _createDialog: function(name, options) {
      if(this.dialogs[name]) {
        console.warn('Tried to create dialog %s but it already exists');
        return;
      }

      options = _.extend(options || {}, {
        localization: app.localization
      });

      console.log('Creating dialog %s with params', name, options);
      var dialog = this.dialogs[name] = new dialogs[name](options);

      this.listenToOnce(dialog, "destroy", _.bind(function() {
        delete this.dialogs[name];
      }, this));
    },

    _requireLogin: function(callback) {
      GigyaView.isLoggedIn().done(_.bind(function(flag) {
        if(!flag) {
          GigyaView.showLoginUI();
          return;
        }

        callback.call(this);
      }, this));
    },

    _toggleIcon: function(videoID, dataParams) {
      var $item = this.$(".item").filter(function() {
        return $(this).data("videoId") === videoID;
      });

      $item.find(".do-playlist-add").toggleClass("h-hidden");
      $item.find(".do-playlist-remove").toggleClass("h-hidden");

      if(dataParams) {
        $item.data("playlist-id", dataParams.playlistId);
        $item.data("playlist-name", dataParams.playlistName);
      }
    }

  });

});
