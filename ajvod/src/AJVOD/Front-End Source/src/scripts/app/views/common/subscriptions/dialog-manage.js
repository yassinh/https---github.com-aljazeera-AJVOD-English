/**
 * Manage subscriptions dialog
 */

define([
 "app",
 "views/common/playlist-manage-dialog",
 "text!templates/subscriptions/dialog-manage.html"
], function(app, PlaylistManageDialog, tmplManageSubscriptions) {
  "use strict";

  var SuperProto = PlaylistManageDialog.prototype;
  var instance;

  var ManageSubscriptionsDialogView = PlaylistManageDialog.extend({
    id: "manage-subscriptions-dialog",

    template: _.template(tmplManageSubscriptions),

    destroy: function() {
      SuperProto.destroy.call(this);
      instance = null;
    },

    render: function(data) {
      console.log('Rendering dialog context', data);

      var context = _.extend({
        limitReached: false,
        data: data
      }, this.options, app.localization);

      this.$el.html(this.template(context));
      this.$el.prepend(this.views.spinner.el);
      this._createSortable();
      this.open();
    },

    _bindEvents: function() {
      this.listenTo(app, "subscriptions:remove:success", this._onUnsubscribeSuccess);
      this.listenTo(app, "subscriptions:reorder:success", this._onReorderSuccess);
      this.listenTo(app, "subscriptions:reorder:error", this._onReorderError);
      this.listenTo(app, "yourchannel:carouselRemoved", this.position);
    },

    _onContinueClick: function(event) {
      event.preventDefault();
      this.views.spinner.show();
      var $container = $(event.currentTarget).closest("li");
      console.log('emitting remove', $container.data());
      app.trigger("subscriptions:remove", $container.data());
    },

    _onSortUpdate: function(event, ui) {
      var $sortable = this.$sortable;
      var spinner = this.views.spinner;
      var ids = $sortable.find("li").map(function(i, elem) {
        return $(elem).data("id");
      }).get();

      spinner.show();
      $sortable.sortable("disable");
      app.trigger("subscriptions:reorder", { ids: ids });
    },

    _onUnsubscribeSuccess: function(data) {
      this._removeLi(data.id);
      this._removeFromCache(data.id);
      this.views.spinner.hide();
      this.position();

      // Re-render if there aren't any subscriptions left
      if(!this.data.length) {
        this.options.limitReached = false;
        this.render(this.data);
      }
    },

    _onReorderSuccess: function() {
      this.$sortable.sortable("enable");
      this.views.spinner.hide();
    },

    _onReorderError: function() {
      app.trigger("notify", {
        title: app.localization.order_subscriptions_failed_text,
        style: "error"
      });

      this.$sortable.sortable("cancel");
      this.$sortable.sortable("enable");
      this.views.spinner.hide();
    },

    _onFetchFail: function(jqxhr) {
      // Tell the mediator that we need to login...
      if(jqxhr.status === 401) {
        app.trigger("subscriptions:loginRequired");

      // Otherwise toss a generic error
      } else {
        app.trigger("notify", {
          style: "error",
          title: app.localization.subscriptions_fetch_error_text
        });
      }
    },

    // utils

    // Removes an item from the cache
    _removeFromCache: function(id) {
      this.data = _.reject(this.data, function(obj) {
        return obj.id === id;
      });
    }
  });

  // Expose the function to create an instance - not the
  // view itself. That's how we keep this thing a singleton.
  return function(opts) {
    if(instance) instance.destroy();
    return (instance = new ManageSubscriptionsDialogView(opts));
  };

});
