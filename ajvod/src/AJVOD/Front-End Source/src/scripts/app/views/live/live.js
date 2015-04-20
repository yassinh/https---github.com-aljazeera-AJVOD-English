/*global twttr*/
/*
 * Top-level show page view
 */

define([
  "app",
  "views/common/videopage",
  "views/common/carousel",
  "views/common/player",
  "views/common/panel",
  "views/common/episodepreviewdialog"
], function(app, VideoPageView, CarouselView, PlayerView, PanelView, EpisodePreviewDialog) {
  "use strict";

  // The top/bottom panel margin
  var SPACING = 40;

  // Inject twitter dependency
  window.twttr = (function (d,s,id) {
    var t, js, fjs = d.getElementsByTagName(s)[0];
    if (d.getElementById(id)) { return; } js=d.createElement(s); js.id=id;
    js.src="https://platform.twitter.com/widgets.js"; fjs.parentNode.insertBefore(js, fjs);
    return window.twttr || (t = { _e: [], ready: function(f){ t._e.push(f); } });
  }(document, "script", "twitter-wjs"));

  return VideoPageView.extend({
    events: _.extend({}, VideoPageView.prototype.events, {
      "click #schedule .title": "_openPreviewDialog"
    }),

    initialize: function() {
      VideoPageView.prototype.initialize.call(this);

      this._cacheElems();

      twttr.ready(_.bind(function() {
        this._renderTimeline();
      }, this));
    },

    _cacheElems: function() {
      this.$container = this.$("#panel");
      this.$player = this.$("#player-container");
      this.$schedule = this.$("#schedule");
      this.$epg = this.$("#epg");
    },

    _bindEvents: function() {
      VideoPageView.prototype._bindEvents.call(this);
      this.listenTo(app, "panel:open", this._resizePlayer);

      this.listenTo(app, "menu:open", function() {
        if(this.views.dialog) {
          this.views.dialog.close();
        }
      });
    },

    _initSubviews: function() {
      VideoPageView.prototype._initSubviews.call(this);

      this.views.carousels = this.$(".carousel").map(_.bind(function(i, elem) {
        return new CarouselView({ el: elem });
      }, this)).get();

      this.views.panel = new PanelView({
        el: $("#panel"),
        panels: ["main", "twitter", "sharing"]
      });

      this.views.player = new PlayerView({
        el: $("#player")
      });
    },

    _renderTimeline: function() {
      var $target = $("#twitter-timeline");

      twttr.widgets.createTimeline(
        $target.data("widget-id"),
        $target[0],
        _.bind(this._resizePlayer, this),
        {
          width: '100%',
          height: 407,
          chrome: "noborders noheader"
        }
      );
    },

    _openPreviewDialog: function(event) {
      // Allow links to pass
      // if(event.target.tagName === "A") {
        // return;
      // }

      var dialog = this.views.dialog;

      // Lazy load the creation of this thing
      if(!dialog) {
        dialog = this.views.dialog = new EpisodePreviewDialog({
          width: 600,
          destroyOnClose: false,
          show: 150,
          hide: 150
        });
      }

      var $target = $(event.currentTarget.parentNode);
      var context = $target.data();

      console.log('context is', context);

      if(dialog.isOpen() && dialog.options.context.videoId === context.videoId) {
        console.warn('Dialog is already open for this video');
        return;
      }

      var $show = $target.find(".show");
      context.title = $target.find(".title").text();
      context.show = $show.text();
      context.showLink = $show.attr("href");

      dialog.options.context = context;
      dialog.render();
      dialog.open();
    },

    _resizePlayer: function(data) {
// <<<<<<< HEAD
      var openPanel = this.views.panel.getOpenPanel();
      var isPlayerSmall = openPanel.el.id !== "panel-main";
      var $schedule = this.$schedule;
      var $player = this.$player;

      if(isPlayerSmall) {
        $schedule.addClass("is-visible");

        this.$player.animate({
          width: "60%"
        }, 200, function() {
          $schedule.removeAttr("style");
        });
      } else {
        $schedule.removeClass("is-visible");

        this.$player.animate({
          width: "100%"
        }, 200, function() {
          $schedule.removeAttr("style");
        });
// =======
      // var panelView = this.views.panel;
      // var openPanel = panelView.getOpenPanel();
      // var flag = openPanel.el.id !== "panel-main";

      // // When the player contracts set the height of the entire container
      // // to the height of the open panel + margins
      // if(flag) {
        // var minHeight = openPanel.$el.outerHeight(true) + (SPACING * 2);
        // this.$container.css("min-height", minHeight);
// >>>>>>> develop-epic-6
      }
    }
  });

});
