// Time machine dialog view
define([
  "app",
  "views/common/bigscreendialog",
  "text!templates/time-machine.html",
  "iscroll"
], function(app, BigScreenDialogView, tmplTimeMachine) {
  "use strict";

  var SuperProto = BigScreenDialogView.prototype;

  return BigScreenDialogView.extend({
    template: _.template(tmplTimeMachine),

    defaults: _.extend({}, SuperProto.defaults, {
      dialogClass: "big-screen timemachine"
    }),

    events: _.extend({}, BigScreenDialogView.prototype.events, {
      "click .bs-tab a": "_toggleCarousel",
      "click #timemachine-timeline li": "_onItemClick"
    }),

    destroy: function() {
      SuperProto.destroy.apply(this, arguments);
      this._destroyScroller();
    },

    // Fetch data before trying to open this dialog. If the fetch
    // succeeds, cache the data and continue opening the dialog.
    open: function() {
      var args = [].slice.call(arguments);

      var onFail = _.bind(function(title) {
        console.error('Time machine ajax error handler fired');
        this.trigger("error");
        app.trigger("notify", { type: "error", title: title });
      }, this);

      app.trigger("spinner:show");

      this._fetch().done(_.bind(function(resp) {
        if(_.isArray(resp.videos) && !resp.videos.length) {
          onFail(app.localization.timeline_no_videos_error);
        } else {
          this.data = resp;
          SuperProto.open.apply(this, args);
        }
      }, this)).fail(function() {
        onFail(app.localization.timeline_open_error);
      }).always(function() {
        app.trigger("spinner:hide");
      });
    },

    _cacheElements: function() {
      this.$container = this.$("#timemachine-timeline");
      this.$timeline = this.$container.find("ul");
      this.$lis = this.$timeline.find("li");
      this.$playlist = this.$(".timemachine-container");
      this.$playlistContainer = this.$(".bs-carousel");
    },

    _onDialogOpen: function() {
      if(!this.data) {
        console.error('Trying to open time machine without any data');
        return;
      }

      // Inherit
      SuperProto._onDialogOpen.apply(this, arguments);

      // Only do momentum scrolling if the browser supports it.
      // Fallback is overflow:scroll
      if(Modernizr.csstransforms) {
        this._createScroller();
        this._centerActiveItem(0);

        if(app.context.isRTL) {
          this.scroller.scrollToElement(this.$lis.first()[0], 0);
        }
      }
    },

    _onWindowResize: function() {
      SuperProto._onWindowResize.apply(this, arguments);
      this._refreshScroller();
    },

    // TODO: use a collection
    _fetch: function() {
      return $.ajax("/api/events/timeline", {
        type: "get",
        data: { videoId: this.options.videoID }
      });
    },

    _createContext: function() {
      var data = this.data;

      if(data == null) {
        console.error('Trying to create a context without any data');
        return;
      }

      var yearMemo = null;
      data.videos = _.map(data.videos, function(video) {
        // format some more dates..
        var date = new Date(video.publishDate);
        video.year = date.getFullYear();

        // Figure out if this is a starting year
        if(video.year !== yearMemo) {
          video.startsYear = true;
          yearMemo = video.year;
        }

        return video;
      });

      // Make sure these keys exist
      if(!("startDate" in data)) {
        data.startDate = data.endDate = null;
      }

      // grab total duration
      data.duration = _.reduce(_.pluck(data.videos, "duration"), function(memo, value) {
        return memo + value;
      }, 0);

      // format it
      data.duration = app.utils.formatDuration(data.duration);

      data.player_id = app.player_id;
      data.player_key = app.player_key;

      return data;
    },

    _createScroller: function() {
      this.scroller = new window.IScroll("#timemachine-timeline", {
        scrollX: true,
        scrollY: false,
        probeType: 3
      });
    },

    _destroyScroller: function() {
      if(!this.scroller) return;
      this.scroller.destroy();
      this.scroller = null;
    },

    _refreshScroller: function() {
      if(!this.scroller) return;
      this._destroyScroller();
      var width = this.$timeline.data("width");
      this.$timeline.css("width", width);
      setTimeout(_.bind(function() {
        this._createScroller();
        this._centerActiveItem(0);
      }, this), 50);
    },

    _setActiveItem: function(index) {
      this.$lis.removeClass("is-active");
      this.$lis.eq(index).addClass("is-active");
      this._loadVideo(this.$lis.eq(this.currentIndex).data("video-id"));
      this._centerActiveItem();
    },

    _centerActiveItem: function(speed) {
      if(!this.scroller) return;
      if(this.scroller.isAnimating) return;
      console.log('centering active item');
      var li = this.$lis.eq(this.currentIndex)[0];
      this.scroller.scrollToElement(li, speed == null ? 750 : speed, true);
    },

    _onItemClick: function(event) {
      // Allow show links to follow. The >1 logic ensures the value of teh link
      // isn't blank and isn't set to a hash.
      if(event.target.tagName === "A" && event.target.getAttribute("href").length > 1) {
        return;
      }

      event.preventDefault();
      if(this.scroller && this.scroller.isAnimating) return;
      BigScreenDialogView.prototype._onItemClick.apply(this, arguments);
    },

    _advanceCarousel: function() {
      this.currentIndex++;
      var $nextLi = this.$lis.eq(this.currentIndex);
      var id = $nextLi.data("video-id");

      if(!id) {
        console.warn('Tried to advance timeline to a null id', id, $nextLi);
      }

      if($nextLi.length) {
        this._setActiveItem(this.currentIndex);
      }
    },

    _createCarousel: $.noop
  });

});
