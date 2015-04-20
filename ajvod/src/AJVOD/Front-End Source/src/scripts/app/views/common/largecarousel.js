/*
 * Large carousel subclass
 */

define([
  "app",
  "views/common/carousel",
  "sortElements"
], function(app, CarouselView) {
  "use strict";

  var hasLastChild = Modernizr.lastchild;
  var sortOrder;

  var LargeCarouselView = CarouselView.extend({
    defaults: _.extend({}, CarouselView.prototype.defaults, {
      moveBy: 2.25,
      liPerPage: 2.25
    }),

    initialize: function() {
      // FIXME!
      this.on("createend", function() {
        setTimeout(_.bind(this._adjustPromoItemHeight, this), 100);
      }, this);

      CarouselView.prototype.initialize.apply(this, arguments);
      console.log('Large carousel initialized', this.el);

      this._order();
      this._addCSS3Classes();
      //this._sortRecent();

      if(!hasLastChild) {
        this._adjustLastLiWidth();
        this._adjustLastItemWidths();
        this.$carousel.jcarousel("reload");
      }

      // Set the promo height
      this._adjustPromoItemHeight();
      $(window).on("resize", _.bind(this._adjustPromoItemHeight, this));
    },

    _adjustPromoItemHeight: function() {
      var $promo = this.$li.filter(".is-promo").find(".item");
      $promo.height(this.$li.eq(1).height() - 8); // 8px = 2px border * 4
    },

    _calculateLastLiWidth: function() {
      var $last = this.$li.last();
      var $lastItems = $last.find(".item");
      var numItems = $lastItems.length;
      var liWidth = $last.width();
      var totalWidth = this.$li.width() * this.$li.length; // total width of the carousel
      var itemWidth, diff, newWidth;

      if(hasLastChild && numItems <= 2) {
        itemWidth = $last.find(".item").width();
      } else if(hasLastChild && numItems > 2) {
        itemWidth = $last.find(".item").outerWidth();
      } else {
        itemWidth = $last.find(".item").outerWidth(true);
      }

      // If this li is full size, double the width of the item. Otherwise the
      // li is half size (only 2 .items) so divide the li width by 2.
      if(numItems > 2) {
        itemWidth = (itemWidth * 2);
      } else {
        liWidth = liWidth / 2;
      }

      diff = liWidth - itemWidth; // width of the margin in pixels
      newWidth = liWidth - (liWidth * (diff / liWidth)); // new width of the last item in px

      // On modern browsers where the number of items is <=2, subtract more
      // from the width.
      if(hasLastChild && numItems <= 2) {
        newWidth -= (diff / 2) - 1;
      }

      // new width expressed in percent
      return (newWidth / totalWidth) * 100;
    },

    _calculateLastLiItemsWidths: function() {
      var $items = this.$li.last().find(".item");
      return $items.length <= 2 ? 100 : null;
    },

    _addCSS3Classes: function() {
      var classes = ["is-first-child", "is-last-child", "one-col"];

      _(Math.max(4, this.$li.length)).times(function(n) {
        classes[classes.length] = "is-nth-child-" + n;
      });

      // remove all classes
      this.$li
        .find(".item")
        .addBack()
        .removeClass(classes.join(" "));

      // Add useful classes for browsers that don't support CSS3 selectors
      this.$li.each(_.bind(function(index, elem) {
        var $elem = $(elem);
        var $items = $elem.find(".item");

        if($items.length <= 2) {
          $elem.addClass("one-col");
        }

        // Add all the other classes in old browsers
        if(!hasLastChild) {
          if(index === 0) {
            $elem.addClass("is-first-child");
          }
          if(index === this.$li.length - 1) {
            $elem.addClass("is-last-child");
          }

          $items.each(function(itemIndex, itemElem) {
            var $itemElem = $(itemElem);
            $itemElem.addClass("is-nth-child-" + (itemIndex + 1));

            if(itemIndex === 0) {
              $itemElem.addClass("is-first-child");
            }
            if(itemIndex === $items.length - 1) {
              $itemElem.addClass("is-last-child");
            }
          });
        }
      }, this));
    },

    // Order the items newspaper column style
    _order: function() {
      this.$li.each(function() {
        var divs = $(this).children();
        divs.eq(1).insertAfter(divs.eq(1).next());
      });
    },

    _refresh: function() {
      CarouselView.prototype._refresh.apply(this, arguments);
      this._addCSS3Classes();
    },

    _comparator: function (a, b) {
        var indexA = $(a).data(this.sortOrder);
        var indexB = $(b).data(this.sortOrder);

        // If date sorting we need to create date objects.
        if (this.sortOrder === "date") {
            indexA = new Date(indexA);
            indexB = new Date(indexB);
            return indexA.getTime() < indexB.getTime() ? 1 : -1;
        }

        return indexA < indexB ? 1 : -1;
    },

    _sort: function (event) {
        console.log('large ' + event.currentTarget.value);
      this.sortOrder = event.currentTarget.value;

      // Sort em
      this.$items.sortElements(_.bind(this._comparator, this));

      // re-order the DOM nodes newspaper style
      this._order();

      // refresh the carousel
      this._refresh(true);

      // Go back to the beginning
      this.$carousel.jcarousel("scroll", 0, false);
      this.currentIndex = 0;

      // update pagination circles
      if(this._hasMultiplePages()) {
        this._updatePagination();
      }

      this._adjustLastItemWidths();
    },

      _sortRecent: function () {
          console.log('large date custom');
          this.sortOrder = 'date';

          // Sort em
      this.$items.sortElements(_.bind(this._comparator, this));

          // re-order the DOM nodes newspaper style
      this._order();

          // refresh the carousel
      this._refresh(true);

          // Go back to the beginning
      this.$carousel.jcarousel("scroll", 0, false);
      this.currentIndex = 0;

          // update pagination circles
      if(this._hasMultiplePages()) {
          this._updatePagination();
      }

      this._adjustLastItemWidths();
  }
  });



  return LargeCarouselView;

});
