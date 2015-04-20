/*
 * Player carousel subclass
 */

define([
  "app",
  "views/common/carousel"
], function(app, CarouselView) {
  "use strict";

  var PlayerCarouselView = CarouselView.extend({

    defaults: _.extend({}, CarouselView.prototype.defaults, {
      moveBy: 1,
      liPerPage: 1,
      pageContainer: ".page"
    }),

    initialize: function() {
      CarouselView.prototype.initialize.call(this);
      this.listenTo(this, "animateend", this._updatePage);
      this._updatePage();
    },

    _updatePage: function() {
      var jcarousel = this.getCarousel();
      var currIndex = jcarousel._fullyvisible.index();
      var total = jcarousel._items.length;

      this.$(this.options.pageContainer).text(function() {
        return "# of #"
          .replace("#", currIndex + 1)
          .replace("#", total);
      });
    },

    _setHeight: $.noop,
    _adjustLastItemWidths: $.noop,
    _adjustLastLiWidth: $.noop
  });

  return PlayerCarouselView;

});
