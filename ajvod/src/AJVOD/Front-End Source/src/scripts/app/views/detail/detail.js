/*
 * Top-level detail page
 */

define([
  "app",
  "views/common/videopage",
  "views/common/carousel",
  "views/common/largecarousel",
  "views/common/timemachinedialog",
  "views/common/player",
  "views/common/panel"
], function(app, VideoPageView, CarouselView, LargeCarouselView, TimeMachineDialogView, PlayerView, PanelView) {
  "use strict";

  return VideoPageView.extend({
    initialize: function() {
      // Pause the player when big screen opens. Need to do this before
      // we inherit, or big screen might open before this event has been
      // bound.
      this.listenTo(app, "bigscreen:open", this._pausePlayer);
      this._cacheElems();
      VideoPageView.prototype.initialize.call(this);
      this._initStickyPlayer();
    },

    _bindEvents: function() {
      VideoPageView.prototype._bindEvents.call(this);
      this.listenTo(this.views.player, "begin", this._sendTaboolaHeartbeat);
      this.listenTo(app, "panel:open", this._resizePlayer);
      this.views.gigya.ready(_.bind(this._loadComments, this));
    },

    _initSubviews: function() {
      VideoPageView.prototype._initSubviews.call(this);

      // Player view
      this.views.player = new PlayerView({
        el: $("#player"),
        autoresume: true
      });

      // Panel view
      this.views.panel = new PanelView({
        el: $("#panel"),
        panels: ["sharing", "main", "comments"]
      });

      // Large carousel
      this.views.largeCarousel = new LargeCarouselView({
        el: this.$(".carousel-large"),
        options: {
          numItems: 2.25,
          moveBy: 2.25
        }
      });

      // Smaller carousels
      this.views.smallCarousels = this.$(".carousel").map(function(i, elem) {
        if($(elem).closest(".ui-dialog").length) return null;
        return new CarouselView({ el: elem });
      }).get();
    },

    _cacheElems: function() {
      this.$player = $("#player-container");
    },

    // Repond to the router moving to time machine
    _initTimeMachine: function(videoID, index) {
      var timemachine = this.views.timemachine = new TimeMachineDialogView({
        currentIndex: index,
        videoID: videoID
      });
      this.listenToOnce(timemachine, "error", function() {
        this.router.navigate("index", true);
        delete this.views.timemachine;
      });
      this.listenToOnce(timemachine, "close", function() {
        delete this.views.timemachine;
      });
    },

    _initStickyPlayer: function() {
      var $player = this.$player;
      var sticky = $player.data("sticky");
      var $panel = $("#panel");
      var $content = $("#panel > .h-content");
      var $doc = $(document);
      var $win = $(window);
      var TOP_PADDING = 20;
      var OFFSET_PIXELS = 30;
      var STICKY_NAV_HEIGHT = $("#header nav").height();
      var PAGE_WIDTH_MIN = app.consts.PAGE_WIDTH_MIN;
      var panelView = this.views.panel;

      var setContainerWidth = function(container) {
        var width = $content.width();
        container.style.width = width + "px";
        container.style.paddingTop = TOP_PADDING + "px";
        container.style.left = "50%";
        container.style.marginLeft = -(width / 2) + "px";
        container.style.zIndex = 1;
      };

      $win.on("scroll resize", function(event) {
        var obj = $player.data("sticky");
        var commentsPanelOpen = panelView.getOpenPanel().el.id === "panel-comments";

        // Only enable the sticky object if the page width is large enough
        // to make it work smoothly
        if(!commentsPanelOpen || window.innerWidth < PAGE_WIDTH_MIN) {
          console.warn('Not making the player sticky because we\'re less than the page in');
          obj.disable();
          return;
        } else if(commentsPanelOpen) {
          console.log('enabling sticky player');
          $player.data("sticky")._setCloneDimensions();
          obj.enable();
        }

        // Bail if there's no sticky object or if it's stuck
        if(!obj.stuck) {
          return;
        }

        var container = obj.$container[0];
        var docHeight = $doc.height();
        var panelOffsetTop = $panel.offset().top;
        var panelEdge = panelOffsetTop + $panel.height();
        var pageEdge = docHeight - (docHeight - $win.scrollTop());
        var visiblePanelPx = panelEdge - pageEdge - STICKY_NAV_HEIGHT;
        var diff = visiblePanelPx - obj.$container.outerHeight(true);

        if(event.type === "resize") {
          setContainerWidth(container);
        }

        // Once we're XX pixels from the bottom of the panel...
        if(diff - OFFSET_PIXELS < 0) {
          container.style.top = (diff + TOP_PADDING) + "px";
        } else {
          container.style.top = STICKY_NAV_HEIGHT + "px";
        }
      });

      $player.sticky({
        offset: TOP_PADDING,
        onStick: function($container) {
          setContainerWidth($container[0]);
        },
        onUnStick: function($container) {
          $container.removeAttr("style");
        }
      });

      $player.data("sticky").disable();
    },

    _resizePlayer: function(data) {
      var isContracted = data.panel.el.id === "panel-comments";
      this.views.panel.$el.toggleClass("is-contracted", isContracted);
    },

    _pausePlayer: function() {
      var player = this.views.player;
      var pause = function() { player.pause(); };

      if(player.getIsPlaying()) {
        pause();
      } else {
        player.on("begin", pause);
      }
    },

    _loadComments: function() {
      var panel = this.views.panel.getPanel("comments");
      
      if(!panel) {
        console.error('Unable to find the comments panel');
        return;
      }

      var videoID = this.$("#player-container").data("video-id");

      // Set number of comments in the nav item
      gigya.comments.getComments({
        categoryID: 'dev-testing',
        streamID: videoID,
        callback: function(resp) {
          panel.setNavItemText(resp.commentCount);
        }
      });

      // Load comments
      gigya.comments.showCommentsUI({
        categoryID: 'dev-testing',
        streamID: videoID,
        containerID: 'gigya-comments',
        cid: '',
        width: '100%',
        lang: app.context.lang,
        minShareOptions: 4,
        enabledShareProviders: 'facebook,twitter,linkedin,google'
      });
    },

    /**
     * When a recommended video is clicked on the id of the video is stored.
     * Once the video starts playing, send a heartbeat call for that video.
     */
    _sendTaboolaHeartbeat: function() {
      var clickedID = localStorage.getItem(app.consts.TABOOLA_CLICKED_VIDEO_ID);
      var session = app.utils.getTaboolaSession();
      var currVid = this.views.player.getCurrentVideo();
      var dest;

      if(!clickedID || !session) {
        return;
      }
      if(currVid && currVid.id !== parseInt(clickedID, 10)) {
        console.warn('Current video does not match clicked taboola id', currVid, clickedID);
        return;
      }

      // No longer need this
      localStorage.removeItem(app.consts.TABOOLA_CLICKED_VIDEO_ID);

      // Build API call  TODO move to collection
      dest = "http://api.taboola.com/1.1/json/{publisherId}";
      dest += "/recommendations.notify-click?app.type=desktop";
      dest += "&app.apikey={apikey}";
      dest += "&response.id={sessionid}";
      dest += "&response.session={session}";
      dest += "&item.type=video";
      dest += "&item.id={videoId}";

      // token replace
      dest = dest.replace("{publisherId}", app.consts.TABOOLA_PUBLISHER_ID)
        .replace("{apikey}", app.consts.TABOOLA_API_KEY)
        .replace("{session}", session.session)
        .replace("{sessionid}", session.id)
        .replace("{videoId}", clickedID);

      console.log('Sending taboola heartbeat to', dest);
      var img = document.createElement("img");
      img.src = dest;
      img.style.display = "none";
      img.onload = img.onerror = function() {
        img.parentNode.removeChild(img);
      };
      document.body.appendChild(img);
    }
  });

});
