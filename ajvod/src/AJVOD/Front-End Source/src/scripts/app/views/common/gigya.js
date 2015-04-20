define([
  "app",
  "text!templates/gigya-navigation.html"
], function(app, tmplGigyaNav) {
  "use strict";

  var BaseView = app.views.BaseView;
  var lang = app.context.lang.toUpperCase();
  var isLoggedInDfd;
  var instance;

  // Create a promise that resolves when Gigya has been
  // initialized.
  var gigyaReady = $.Deferred();
  window.onGigyaServiceReady = function() {
    gigyaReady.resolve();
  };

  // Also poll for gigya ready in case the onGigyaServiceReady hook isn't
  // attached to window by the time Gigya is actually loaded. This can
  // happen because Gigya is added to the DOM asynchronously.
  var gigyaReadyTimer = setInterval(function() {
    if(window.gigya && window.gigya.loaded && gigyaReady.state() !== "resolved") {
      window.onGigyaServiceReady();
      clearInterval(gigyaReadyTimer);
    }
  }, 250);

  // screen set lang suffix
  var suffix = app.context.lang === "en" ? "" : ("-" + app.context.lang);

  /*
   * Base gigya view
   * el: $("#header .links")
   */

  var GigyaView = BaseView.extend({
    events: {
      "click .logout": "_logout",
      "click .login": "_showLoginUI",
      "click .register": "_showRegisterUI",
      "click .welcome": "_showEditProfileUI",
      "click .socialnetworks": "_showConnectionsUI"
    },

    template: _.template(tmplGigyaNav),

    initialize: function() {
      _.bindAll(this, "render");

      BaseView.prototype.initialize.call(this);

      gigyaReady.done(_.bind(function() {
        gigya.accounts.addEventHandlers({
          onLogin: _.bind(this._loginToServer, this)
          // onLogout: _.bind(this.onGigyaLogout, this)
        });
        gigya.socialize.addEventHandlers({
          onConnectionAdded: this.render,
          onConnectionRemoved: this.render
        });

        this._loginCheck();
        this.render();
      }, this));

      instance = this;
    },

    ready: function(callback) {
      return gigyaReady.done(callback);
    },

    render: function() {
      GigyaView.getUserInfo().done(_.bind(function(data) {
        console.log('Rendering context', data);
        data.localization = app.localization;
        this.$el.html(this.template(data));
        this._initTitleTooltips(this.$el);

        setTimeout(_.bind(function() {
          this.$el.addClass("run-animation");
        }, this), 50);
      }, this)).fail(function(data) {
        console.error('Error getting user info in render', data);
      });
    },

    /**
     * Fires when the call to Gigya login has succeeded.
     * @param {object} data - Gigya DTO
     * @return null
     */
    _loginToServer: function(data) {
      $.ajax({
        url: "/api/login",
        method: "post",
        data: {
          UID: data.UID,
          UIDSignature: data.UIDSignature,
          signatureTimestamp: data.signatureTimestamp
        }
      }).pipe(function(resp) {
        if(resp.status === "FAIL") {
          return $.Deferred().reject(data);
        }

        localStorage.setItem(app.consts.GIGYA_CURRENT_SESSION_CACHE_KEY, data.signatureTimestamp);
        app.trigger("gigya:login", data);
        isLoggedInDfd = null;
      }).fail(_.bind(function() {
        console.error('Error logging into gigya', arguments);
        app.trigger("notify", {
          style: "error",
          title: app.localization.login_error_text
        });

        this._logout();
      }, this));
    },

    // Perform the logout from ALJ back end and Gigya service
    _logout: function(event, details) {
      if(event) event.preventDefault();
      gigya.socialize.logout();
      gigya.socialize.hideUI();
      isLoggedInDfd = null;
      GigyaView.clearSession();
      $.get("/api/logout").always(function() {
        app.trigger("gigya:logout:complete");
      });
      app.trigger("gigya:logout");
    },

    // Login screens
    _showLoginUI: function(event) {
      if(event) event.preventDefault();

      app.trigger("spinner:show");
      this.openScreenSet = app.consts["GIGYA_LOGIN_SCREEN_SET_" + lang];

      gigya.accounts.showScreenSet({
        screenSet: this.openScreenSet,
        mobileScreenSet: app.consts["GIGYA_LOGIN_MOBILE_SCREEN_SET_" + lang],
        sessionExpiration: 2 * 60 * 60, // 2 hours
        onError: function() {
          console.error('error showing login screen', arguments);
        },
        onLoad: _.bind(function() {
          app.trigger("spinner:hide");
        }, this)
      });
    },

    // Add connections UI
    // http://developers.gigya.com/020_Client_API/010_Socialize/socialize.showAddConnectionsUI
    // http://developers.gigya.com/010_Developer_Guide/10_UM360/070_Connect
    _showConnectionsUI: function(event) {
      if(event) event.preventDefault();
      gigya.socialize.showAddConnectionsUI();
      this._closeOpenScreenSet();
    },

    // Hide all gigya dialogs
    _hideUI: function() {
      gigyaReady.done(_.bind(function() {
        gigya.socialize.hideUI();
        this._closeOpenScreenSet();
      }, this));
    },

    // Registration UI
    _showRegisterUI: function(event) {
      if(event) event.preventDefault();
      this.openScreenSet = app.consts["GIGYA_LOGIN_SCREEN_SET_" + lang];
      gigya.socialize.hideUI();

      gigya.accounts.showScreenSet({
        screenSet: this.openScreenSet,
        mobileScreenSet: app.consts["GIGYA_LOGIN_MOBILE_SCREEN_SET_" + lang],
        startScreen: app.consts["GIGYA_LOGIN_REGISTER_SCREEN_" + lang],
        onFieldChanged: _.bind(function(data) {
          this._cleanFormField(data.form, data.fieldName);
        }, this)
      });
    },

    // Edit profile
    _showEditProfileUI: function(event) {
      event.preventDefault();

      this.openScreenSet = app.consts["GIGYA_EDIT_PROFILE_SCREEN_SET_" + lang];
      gigya.socialize.hideUI();

      gigya.accounts.showScreenSet({
        screenSet: this.openScreenSet,
        onFieldChanged: _.bind(function(data) {
          this._cleanFormField(data.form, data.fieldName);
        }, this)
      });
    },

    // utils

    /**
     * Make an login API call if the user is logged into gigya and this
     * is the first session request. This keeps the gigya logged in state
     * in sync with the server login state in the event that the server
     * is restarted or something.
     */
    _loginCheck: function() {
      if(sessionStorage.getItem(app.consts.GIGYA_PERFORMED_LOGIN_CHECK) !== "true") {
        GigyaView.getUserInfo().done(_.bind(function(data) {
          sessionStorage.setItem(app.consts.GIGYA_PERFORMED_LOGIN_CHECK, true);

          if(data.user && data.user.isLoggedIn) {
            this._loginToServer(data);
          } else {
            this._logout();
          }
        }, this));
      }
    },

    _closeOpenScreenSet: function() {
      if(!this.openScreenSet) {
        return;
      }

      gigya.accounts.hideScreenSet({
        screenSet: this.openScreenSet
      });

      this.openScreenSet = null;
    },

    _cleanFormField: function(formID, fieldName) {
      var sanitize = function(s) {
        return s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/"/g, '&quot;');
      };

      $('form[data-gigya-id="' + formID + '"]')
        .find('[name="' + fieldName + '"]')
        .val(function(i, value) {
          return sanitize(value);
        });
    }
  }, {

    /**
     * Get the current Gigya user DTO
     * @return {deferred} a promise that resolves with Gigya user info
     */
    getUserInfo: function() {
      return $.Deferred(function(dfd) {
        gigyaReady.always(function() {
          gigya.socialize.getUserInfo({
            callback: function(data) {
              dfd[data.status === "FAIL" ? "reject" : "resolve"](data);
            }
          });
        });
      }).promise();
    },

    /**
     * Re-render the header links
     * @returns null
     */
    renderHeader: function() {
      instance.render();
    },

    /**
     * Log out of gigya
     */
    logout: function() {
      instance._logout();
    },

    /**
     * Show the connection UI
     * @returns null
     */
    showConnectionsUI: function() {
      instance._showConnectionsUI();
    },

    /**
     * Hide all Gigya menus
     */
    hideUI: function() {
      instance._hideUI();
    },

    /**
     * Show the Gigya Login UI
     * @returns null
     */
    showLoginUI: function() {
      instance._showLoginUI();
    },

    /**
     * Check to see if the user is currently authenticated with Gigya.
     * @returns {Deferred} a promise that resolves with a true/false flag
     */
    isLoggedIn: function() {
      var filter = function(data) {
        return data.status === "FAIL" ? false : data.user.isLoggedIn;
      };

      return isLoggedInDfd ?
        isLoggedInDfd :
        (isLoggedInDfd = GigyaView.getUserInfo().then(filter, filter));
    },

    /**
     * Get the current Gigya session key
     * @return {string|null} the session key
     */
    getSession: function() {
      return localStorage.getItem(app.consts.GIGYA_CURRENT_SESSION_CACHE_KEY);
    },

    /**
     * Clear the current cached Gigya session key
     * @return null
     */
    clearSession: function() {
      localStorage.removeItem(app.consts.GIGYA_CURRENT_SESSION_CACHE_KEY);
    }
  });

  return GigyaView;
});
