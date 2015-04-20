// The player view wraps around the standard Brightcove player
// and provides an easier-to-use API.
//
// Create a new PlayerView instance on an object that contains
// Brightcove embed code.

define([
  "app",
  "views/common/gigya",
  "views/common/autoresume"
], function(app, GigyaView, AutoResumeView) {
  "use strict";

  var BaseView = app.views.BaseView;
  var instances = [];

  // Initialize each instance once the template has loaded.
  window.onTemplateLoaded = function(experienceID) {
    _.each(instances, function(obj) {
      if(obj.experienceID === experienceID) {
        console.log('Initializing player view with experience ID', experienceID, obj);
        obj._initWhenTemplateLoaded();
      }
    });
  };

  return BaseView.extend({
    // Template ready fired yet?
    isReady: false,

    // Queue of methods to fire once the player is ready
    _readyQueue: [],

    // The experience associated with this instance
    experienceID: null,

    // The current video loaded into the player
    currentVideo: {},

    defaults: {
      automute: false,
      autoresume: false
    },

    initialize: function() {
      BaseView.prototype.initialize.call(this);

      this.options = _.extend({}, this.defaults, this.options);

      // Set experience ID
      this.experienceID = this.$el.find("object").attr("id");
      console.log('Player: setting exp ID to', this.experienceID);
      console.log('Player', this.$el.find("object"));
      console.log('options', this.options);
      // Proxy module methods to this view immediately. This allows
      // commands to be sent to the player before the ready event has
      // even fired
      this._proxyMethods("modVP", [
        "loadVideo", "mute", "pause", "seek"
      ]);

      // Enable auto resume?
      if(this.options.autoresume && app.customplayer !== true) {
        this.views.autoResume = new AutoResumeView({
          playerView: this
        });
      }

      // Auto mute?
      if(this.options.automute) {
        if(app.customplayer === true) {
          $('<param name="startMuted" value="true" />').appendTo(this.$el.find("object"));
        } else {
          this.listenTo(this, "template_ready", function() {
            this.mute();
          });
        }
      }

      // For the API
      this.listenTo(app, "api:retrieveVideoProgress", this._retrieveVideoProgress);

      // Remember this instance
      instances.push(this);
    },

    destroy: function() {
      this.remove();

      // Remove instance from array
      instances = _.reject(instances, function(obj) {
        return obj.experienceID === this.experienceID;
      }, this);
    },

    getCurrentVideo: function() {
      return this.currentVideo;
    },

    getIsPlaying: function() {
      return this._isPlaying;
    },

    getCurrentPosition: function() {
      return this.position;
    },

    // Custom initializer. This is called automatically when template loaded
    // has fired for this view's experience ID
    _initWhenTemplateLoaded: function() {
      // Get references to Brightcove modules
      var player = this.player = brightcove.getExperience(this.experienceID);
      this.modVP = player.getModule(APIModules.VIDEO_PLAYER);
      this.modExp = player.getModule(APIModules.EXPERIENCE);
      this.modCon = player.getModule(APIModules.CONTENT);

      // Proxy module events directly to this view so you can listen to an
      // event via the syntax:
      //    playerViewInstance.on("change", fn);
      this._proxyEvents("modExp", [
        "template_ready", "enter_fullscreen", "exit_fullscreen"
      ]);
      this._proxyEvents("modVP", [
        "begin", "change", "complete", "error",
        "play", "progress", "seek_notify", "stop"
      ]);

      // Trigger ready event when the TEMPLATE_READY callback fires
      this.on("template_ready", function() {
        this.isReady = true;

        // Process any commands that were sent into the player before it was ready
        _.each(this._readyQueue, function(fn) {
          fn();
        });
      }, this);

      // Keep track of the current video whenever one changes
      this.on("change", function() {
        this.currentVideo = this.modVP.getCurrentVideo();
      }, this);

      // Remember videos that are played for Taboola results
      this.on("begin", function() {
        localStorage.setItem(app.consts.TABOOLA_CACHE_KEY, JSON.stringify({
          id: this.currentVideo.id,
          name: this.currentVideo.displayName,
          url: window.location.href
        }));
      }, this);

      // Keep track of the current position. This is necessary because
      // modVP's getCurrentPosition is async and thus doesn't work on
      // page unload.
      this.on("progress", function(data) {
        this.position = data[0].position;
      }, this);

      // Remember if this video is playing or not
      this.on("play", function() {
        this._isPlaying = true;
      }, this);
      this.on("stop", function() {
        this._isPlaying = false;
      }, this);
    },

    _proxyEvents: function(module, methods) {
      var self = this;
      var namespace = module === "modVP" ? BCMediaEvent : BCExperienceEvent;

      _.each(methods, function(name) {
        self[module].addEventListener(namespace[name.toUpperCase()], function() {
          self.trigger(name, [].slice.call(arguments));
        });
      });
    },

    _proxyMethods: function(module, methods) {
      var self = this;

      _.each(methods, _.bind(function(method) {
        self[method] = function() {
          var args = [].slice.call(arguments);

          if(!self.isReady) {
            console.warn('Attempted to call %s but the player is not ready yet; queuing command', method);
            self._readyQueue.push(function() {
              console.log('Player - processing queue: ', module, method);
              self[module][method].apply(self[module], args);
            });
          } else {
            self[module][method].apply(self[module], args);
            console.log("Calling method %s on module %s with args", method, module, args);
          }
        };
      }, this));
    },

    _retrieveVideoProgress: function(videoID, eventLoopId) {
      GigyaView.isLoggedIn().done(function(isLoggedIn) {
        var done = function(data) {
          app.trigger('api:retrieveVideoProgress:complete', data ? data.pos : -1, eventLoopId);
        };

        if(!isLoggedIn) {
          app.trigger('api:retrieveVideoProgress:complete', -1, eventLoopId);
          return;
        }

        $.ajax("/api/positions/get", {
          data: { id: videoID }
        }).then(done, done);
      });
    }
  });

});
