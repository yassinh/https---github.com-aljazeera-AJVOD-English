define([
  "app",
  "views/base/menu",
  "views/common/overlay",
  "views/common/selectmenu",
  "views/common/gigya",
  "collections/preferences",
  "text!templates/preferences.html"
], function(app, MenuView, overlayView, SelectMenuView, GigyaView, PreferencesCollection, tmplPreferences) {
  "use strict";

  var CACHE_KEY = "alj-preferences-updated";

  return MenuView.extend({
    id: "preferences",

    tagName: "menu",

    template: _.template(tmplPreferences),

    events: _.extend({}, MenuView.prototype.events, {
      "click .networks": "_onNetworksClick",
      "click .logout": "_onLogoutClick",
      "change select": "_onSelectChange"
    }),

    initialize: function() {
      MenuView.prototype.initialize.call(this);
      this.collection = new PreferencesCollection();

      if(sessionStorage.getItem(CACHE_KEY) === "true") {
        app.trigger("notify", {
          title: app.localization.preferences_updated
        });

        sessionStorage.removeItem(CACHE_KEY);
      }

      this.listenTo(app, "gigya:login", this._login);
      this.listenTo(app, "gigya:logout", this.close);
    },

    open: function() {
      // Lazy load this element into the DOM
      if(!$.contains(document.body, this.el)) {
        $("#container").append(this.el);
      }

      // inherit
      MenuView.prototype.open.apply(this, arguments);

      // Render without any data immediately
      this._render();

      // fetch data and re-render
      $.when(
        this.collection.fetch(),
        GigyaView.getUserInfo()
      )
      .done(_.bind(this._render, this))
      .fail(_.bind(this._onFetchFail, this));
    },

    close: function() {
      MenuView.prototype.close.apply(this, arguments);
      GigyaView.hideUI();
      this._destroySelectMenus();
      this.$el.empty();
    },

     /**
      * Renders the template.
      * @param data {Object} - User's stored preferences data from the ALJ server
      * @param userData {Object} - Gigya user DTO
      */
    _render: function(data, userData) {
      var context = {
        data: data,
        user: userData ? userData.user : null,
        localization: app.localization
      };

      context.isLoggedIn = context.user && context.user.isLoggedIn;
      console.log("Rendering preferences menu", context);
      this.$el.html(this.template(context));
      this._createSelectMenus();
    },

    _onSelectChange: function(event) {
      if(this.syncDfd && this.syncDfd.state() === "pending") {
        console.warn('Existing preferences save in progress; returning');
        return;
      }

      this.syncDfd = this.collection.save(this.$("form").serialize())
        .fail(function() {
          app.trigger("notify", {
            style: "error",
            title: app.localization.preferences_save_failed
          });
        })
        .done(function(resp) {
          if(event.currentTarget.name === "lang") {
            sessionStorage.setItem(CACHE_KEY, true);
            window.location.reload();
          } else {
            app.trigger("notify", {
              title: app.localization.preferences_updated
            });
          }
        });
    },

    _onNetworksClick: function(event) {
      event.preventDefault();
      GigyaView.showConnectionsUI();
    },

    _onLogoutClick: function(event) {
      event.preventDefault();
      GigyaView.logout();
    },

    _onFetchFail: function(jqxhr) {
      console.log("fetch fail");
      var msg = { style: "error" };

      if(jqxhr.status === 401) {
        msg.title = app.localization.preferences_please_login;
      } else {
        msg.title = app.localization.preferences_fetch_failed;
      }

      app.trigger("notify", msg);
      this.close();
    },

    _createSelectMenus: function() {
      this.views.selectmenus = this.$("select").map(function() {
        return new SelectMenuView({
          el: $(this),
          insideOverlay: true,
          insidePreferences: true
        });
      }).get();
    },

    _destroySelectMenus: function() {
      this.views.selectmenus = _.filter(this.views.selectmenus, function(obj) {
        obj.destroy();
        return true;
      });
    },

    // gigya event listener
    _login: function() {
      // Re-fetch and render if this thing is open when the user logs in
      if(this.isOpen) {
        this.open();
      }
    }
   });

});
