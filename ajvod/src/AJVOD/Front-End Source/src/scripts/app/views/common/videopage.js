/**
 * This page extends the base page view and layers on some extra
 * functionality that most pages need, like subscriptions, big screen,
 * etc.
 */
define([
  "app",
  "views/common/page",
  "views/common/subscriptions/mediator",
  "views/common/playlists/mediator",
  "views/common/bigscreendialog",
  "routers/router"
], function(
  app,
  PageView,
  SubscriptionsMediatorView,
  PlaylistsMediatorView,
  BigScreenDialogView,
  Router
) {
  "use strict";

  return PageView.extend({
    events: _.extend({}, PageView.prototype.events, {
      "click .do-big-screen": "_navigateBigScreen",
      "click .do-time-machine": "_navigateTimeMachine"
    }),

    initialize: function() {
      PageView.prototype.initialize.call(this);
      this._initRouter();
      this._initItemTooltips();
      this._initTitleTooltips();
    },

    _initRouter: function() {
      this.router = app.router = new Router();
      this.router.on("route", this._onRouteChange, this);
      this.router.on("route:bigscreen", this._initBigScreen, this);
      this.router.on("route:timemachine", this._initTimeMachine, this);
      Backbone.history.start();
    },

    _initSubviews: function() {
      PageView.prototype._initSubviews.call(this);

      this.views.subscriptions = new SubscriptionsMediatorView({
        el: document.body
      });
      this.views.playlists = new PlaylistsMediatorView({
        el: document.body
      });
    },

    _navigateBigScreen: function(event) {
      event.preventDefault();

      var containerSelectors = ".carousel, .carousel-large";
      var id = document.body.id;

      if(id === "topics") {
        containerSelectors = ".topic";
      } else if(id === "topic" || id === "search") {
        containerSelectors = "#main";
      }

      var $target = $(event.currentTarget);
      var carouselId = $target.data("carousel-id") || $target.closest(containerSelectors).data("carousel-id");

      if(!carouselId) {
        console.error('Tried to enter big screen without an id');
        this.router.navigate("", true);
        return;
      }

      this.router.navigate("big-screen/" + carouselId, {
        trigger: true
      });
    },

    _navigateTimeMachine: function(event) {
      event.preventDefault();
      var id = event.currentTarget.getAttribute("data-video-id");

      if(!id) {
        console.error('Tried to enter time machine without an id');
        this.router.navigate("", true);
        return;
      }

      this.router.navigate("time-machine/" + id, {
        trigger: true
      });
    },

    // Close big screen / time machine when navigating off their hash
    _onRouteChange: function(route) {
      if(route !== "bigscreen" && this.views.bigscreen) {
        this.views.bigscreen.close();
      }
      if(route !== "timemachine" && this.views.timemachine) {
        this.views.timemachine.close();
      }
    },

    _initBigScreen: function(id) {
      // Find the video items that we'll display in big screen mode
      var $root = $('[data-carousel-id="' + id + '"]').not("a");
      var $items = $root.find(".item, .item-search");

      if(!$items.length) {
        console.error("Routed to big screen but cannot find the carousel", id);
        return;
      }

      // Try to find a title for big screen
      var title = $root.find(".heading .title").text();
      if(!title || document.body.id === "search") {
        title = $root.find("h1").text();
      }

      // Create the view with the items
      this.views.bigscreen = new BigScreenDialogView({
        $items: $items,
        currentIndex: 0,
        title: title
      });

      this.listenToOnce(this.views.bigscreen, "destroy", function() {
        delete this.views.bigscreen;
      });
    },

    _initTimeMachine: function() {
      // This is a no-op on purpose. Pages that use time machine
      // should overwrite this.
    }

  });

});
