/**
 * This is the base manage dialog for both subscriptions and playlists.
 * It handles the sorting, unsubscribe interactions, and other common
 * functions between the two manage dialogs.
 */

define([
 "app",
 "views/common/dialog",
 "views/common/spinner"
], function(app, DialogView, SpinnerView) {
  "use strict";

  var SuperProto = DialogView.prototype;

  return DialogView.extend({
    className: "playlist-manage-dialog",

    defaults: $.extend({}, SuperProto.defaults, {
      closeWhenOthersOpen: true,
      width: 600,
      fixed: true
    }),

    events: _.extend({}, SuperProto.events, {
      "click .remove": "_onRemoveClick",
      "click .cancel": "_onCancelClick",
      "click .continue": "_onContinueClick"
    }),

    initialize: function() {
      SuperProto.initialize.call(this);

      // fetch data and render.
      this._fetch()
        .fail(_.bind(this._onFetchFail, this))
        .done(_.bind(this.render, this));
    },

    destroy: function() {
      if(this.$sortable && this.$sortable.data("uiSortable")) {
        console.log('Destroy sortable');
        this.$sortable.sortable("destroy");
      }
      if(this.fetchXhr && this.fetchXhr.state() === "pending") {
        this.fetchXhr.abort();
      }

      SuperProto.destroy.call(this);
    },

    _initSubviews: function() {
      SuperProto._initSubviews.call(this);

      this.views.spinner = new SpinnerView({
        minDisplayFor: 200,
        style: "dark"
      });
    },

    /**
     * Event handlers
     */

    _onRemoveClick: function(event) {
      event.preventDefault();
      this._toggleConfirm(event.currentTarget, true);
    },

    _onCancelClick: function(event) {
      event.preventDefault();
      this._toggleConfirm(event.currentTarget, false);
    },

    _onContinueClick: function() {
      // no-op: overwrite this
    },

    /**
     * Collection APIs
     */

    _fetch: function() {
      app.trigger("spinner:show");

      this.fetchXhr = this.collection.fetch().done(_.bind(function(data) {
        this.data = data.playlists || data.subscriptions;
      }, this)).always(function() {
        app.trigger("spinner:hide");
      });

      return this.fetchXhr;
    },

    _onFetchFail: function() {
      // no-op: overwrite this
    },

    /**
     * Sorting stuff
     */

    _createSortable: function() {
      this.$sortable = this.$el.find("ul").sortable({
        containment: this.el,
        axis: "y",
        cursor: "move",
        placeholder: "ui-sortable-placeholder",
        forcePlaceholderSize: true,
        items: "> li:not(.empty)"
      });

      this.$sortable.on("sortstart", _.bind(this._onSortStart, this));
      this.$sortable.on("sortstop", _.bind(this._onSortStop, this));
      this.$sortable.on("sortupdate", _.bind(this._onSortUpdate, this));
    },

    _onSortStart: function(event, ui) {
      ui.item.addClass("ui-sortable-active");
    },

    _onSortStop: function(event, ui) {
      ui.item.removeClass("ui-sortable-active");
    },

    _onSortUpdate: function() {
      // No-op - override this
    },

    /**
     * Confirmation stuff
     */

    _toggleConfirm: function(elem, show) {
      $(elem)
        .closest("li")
        .find(".unsubscribe")
        .toggleClass("h-fade-in-run", show);
    },

    /**
     * Utils
     */

    // Removes an <li> node from the list and updates the sortable
    // appropriately.
    _removeLi: function(id) {
      this.$el.find("li").filter(function(i, elem) {
        return $(elem).data("id") === id;
      }).remove();

      this.$sortable.sortable("refresh");
    }

  });
});
