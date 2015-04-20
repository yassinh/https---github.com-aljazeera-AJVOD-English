/*
 * Top-level show page view
 */

define([
  "app",
  "views/common/videopage",
  "views/common/carousel",
  "views/common/player",
  "views/common/largecarousel",
  "views/common/panel"
], function(app, VideoPageView, CarouselView, PlayerView, LargeCarouselView, PanelView) {
  "use strict";

  return VideoPageView.extend({
    _initSubviews: function() {
      VideoPageView.prototype._initSubviews.call(this);

      this.views.largeCarousel = new LargeCarouselView({
        el: this.$(".carousel-large")
      });

      this.views.smallCarousels = this.$(".carousel").map(_.bind(function(i, elem) {
        return new CarouselView({ el: elem });
      }, this));

      this.views.panel = new PanelView({
        el: $("#banner-top"),
        fade: true,
        panels: ["main", "sharing"]
      });

      this.views.player = new PlayerView({
        el: $("#player"),
        automute: true
      });
    }
  });

});
