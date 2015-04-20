/*
 * Top-level channel page view
 */

define([
  "app",
  "views/common/videopage",
  "views/common/carousel",
  "views/common/largecarousel",
  "views/common/featuredcarousel"
], function(app, VideoPageView, CarouselView, LargeCarouselView, FeaturedCarouselView) {
  "use strict";

  return VideoPageView.extend({
    _initSubviews: function() {
      VideoPageView.prototype._initSubviews.call(this);

      // Featured carousel
      this.views.featuredCarousel = new FeaturedCarouselView({
        el: this.$(".carousel-featured")
      });

      // Large carousel
      this.views.largeCarousel = new LargeCarouselView({
        el: this.$(".carousel-large")
      });

      // Create a view for each small carousel on the page
      this.views.smallCarousel = this.$(".carousel").map(_.bind(function(i, elem) {
        return new CarouselView({ el: elem });
      }, this)).get();
    }
  });

});
