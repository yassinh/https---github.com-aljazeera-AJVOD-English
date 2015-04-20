define(["app"], function(app) {
  "use strict";

  var support = {
    video: /no-video/.test(document.documentElement.className) === false && /iPod|iPhone|IEMobile|WPDesktop/.test(navigator.userAgent) === false,
    bgsize: Modernizr.backgroundsize
  };

  var $win = $(window);

  /**
   * @constructor
   */
  function BackgroundPlayer(container, options) {
    // Cache elements
    this.$container = $(container);
    this.$video = this.$container.children();
    this.$sources = this.$video.children("source");
    this.video = this.$video[0];

    // Create options
    this.options = $.extend({}, {
      loop: true,
      muted: true,
      autoPlayTimeout: 2000,
      onFallbackRendered: $.noop
    }, options || {});

    // Kickoff
    this._init();
  }

  BackgroundPlayer.prototype = {
    /**
     * @private
     * Initializer
     */
    _init: function() {
      if(!support.video) {
        this._renderFallback();
        return;
      }

      // Namespace to bind global events with
      this._eventNamespace = ".bgplayer-" + (new Date()).getTime();

      // Register listenerrs
      this._bindEvents();

      if(this.options.loop) {
        this.video.loop = true;
      }
      if(this.options.muted) {
        this.video.muted = true;
      }
    },

    /**
     * Destroy this plugin. Public method.
     */
    destroy: function() {
      $win.off(this._eventNamespace);
      this.$video.removeAttr("style");
    },

    /**
     * @private
     * Timer for determining whether or not autoplay is supported
     */
    _autoPlayTimer: null,

    /**
     * @private
     * Register event listeners
     */
    _bindEvents: function() {
      $win.on("resize" + this._eventNamespace, $.proxy(this._resize, this));

      var onstalled = _.bind(this._fallbackAllSourcesFailed, this);
      var onerror = _.bind(this._fallbackAllSourcesFailed, this);
      var oncanplay = _.bind(this._fallbackNoAutoPlay, this);
      var onloadedmetadata = _.bind(this._onMetadataLoaded, this);

      if(window.addEventListener) {
        this.video.addEventListener('stalled', onstalled, false);
        this.video.addEventListener('error', onerror, false);
        this.video.addEventListener('canplay', oncanplay, false);
        this.video.addEventListener('loadedmetadata', onloadedmetadata, false);
        this.$sources.last()[0].addEventListener('error', onerror, false);
      } else if (window.attachEvent) {
        this.video.attachEvent('onstalled', onstalled);
        this.video.attachEvent('onerror', onerror);
        this.video.attachEvent('oncanplay', oncanplay);
        this.video.attachEvent('onloadedmetadata', onloadedmetadata);
        this.$sources.last()[0].attachEvent('onerror', onerror);
      }
    },

    _onError: function() {
      if(this.video.networkState === 3) { // no source
        this._fallbackAllSourcesFailed();
      }
    },

    /**
     * @private
     * Event handler for when metadata has loaded inside the <video> tag.
     */
    _onMetadataLoaded: function() {
      this._mediaAspect = this.video.videoWidth / this.video.videoHeight;
      this._resize();
    },

    /**
     * @private
     * Event handler for when the video tag is ready to be played.
     * If the video hasn't begun to play Xms after canPlay has fired, then
     * render the fallback.
     */
    _fallbackNoAutoPlay: function() {
      clearTimeout(this._autoPlayTimer);

      this._autoPlayTimer = setTimeout($.proxy(function() {
        if(this.video.currentTime === 0) {
          this._log("Auto play doesn't seem to be supported; rendering fallback");
          this._renderFallback();
        }
      }, this), this.options.autoPlayTimeout);
    },

    /**
     * @private
     * Event handler for when the video tag experiences an error. If the last
     * source node errors out, then all sources have failed.
     */
    _fallbackAllSourcesFailed: function(event) {
      this._log("The last source experienced an error; rendering fallback");
      this._renderFallback();
    },

    /**
     * @private
     * Render the fallback image
     */
    _renderFallback: function() {
      //var $fallback = this.$container.find(".bgplayer-fallback");
      //var url = $fallback.data("image");
      //$fallback.css("background-image", "url('" + url + "')");
      //$fallback.insertAfter(this.$video);
      //this.$video.remove();
        this.$sources[0].attr('src', '/resources/images/VOD_Background.mp4');
        this.$sources[1].attr('src', '/resources/images/VOD_Background.mp4');
      this.options.onFallbackRendered.call(this);
    },

    /**
     * @private
     * Resize the video using an algorithm that removes letterboxing
     */
    _resize: function() {
      var windowW = window.innerWidth;
      var windowH = window.innerHeight;
      var mediaAspect = this._mediaAspect;
      var video = this.video;
      var windowAspect = windowW / windowH;
      var top, left, width, height;

      if (windowAspect < mediaAspect) {
        top = 0;
        left = (-(windowH * mediaAspect - windowW) / 2) + "px";
        width = (windowH * mediaAspect) + "px";
        height = windowH + "px";
      } else {
        left = 0;
        top = ((((windowW / mediaAspect) - windowH) / 2) * -1) + "px";
        width = windowW + "px";
        height = (windowW / mediaAspect) + "px";
      }

      video.style.left = left;
      video.style.top = top;
      video.style.width = width;
      video.style.height = height;
    },

    /**
     * @private
     * Utility method for logging to the console
     * @param msg {string} message to log
     */
    _log: function(msg) {
      console.log(">>> Background Player:", msg);
    }
  };

  return BackgroundPlayer;

});
