/**
 * This is the base page view from which most others extend. This class
 * provides only the most basic functionality that needs to be available
 * on every page.
 *
 * You may consider extending from VideoPage instead to enable
 * subscriptions, grid hover states, etc.
 */
define([
  "app",
  "views/common/navigation",
  "views/common/searchOverlay",
  "views/common/gigya",
  "views/common/preferences",
  "views/common/notification",
  "views/common/autoresume",
  "views/common/spinner",
  "views/common/help",
  "collections/subscriptions",
  "api"
], function(
  app,
  NavigationView,
  SearchView,
  GigyaView,
  PreferencesView,
  NotificationView,
  AutoResumeView,
  SpinnerView,
  HelpView,
  SubscriptionsCollection,
  api
) {
  "use strict";

  var BaseView = app.views.BaseView;

  return BaseView.extend({
    el: document.body,

    events: _.extend({}, BaseView.prototype.events, {
      "click .preferences": "_togglePreferences",
      "click .nav-search": "_toggleSearchOverlay",
      "click .help-dialog": "_openHelpDialog"
    }),

    initialize: function() {
      BaseView.prototype.initialize.call(this);

      if(app.customplayer !== true) {
        AutoResumeView.syncStoredPosition();
      }

      GigyaView.getUserInfo().done(_.bind(function(data) {
        if(data.user.isLoggedIn) {
          this._showMyChannelMenuTile(data.user.thumbnailURL);
        }
      }, this));

      // Check for new subscriptions
      if(this.el.id !== "your-channel") {
        this._checkNewSubscriptions();
      }
    },

   

    _initSubviews: function() {
      BaseView.prototype._initSubviews.call(this);

      // Place this first so other views can send notifications
      this.views.notify = new NotificationView();

      this.views.navigation = new NavigationView({
        el: $("#header")
      });
      this.views.preferences = new PreferencesView();
      this.views.search = new SearchView({
        el: $("menu[data-for='search-overlay']")
      });
      this.views.gigya = new GigyaView({
        el: $("#header .links")
      });
      this.views.spinnerOverlay = new SpinnerView({
        append: true,
        style: "overlay"
      });
    },

    _bindEvents: function() {
      // Open the search dialog via a keyboard shortcut
      $(document).on("keyup", _.bind(function(event) {
        if(event.which === 83 &&
          this.views.search &&
          this.views.search.isOpen === false &&
          event.target.tagName !== "INPUT" &&
          event.target.tagName !== "TEXTAREA"
        ) {
           this.views.search.open();
        }
      }, this));

      this.listenTo(app, "spinner:show", function() {
        this.views.spinnerOverlay.show();
      });
      this.listenTo(app, "spinner:hide", function() {
        this.views.spinnerOverlay.hide();
      });
      this.listenTo(app, "gigya:login", this._onGigyaLogin);
      this.listenTo(app, "gigya:logout", this._onGigyaWillLogout);
      this.listenTo(app, "gigya:logout:complete", this._onGigyaLogout);

      // Close Gigya dialogs when menus open
      this.listenTo(app, "menu:open", function() {
        GigyaView.hideUI();
      });
      this.listenTo(app, "dialog:open", function() {
        GigyaView.hideUI();
      });
    },

    // Check if there are new subscriptions based upon the last time
    // the `your channel` page was visited.
    _checkNewSubscriptions: function() {
      var collection = new SubscriptionsCollection();

      // If there are new subscriptions show a notification
      collection.checkForNew().done(function() {
        app.trigger("notify", {
          title: window.localization.new_subscriptions_text
        });
      });
    },

    _togglePreferences: function(event) {
      this.views.preferences.toggle();
      event.preventDefault();
    },

    _toggleSearchOverlay: function(event) {
      this.views.search.toggle();
      event.preventDefault();
    },

    _onGigyaLogin: function(gigyaData) {
      if(gigyaData.operation === "getUserInfo") {
        return;
      }

      app.trigger("spinner:show");
      window.location.reload();
    },

    _onGigyaWillLogout: function() {
      app.trigger("spinner:show");
    },

    _onGigyaLogout: function() {
      window.location.reload();
    },

    _openHelpDialog: function(event) {
      event.preventDefault();
      var help = this.views.help;
      var data = $(event.currentTarget).data();

      var context = {};
      context.title = data.helpTitle;
      context.description = data.helpDesc;

      if(!help) {
        help = this.views.help = new HelpView({
          destroyOnClose: false,
          width: 500
        });
      }

      help.render(context);
      help.open();
    },

    _showMyChannelMenuTile: function(thumbUrl) {
      var menu = this.views.navigation.getMenu("channel");

      if(!menu) {
        console.warn('Tried to show the my channel tile but the channel menu does not exist');
        return;
      }

      menu.$el.find(".your-channel").removeClass("theme-orange");
      menu.$el.find(".your-channel").css('backgrond-color',"gray");
      var $tile = menu.$el.find(".your-channel").removeClass("h-hidden");

      if(thumbUrl) {
        $tile.find("img").attr("src", thumbUrl);
      }
    }
  });

});
