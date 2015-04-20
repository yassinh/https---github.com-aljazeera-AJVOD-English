/**
 * Edit playlists dialog
 */

define([
 "app",
 "views/common/playlist-manage-dialog",
 "text!templates/playlists/edit-playlists.html"
], function(app, PlaylistManageDialog, tmplEditPlaylists) {
  "use strict";

  var SuperProto = PlaylistManageDialog.prototype;
  var instance;

  /**
   * @constructor
   * @param collection {Backbone collection}
   */
  return PlaylistManageDialog.extend({
    id: "edit-playlists-dialog",

    template: _.template(tmplEditPlaylists),

    events: _.extend({}, SuperProto.events, {
      "click .name": "_onPlaylistNameClick",
      "submit form": "_onRenameFormSubmit"
    }),

    destroy: function() {
      SuperProto.destroy.call(this);
      instance = null;
    },

    render: function(data) {
      var context = _.extend({
        limitReached: false,
        playlists: data.playlists,
        localization: app.localization
      }, this.options);

      this.$el.html(this.template(context));
      this.$el.append(this.views.spinner.el);
      this._createSortable();
      this.open();
    },

    close: function() {
      var $focused = this.$("input:focus");

      if(!$focused.length) {
        SuperProto.close.apply(this, arguments);
        return;
      }

      this._toggleRename($focused.closest("li"));
    },

    _bindEvents: function() {
      this.listenTo(app, "playlists:rename:success", this._onRenameSuccess);
      this.listenTo(app, "playlists:rename:exists", this._onRenameExists);
      this.listenTo(app, "playlists:reorder:success", this._onReorderComplete);
      this.listenTo(app, "playlists:reorder:error", this._onReorderError);
      this.listenTo(app, "playlists:reorder:error", this._onReorderComplete);
      this.listenTo(app, "playlists:delete:success", this._onRemoveSuccess);
    },

    _onContinueClick: function(event) {
      event.preventDefault();
      var $container = $(event.currentTarget).closest("li");
      app.trigger("playlists:delete", {
        playlistId: $container.data("id")
      });
    },

    _onPlaylistNameClick: function(event) {
      event.preventDefault();
      var $li = $(event.currentTarget).closest("li");
      this._toggleRename($li);
    },

    _onRenameFormSubmit: function(event) {
      event.preventDefault();
      var $target = $(event.currentTarget).closest("li");
      var $input = $target.find("input");

      if($.trim($input.val()).length === 0) {
        $input.addClass("error");
        return;
      }

      $input.addClass("has-spinner");
      app.trigger("playlists:rename", {
        playlistId: $target.data("id"),
        name: $target.find("input").val()
      });
    },

    _onRenameSuccess: function(params) {
      var $li = this.$("li").filter(function() {
        return $(this).data("id") === params.playlistId;
      });
      var $input = $li.find("input");
      $input.removeClass("has-spinner");
      $li.find(".name").text($input.val());
      this._toggleRename($li);
    },

    _onRenameExists: function() {
      this.$(".has-spinner").removeClass("has-spinner");
    },

    _onSortUpdate: function(event, ui) {
      var $sortable = this.$sortable;
      var ids = $sortable.find("li").map(function(i, elem) {
        return $(elem).data("id");
      }).get();

      $sortable.sortable("disable");

      app.trigger("playlists:reorder", {
        ids: ids
      });
    },

    _onReorderError: function() {
      this.$sortable.sortable("cancel");
    },

    _onReorderComplete: function() {
      this.$sortable.sortable("enable");
    },

    _onRemoveSuccess: function(params) {
      this._removeLi(params.playlistId);
      this.position();
      var showEmptyText = this.$("li").not(".empty").length === 0;
      this.$(".empty").toggleClass("h-hidden", !showEmptyText);
    },

    /**
     * Toggle the remain div visiblity
     * @param {element} elem - the link that was clicked on
     */
     _toggleRename: function($li) {
       var $form = $li.find("form");
       var flag = $form.hasClass("h-fade-in-run");
       $form.toggleClass("h-fade-in-run", !flag);
       $form.find("input")[!flag ? "focus" : "blur"]();
     }
  });
});
