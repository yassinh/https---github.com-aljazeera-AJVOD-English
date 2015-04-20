// This file is a common dependency for all other modules. Everything else
// should pull this in. App is responsible for setting up the global
// namespace and importing global dependencies that all other parts of the
// code will need.

define([
  "views/common/base",

  // Plugins/libs that we need on all pages
  "jquery",
  "jqueryraf",
  "underscore",
  "backbone",
  "carousel",
  "sticky"
], function(BaseView, api) {
  "use strict";

  var MONTHS = [
    "Jan", "Feb", "Mar", "Apr", "May", "June",
    "July", "Aug", "Sept", "Oct", "Nov", "Dec"
  ];

  function pad(value) {
    return value < 10 ?  ("0" + value) : value;
  }

  // Disable caching on all xhr requests. Especially important
  // for IE.
  $.ajaxSetup({ cache: false });

  // Create a shared namespace object and enable pub/sub on it
  var app = _.extend(window.app || {}, {
    views: {}
  }, window.app, Backbone.Events);

  // Global views. Define them on app so every other view won't
  // need to pull them in (keeps dependency list lean).
  app.views.BaseView = BaseView;

  // Environment vars
  var $html = $("html");

  app.context = {
    isRTL: false,
    lang: "en"
  };

  // Constants/config
  app.consts = {
    ACTIVE_CLASS: "is-active",
    DISABLED_CLASS: "is-disabled",
    GIGYA_COMMENTS_CATEGORY_ID: "dev-testing",
    GIGYA_LOGIN_SCREEN_SET_EN: "Login-web",
    GIGYA_LOGIN_SCREEN_SET_AR: "Login-web-ar",
    GIGYA_LOGIN_MOBILE_SCREEN_SET_EN: "Mobile-login",
    GIGYA_LOGIN_MOBILE_SCREEN_SET_AR: "Mobile-login-ar",
    GIGYA_EDIT_PROFILE_SCREEN_SET_EN: "Profile-web",
    GIGYA_EDIT_PROFILE_SCREEN_SET_AR: "Profile-web-ar",
    GIGYA_LOGIN_REGISTER_SCREEN: "gigya-register-screen",
    // The timestamp of the current Gigya session
    GIGYA_CURRENT_SESSION_CACHE_KEY: "alj-gigya-currts",
    // Cache key for the auto resume position
    AUTO_RESUME_CACHE_KEY: "alj-autoresume-position",
    // Used to cache video IDs as they're watched so recommendations can be made
    TABOOLA_CACHE_KEY: "alj-taboola-video",
    // Taboola API key
    TABOOLA_API_KEY: "4f48cc77757cdbff8fda5dd5ff56cd6da4fa1621",
    // Taboola pub key
    TABOOLA_PUBLISHER_ID: "al-jazeera",
    // Used to cache session details after a get request, so heartbeat calls
    // can be made with the same session details
    TABOOLA_SESSION_CACHE_KEY: "alj-taboola-session",
    // The id of the recommended video clicked on from the your channel page.
    // Used to send a heartbeat when the vid starts.
    TABOOLA_CLICKED_VIDEO_ID: "alj-taboola-vidid",
    // Used to cache the timestamp of the latest video
    LATEST_SUBSCRIPTION_VIDEO_DATE: "alj-subscription-date",
    OVERLAY_FADE_SPEED: 250,
    // Maximum number of playlists a user is allowed to create
    MAX_PLAYLISTS: 10,
    // Minimum page width. See config.scss.
    PAGE_WIDTH_MIN: 992
  };

  // Utilities
  app.utils = {};

  // Format video duration for carousel items
  // @param input - length of video in milliseconds
  app.utils.formatDuration = function(input, options) {
    options = _.extend({
      isSeconds: false
    }, options || {});

    var multiplier = options.isSeconds ? 1000 : 1;
    input = new Date(input * multiplier);

    return "M:S".replace("M", function() {
      return pad(input.getMinutes());
    }).replace("S", function() {
      return pad(input.getSeconds());
    });
  };

  // Format video date for carousel items
  app.utils.formatDate = function(input) {
    input = new Date(input);

    return "M D, Y"
      .replace("M", MONTHS[input.getMonth()])
      .replace("D", input.getDate())
      .replace("Y", input.getFullYear());
  };

  app.utils.getTaboolaSession = function() {
    var session = sessionStorage.getItem(app.consts.TABOOLA_SESSION_CACHE_KEY);
    return session ? JSON.parse(session) : null;
  };

  // Configure sticky plugin defaults
  $.fn.sticky.defaults.onStick = function($container) {
    $container
      .wrapInner('<div class="h-content"></div>')
      .wrapInner('<div class="h-container"></div>');
  };
  $.fn.sticky.defaults.onUnStick = function($container) {
    $container
      .find(".h-content").children().unwrap();
    $container
      .find(".h-container").children().unwrap();
  };

  // Add a beforeopen event to the tooltips so we can suppress them
  // from opening if need be.
  (function(oldOpen, oldClose) {
    $.ui.tooltip.prototype.open = function(event) {
      if(this._trigger("beforeopen", event) !== false) {
        oldOpen.apply(this, arguments);
      }
    };

    $.ui.tooltip.prototype.close = function(event) {
      var target = $(event ? event.currentTarget : this.element);
      var tooltip = this._find(target);

      if(this._trigger("beforeclose", {
        event: event,
        tooltip: tooltip
      }) !== false) {
        oldClose.apply(this, arguments);
      }
    };
  })(
    $.ui.tooltip.prototype.open,
    $.ui.tooltip.prototype.close
  );

  // Expose
  window.app = app;

  return app;
});
