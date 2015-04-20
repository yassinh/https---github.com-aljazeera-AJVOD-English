// Auto resume functionality

define([
  "app",
  "views/common/gigya"
], function(app, GigyaView) {
  "use strict";

  var BaseView = app.views.BaseView;
  var cacheKey = app.consts.AUTO_RESUME_CACHE_KEY;
  var _currentVideoID = null;
  var _videoPosition = 0;
  var _isLoggedIn = false;
  
  /**
   * Read the stored position from localStorage and cache it. All other requests
   * after this one will return the cached value instead.
   * @private
   */
  var getStoredPosition = (function() {
    var storedPosition;
    
    return function() {
      if(storedPosition) {
        return storedPosition;
      }
      
      storedPosition = localStorage.getItem(cacheKey);
      return storedPosition;
    };
  })();
  
  /**
   * Cache the video position for the current video ID.
   * @private
   * @param {Number} position
   */
  var setStoredPosition = function(position, sync) {
    if(_currentVideoID && _isLoggedIn && position) {
      localStorage.setItem(cacheKey, position + '-' + _currentVideoID);

      if(sync) {
        syncPosition(_currentVideoID, position);
      }
    }
  };

  var removeStoredPosition = function() {
    localStorage.removeItem(cacheKey);

    $.post("/api/positions/remove", {
      id: _currentVideoID
    });
  };
  
  /**
   * Parse the video ID out of the value stored in localStorage.
   * @private
   * @param {String} storedValue
   */
  var getStoredVideoID = function(storedValue) {
    return parseInt(storedValue.split("-").pop(), 10);
  };
  
  /**
   * Parse the seek position out of the value stored in localStorage.
   * @private
   * @param {String} storedValue
   */
  var getStoredSeekTime = function(storedValue) {
    var storedPositionDelimiterIndex = storedValue.indexOf('-');
    var storedSeekPosition = storedValue.substring(0,storedPositionDelimiterIndex);
    return storedSeekPosition;
  };

  /**
   * Sync a position and video ID to the server.
   * @private
   * @param {Number} videoID
   * @param {Number} position
   */
  var syncPosition = function(videoID, position) {
    console.log('Auto resume: syncing stored position', position, "for video", videoID);
    $.ajax("/api/positions/set", {
      type: "post",
      data: { id: videoID, pos: position }
    });
    // localStorage.removeItem(cacheKey);
  };

  return BaseView.extend({
    initialize: function() {
      BaseView.prototype.initialize.apply(this, arguments);
      this.playerView = this.options.playerView;

      // now that you have playerView isntance you can listen to events,
      // call methods on the player, etc.
      this.playerView.on("template_ready", this._onTemplateReady, this);
      this.playerView.on("stop", this._onPlayerStop, this);
      this.playerView.on("begin", this._onMediaBegin, this);

      // API methods
      this.listenTo(app, "api:saveVideoProgress", function(videoID, seekTime) {
        if(!_isLoggedIn) return;
        syncPosition(videoID, seekTime);
      });

      app.on("gigya:login", this._login);
      app.on("gigya:logout", this._logout);
      
      // use jQuery to listen for when the page unloads
      $(window).on("unload", _.bind(function() {
        var position = this.playerView.getCurrentPosition();
        var duration = this.playerView.modVP.getVideoDuration();

        if(position > 0 && Math.floor(position) < Math.floor(duration)) {
          console.log('Auto resume: storing position on unload', position);
          setStoredPosition(position);
        }
      }, this));
      
      GigyaView.isLoggedIn().done(function(flag) {
        _isLoggedIn = flag;
      });
    },
    
    _login: function() {
      _isLoggedIn = true;
    },
    
    _logout: function() {
      _isLoggedIn = false;
    },
    
    _onTemplateReady: function() {
      // get current video ID
      _currentVideoID = this.playerView.modVP.getCurrentVideo().id;
    },
    
    _onMediaBegin: function() {
      // if we already have a seek position from localStorage,
      // then use it.
      var storedPosition = getStoredPosition();
      var storedSeekTime = 0;
      var self = this;
      
      if(!_isLoggedIn) {
        return;
      }

      // If there's a position stored in localStorage and the position
      // correlates with the video ID we're currently watching, seek to that
      // position immediately.
      if(storedPosition) {
        var storedVideoID = getStoredVideoID(storedPosition);

        // if the stored Video ID matches the current video, grab the time and seek to it
        if(storedVideoID === _currentVideoID) {
          storedSeekTime = getStoredSeekTime(storedPosition);
        }

        this.playerView.modVP.seek(storedSeekTime);

      // otherwise ask server for the position
      } else {
        var jqxhr = $.ajax("/api/positions/get", {
          data: { id: _currentVideoID }
        }).done(function(data) {
          if(data.pos) {
            console.log('Auto resume: seeking to', data.pos);
            self.playerView.seek(data.pos);
          }
        });
      }
    },

    _onPlayerStop: function() {
      if(!_isLoggedIn) return;
      var pos = this.playerView.getCurrentPosition();
      var duration = this.playerView.modVP.getVideoDuration();

      if((Math.round(pos) / Math.round(duration)) * 100 >= 99) {
        console.log('Auto resume: video stop on completion; calling remove API');
        removeStoredPosition();

      } else {
        console.log('Auto resume: storing position on stop', pos);
        setStoredPosition(pos, true);
      }
    }

  // Class methods. See page.js for how we're using this
  }, {
    /**
     * This method is called on every page load, and sync's the stored position
     * to the server if a position it stored.
     */
    syncStoredPosition: function() {
      GigyaView.isLoggedIn().done(function(isLoggedIn) {
        if(!isLoggedIn) return;

        // call this on every page load to fire an ajax request
        // if there is a stored position/video id
        var storedPosition = getStoredPosition();
        
        // if we already have a seek position from localStorage,
        // then use it.
        if(storedPosition) {
          // grab the stored Video ID and stored position and write to the server
          var storedVideoID = getStoredVideoID(storedPosition);
          var storedSeekTime = getStoredSeekTime(storedPosition);
          syncPosition(storedVideoID, storedSeekTime);
          console.log('Auto resume: recording last stored position on page load', storedVideoID, storedSeekTime);
        }
      });
    }
  });

});
