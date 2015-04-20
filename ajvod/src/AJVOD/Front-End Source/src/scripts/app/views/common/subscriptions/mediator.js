/*
 * Subscriptions mediator
 *
 * This view acts as a mediator between the DOM and the
 * manage subscriptions / unsubscribe dialog. All three
 * talk to each other when events. For example, if the
 * user unsubscribes from a channel in the manage dialog,
 * the manage dialog sends an event to let the controller
 * know. The controller performs the action and emits another
 * event once it's completed successfully. The manage
 * dialog waits for that event to be emitted and then
 * updates its UI.
 */

define([
  "app",
  "collections/subscriptions",
  "views/common/gigya",
  "views/common/subscriptions/dialog-manage",
  "views/common/subscriptions/dialog-remove"
], function(
  app,
  SubscriptionsCollection,
  GigyaView,
  SubscriptionsDialog,
  UnsubscribeDialog
) {
  "use strict";

  var BaseView = app.views.BaseView;

  // Map dialog views so we can dynamically invoke them
  var dialogs = {
    manage: SubscriptionsDialog,
    remove: UnsubscribeDialog
  };

  /**
   * Creates a subscription title.
   * @param {jQuery Object} $container - root element with the data-* attrs
   * @return {string} title for the subscription
   */
  var createTitle = function($container) {
    // Start on the container itself. Look for a title data-attribute, and if not
    // found continue inwards and look for a heading module with a title.
    var title = $container.data("title") || $container.find(".heading .title").text();
    var prefix = $container.data("prefix");

    return {
      title: title,
      prefix: prefix
    };
  };

  /**
   * @constructor
   * @param el {Element} - document.body
   */
  return BaseView.extend({

    // Event listeners
    events: {
      "click .edit-subscriptions": "_onEditSubscriptions",
      "click .do-unsubscribe": "_onUnsubscribeClick",
      "click .do-subscribe": "_onSubscribeClick"
    },

    initialize: function() {
      BaseView.prototype.initialize.apply(this, arguments);
      this.collection = new SubscriptionsCollection();
      this.dialogs = {};
    },

    _bindEvents: function() {
      this.listenTo(app, "subscriptions:remove", this._onUnsubscribeConfirm);
      this.listenTo(app, "subscriptions:remove:success", this._onUnsubscribeSuccess);
      this.listenTo(app, "subscriptions:remove:error", this._onUnsubscribeError);

      this.listenTo(app, "subscriptions:add:success", this._onSubscribeSuccess);
      this.listenTo(app, "subscriptions:add:error", this._onSubscribeError);
      this.listenTo(app, "subscriptions:add:exists", this._onSubscribeExists);
      this.listenTo(app, "subscriptions:add:limitreached", this._onSubscribeLimitReached);
      this.listenTo(app, "subscriptions:add:unauthorized", this._onSubscribeUnauthorized);

      this.listenTo(app, "subscriptions:reorder", this._onReorder);
      this.listenTo(app, "subscriptions:loginRequired", this._requestLogin);

      this.listenTo(app, "api:subscribe", this._onSubscribeByAPI);
      this.listenTo(app, "api:unsubscribe", this._onUnsubscribeByAPI);
      this.listenTo(app, "api:isSubscribed", this._onIsSubscribedByAPI);
    },

    /*
     * Event Listeners
     */

    // Called when an unsubscribe request comes in from one of the dialogs
    _onUnsubscribe: function(data) {
      this._unsubscribe(data);
      this._closeDialog("remove");
    },

    // When the "Edit Subscriptions" link is clicked on
    _onEditSubscriptions: function(event) {
      event.preventDefault();
      this._createDialog("manage");
    },

    /********************************
     * API methods
     * *****************************/

    _getSubscriptionLinksForVideoID: function(videoID) {
      var $item = $('.carousel-hook .item').filter(function() {
        return $(this).data("video-id") === videoID;
      }).first();

      if(!$item.length) {
        console.warn('Tried to subscribe through the API but the video is not on the page');
        return;
      }

      return $item.closest("section").find(".do-subscribe, .do-unsubscribe");
    },

    _onSubscribeByAPI: function(videoID, eventLoopId) {
      var $links = this._getSubscriptionLinksForVideoID(videoID);
      if($links && $links.length) {
        this.listenToOnce(app, "subscriptions:add:complete", function(resp) {
          // add was complete if the status is 201. otherwise it was an error of some kind.
          app.trigger("api:subscribe:complete", resp.status === 201, eventLoopId);
        });

        $links.filter(".do-subscribe").trigger("click");
      }
    },

    _onUnsubscribeByAPI: function(videoID, eventLoopId) {
      var $links = this._getSubscriptionLinksForVideoID(videoID);
      if($links && $links.length) {
        this.listenToOnce(app, "subscriptions:remove:complete", function(resp) {
          // add was complete if the status is 201. otherwise it was an error of some kind.
          app.trigger("api:unsubscribe:complete", false, eventLoopId);
        });
        $links.filter(".do-unsubscribe").trigger("click");
      }
    },

    _onIsSubscribedByAPI: function(videoID, eventLoopId) {
      var $links = this._getSubscriptionLinksForVideoID(videoID);
      var subscribed;

      if($links && $links.length) {
        subscribed = $links.filter(".do-subscribe").hasClass("h-hidden");
      }

      app.trigger("api:isSubscribed:complete", subscribed, eventLoopId);
    },

    /********************************
     * Unsubscribe actions
     * *****************************/

    // User clicked on a carousel unsubscribe link
    _onUnsubscribeClick: function(event) {
      event.preventDefault();

      var $elem = $(event.currentTarget);
      var $container = $elem.closest("[data-query]");
      var title = createTitle($container);
      var id = $container.data("id");

      // If there isn't an id error out
      if(!id) {
        console.error("Trying to unsubscribe with a null id; title is", title);
        this._showUnsubscribeError();
        return;
      }

      this._createDialog("remove", {
        title: title.title,
        id: id
      });
    },

    // Confirm unsubscribe
    _onUnsubscribeConfirm: function(data) {
      this.collection.removeSubscription(data.id);
    },

    _onUnsubscribeSuccess: function(data) {
      this._flipText(data.id);
      this._closeDialog("remove");
    },

    _onUnsubscribeError: function() {
      this._showUnsubscribeError();
    },

    /********************************
     * Subscribe actions
     * *****************************/

    // When a subscribe link is clicked on
    _onSubscribeClick: function(event) {
      event.preventDefault();

      if(this.dialogs.manage) {
        this.dialogs.manage.close();
      }

      // Show spinner
      this._showSpinner();

      // Perform action based upon whether or not the user is already logged in
      GigyaView.isLoggedIn().done(_.bind(function(isLoggedIn) {
        // Ask user to login if they're not already
        if(!isLoggedIn) {
          this._requestLogin();
          this._hideSpinner();
          return;
        }

        var $elem = $(event.currentTarget);
        var $container = $elem.closest("[data-query]");
        var query = $container.data("query");
        var title = createTitle($container);

        // Make sure there's a query attribute
        if(!query) {
          console.error("Trying to unsubscribe with a null query", $container);
          this._showSubscribeError();
          return;
        }

        this.collection.addSubscription({
          name: title.title,
          prefix: title.prefix,
          query: query,
          url: window.location.href
        });
      }, this));
    },

    _onSubscribeSuccess: function(params) {
      var msg;

      if(params.prefix) {
        msg = app.localization.subscriptions_subscribe_success_withcontext;
        msg = msg.replace('{0}', params.name);
        msg = msg.replace('{1}', params.prefix);
      } else {
        msg = app.localization.subscriptions_subscribe_success;
        msg = msg.replace('{name}', params.name);
      }

      app.trigger("notify", {
        title: msg,
        style: "success"
      });

      this._setID(params.query, params.id);
      this._flipText(params.id);
      this._hideSpinner();
    },

    _onSubscribeError: function() {
      this._showSubscribeError();
      this._hideSpinner();
    },

    _onSubscribeExists: function(params) {
      app.trigger("notify", {
        title: app.localization.subscriptions_already_exist.replace('{name}', params.name || "this channel")
      });

      this._setID(params.query, params.id);
      this._flipText(params.id);
      this._hideSpinner();
    },

    _onSubscribeLimitReached: function() {
      this._createDialog("manage", {
        limitReached: true
      });
      this._hideSpinner();
    },

    _onSubscribeUnauthorized: function() {
      this._requestLogin();
      this._hideSpinner();
    },

    /********************************
     * Sort actions
     * *****************************/

    _onReorder: function(params) {
      this.collection.reorder(params);
    },

    /****************************
     * utils
     ****************************/

    /**
     * Close a dialog by name.
     * @param {string} name - name of the dialog to lcose
     */
    _closeDialog: function(name) {
      if(this.dialogs[name]) {
        this.dialogs[name].close();
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

      options = options || {};
      options.collection = this.collection;
      var dialog = this.dialogs[name] = new dialogs[name](options);

      this.listenToOnce(dialog, "destroy", _.bind(function() {
        delete this.dialogs[name];
      }, this));
    },

    _showSpinner: function()  {
      app.trigger("spinner:show");
    },

    _hideSpinner: function() {
      app.trigger("spinner:hide");
    },

    _requestLogin: function() {
      GigyaView.showLoginUI();
    },

    // Flips the subscribe link to unsubscribe and vice versa
    _flipText: function(id) {
      var $links = $("[data-query]")
        .filter(function() { return $(this).data("id") === id; })
        .find(".do-subscribe, .do-unsubscribe");

      var $visible = $links.filter(".h-hidden").removeClass("h-hidden");
      $links.not($visible).addClass("h-hidden");
    },

    _setID: function(query, id) {
      var $container = $("[data-query]").filter(function() {
        return $(this).data("query") === query;
      }).data("id", id);
    },

    // Open an error message
    _showSubscribeError: function() {
      app.trigger("notify", {
        title: app.localization.subscription_save_error,
        style: "error"
      });

      this._hideSpinner();
    },

    _showUnsubscribeError: function() {
      app.trigger("notify", {
        title: app.localization.subscription_unsubscribe_error,
        style: "error"
      });

      this._hideSpinner();
    }
  });

});
