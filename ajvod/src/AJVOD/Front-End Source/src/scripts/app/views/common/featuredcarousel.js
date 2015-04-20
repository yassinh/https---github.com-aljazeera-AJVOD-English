/*
 * Featured carousel subclass
 */

define([
  "app",
  "views/common/carousel"
], function(app, CarouselView) {
  "use strict";

  var FeaturedCarouselView = CarouselView.extend({

    defaults: _.extend({}, CarouselView.prototype.defaults, {
      lazyLoadImages: false,
      wrap: "circular",
      autoScroll: true,
      autoScrollSpeed: 7000,
      liPerPage: 1,
      moveBy: 1,
      fadeSlidesInTimeout: 50
    }),

    initialize: function() {
      CarouselView.prototype.initialize.apply(this, arguments);

      // Stop auto scroll on next/prev
      this.$el.on("click", ".next, .prev", _.bind(this._stopAutoScroll, this));

      // Kick off animations immediately and once a slide transitions
      this.listenTo(this, "scrollend", function(event, data) {
        data._items.not(data._fullyvisible).removeClass("run-animation");
        data._fullyvisible.addClass("run-animation");
      });

      // Fade the slide content in
      setTimeout(_.bind(function() {
        this.getCarousel()._fullyvisible.addClass("run-animation");
      }, this), this.options.fadeSlidesInTimeout);

      // pause on mouseenter, resume on mouseleave
      this.$el.on("mouseenter mouseleave", _.bind(function(event) {
        var isEnter = event.type === "mouseenter";

        if(this._hasAutoScroll()) {
          this[isEnter ? "stop" : "start"]();
        }
      }, this));
    },

    _stopAutoScroll: function() {
      if(this.$carousel.data("jcarouselAutoscroll")) {
        this.$carousel.jcarouselAutoscroll("destroy");
      }
    },

    // Disable some base carousel functionality
    _updateArrowState: $.noop,
    _setHeight: $.noop,
    _adjustLastLiWidth: $.noop,
    _adjustLastItemWidths: $.noop

  });

  return FeaturedCarouselView;

});
