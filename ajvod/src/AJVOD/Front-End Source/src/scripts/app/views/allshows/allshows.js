/*
 * Top-level all shows page view
 */

define([
  "app",
  "views/common/page",
  "views/common/advancedmenucarousel"
], function(app, PageView, AdvancedMenuCarousel) {
  "use strict";

  return PageView.extend({
    _initSubviews: function() {
      PageView.prototype._initSubviews.call(this);
      this.$(".your-channel").removeClass("theme-orange");

      this.views.carousels = this.$(".carousel-advanced-menu").map(_.bind(function(i, elem) {
        return new AdvancedMenuCarousel({ el: elem });
      }, this));
      
       
    }
  });

});
