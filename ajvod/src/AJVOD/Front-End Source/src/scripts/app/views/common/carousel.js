// Carousel factory view
// This view acts as an API behind the real
// carousel plugin.
//
// A carousel view should be initialize on the very top
// most container that contains:
//  1. a .prev button
//  2. a .next button
//  3. a .carousel-hook element (the actual carousel)

define([
  "app",
  "views/common/selectmenu",
  "sortElements"
], function(app, SelectMenuView) {
  "use strict";

  var $win = $(window);
  var isIE = $("html").hasClass("is-ie");
  var ACTIVE_CLASS = app.consts.ACTIVE_CLASS;
  var DISABLED_CLASS = app.consts.DISABLED_CLASS;
  var BaseView = app.views.BaseView;

  return BaseView.extend({

    // default sorting order
    sortOrder: "date",

    // track the current page
    currentIndex: 0,

    itemClass: ".item",

    // Event listeners on the carousel container
    events: function() {
      var isRTL = this.options.rtl;

      return {
        "click .next": (isRTL ? "prev" : "next"),
        "click .prev": (isRTL ? "next" : "prev"),
        "change select": "_sort"
      };
    },

    // Carousel default options
    defaults: {
      autoScroll: false,
      autoScrollSpeed: 1000,
      moveBy: 4.5, // Number of pages to move by on prev/next
      liPerPage: 4.5, // Number of <li> per page. ONLY used in calculating pagination. Control is in CSS.
      lazyLoadImages: true,
      pagination: true,
      transitions: false,
      rtl: app.context.isRTL, // Use the global RTL setting by default
      refreshOnResize: true,
      list: function() {
        return this.element().children("ul").eq(0);
      }
    },

    initialize: function() {
      BaseView.prototype.initialize.call(this);

      // Gather carousel-specific options.
      // Full list @ http://sorgalla.com/jcarousel/docs/reference/configuration.html
      this.options = _.extend({}, this.defaults, this.options.options || {});

      // Cache useful selectors
      this._cacheElems();

      // Set the height immediately
      this._setHeight();

      // Handle failed images
      this.$items.find("img").on("error", this._onImageError);

      console.log('items :::', this.$items);

      // Look for a RTL override on the carousel itself
      if(this.el.hasAttribute("dir")) {
        this.options.rtl = this.$el.attr("dir") === "rtl";
      }

      var hasMultiplePages = this._hasMultiplePages();

      // Cache pagination elements
      if(hasMultiplePages) {
        this.$pagination = this.$("footer ul");
        this.$nextprev = this.$(".next-prev");
        this.$next = this.$nextprev.find(".next");
        this.$prev = this.$nextprev.find(".prev");
      }

      // Proxy carousel events onto this object
      // Reference: http://sorgalla.com/jcarousel/docs/reference/events.html
      _.each("createend scroll scrollend animate animateend".split(" "), function(evt) {
        this.$carousel.on(evt + ".jcarousel", _.bind(function() {
          this.trigger.apply(this, [ evt ].concat([].slice.call(arguments)));
        }, this));
      }, this);

      // Setup lazy loading images?
      if(this.options.lazyLoadImages) {
        var imgs = this.$carousel.find(".thumb img").addClass("h-fade-in");

        // IE can't calculate the height of the UL element properly
        // when these images are visible, so hide them.
        if(isIE) {
          imgs.hide();
        }
      }

      // Register listeners
      this._bindEventListeners();

      // Set the ul to absolute positioning. Doing this here instead of
      // in the CSS in order to avoid a FOUC; the ul has a 0 height
      // when absolutely positioned.
      if(this.$ul.length) {
        this.$ul[0].style.position = "absolute";
      }

      // Create the carousel instance
      this.$carousel.jcarousel(this.options);

      // Workaround a bug in jCarousel where if there is only one list
      // item in RTL mode, it's left aligned and not right aligned.
      // TODO: fix this properly. ALJ-609
      if(this.options.rtl && this.$li.length === 1) {
        this.$ul.css({ right: 0, left: "auto" });
      }

      // Unbind jCarousel's resize handler
      $win.off("resize.jcarousel");

      // Select boxes
      this.views.selectmenu = new SelectMenuView({
        el: this.$("select")
      });

      // Enable the auto scroll plugin?
      if(this.options.autoScroll && hasMultiplePages) {
        this.$carousel.jcarouselAutoscroll({
          carousel: this.$carousel,
          interval: this.options.autoScrollSpeed
        });
      }

      // Enable the pagination plugin?
      if(hasMultiplePages) {
        this._createPagination();
      }

      // Adjust the margin/width of the last item in the carousel
      // so that things line up correctly.
      this._adjustLastLiWidth();
      this._adjustLastItemWidths();
    },

    /**
     * Public API
     */

    // Move carousel to the next slide
    next: function(event) {
      if(event) { event.preventDefault(); }
      this.$carousel.jcarousel("scroll", "+=" + this.options.moveBy);
    },

    // Move carousel to the previous slide
    prev: function(event) {
      if(event) { event.preventDefault(); }
      this.$carousel.jcarousel("scroll", "-=" + this.options.moveBy);
    },

    scrollToElem: function($elem) {
      this.$carousel.jcarousel("scroll", $elem);
    },

    // Returns the jCarousel object
    getCarousel: function() {
      return this.$carousel.data("jcarousel");
    },

    // Get the currently visible list items
    getVisible: function() {
      return this.$carousel.jcarousel("visible");
    },

    // Resume the auto scroll timer
    start: function() {
      if(this._hasAutoScroll()) {
        this.$carousel.jcarouselAutoscroll("start");
      }
    },

    // Stop the auto scroll timer
    stop: function() {
      if(this._hasAutoScroll()) {
        this.$carousel.jcarouselAutoscroll("stop");
      }
    },

    // Used by tests
    getTarget: function() {
      return this.$carousel.jcarousel("target");
    },

    // Remove an item from the carousel
    // This method assumes that there is one item per li, so the
    // entire li is removed.
    removeItem: function($item, callback) {
      var $li = $item.parent();
      var $siblings = this.$li.not($li);

      $item.fadeOut(_.bind(function() {
        $li.parent().css("width", (($siblings.length / 4.5) * 100) + "%");
        $siblings.css("width", (100 / $siblings.length) + "%");
        $li.remove();

        this._cacheElems();
        this.$items.last().removeAttr("style");
        this._adjustLastLiWidth();
        this._adjustLastItemWidths();

        if(this.$li.length <= this.options.liPerPage) {
          this.destroyPagination();
        }

        this._refresh();
        this._cacheElems();
        this._lazyload();

        callback(this.$items);
      }, this));
    },

    // Destroy - used by unit tests
    destroy: function() {
      if(this._hasAutoScroll()) {
        this.$carousel.jcarouselAutoscroll("destroy");
      }

      $win.off(".carousel" + this.cid);
      this.destroyPagination();
      this.$carousel.jcarousel("destroy");
      this.stopListening();
      this.undelegateEvents();
      // this.remove();
    },

    destroyPagination: function() {
      if(this.$pagination && this.$pagination.data("jcarouselPagination")) {
        this.$pagination.jcarouselPagination("destroy");
        this.$nextprev.removeClass("run-animation");
      }
    },

    /**
     * Private methods
     */

    _cacheElems: function() {
      this.$carousel = this.$(".carousel-hook");
      this.$ul = this.$carousel.find("ul");
      this.$li = this.$carousel.find("li");
      this.$items = this.$li.find(this.itemClass);
    },

    _setHeight: function() {
      this.$carousel.height(this.$ul.outerHeight());
    },

    _hasAutoScroll: function() {
      return this.options.autoScroll && this.$carousel.data("jcarouselAutoscroll");
    },

    _hasMultiplePages: function() {
      return this.options.pagination && this.$li.length > this.options.liPerPage;
    },

    _refresh: function(recache) {
      this.$carousel.jcarousel("reload");

      if(recache === true) {
        this._cacheElems();
      }
      if(this._hasMultiplePages()) {
        this.$pagination.jcarouselPagination("reload");
      }
    },

    _bindEventListeners: function() {
      // Lazy-load images in on movement
      if(this.options.lazyLoadImages) {
        this.on("scrollend createend", this._lazyload, this);
      }

      // Update the active pagination circles
      if(this._hasMultiplePages()) {
        // Toggle next/prev arrow disabled state
        if(this.options.wrap !== "circular") {
          this.on("scrollend createend", this._updateArrowState, this);
        }

        // If we're displaying and moving by fractions use a different
        // code path to update the pagination since jcarousel doesn't
        // handle this natively
        if(this.options.moveBy % 1 !== 0 || this.options.liPerPage % 1 !== 0) {
          this.on("scroll", this._calculatePagination, this);

        // otherwise use jcarousel's api
        } else {
          this.$pagination.on("active.jcarouselpagination", "li", _.bind(function(event) {
            this.currentIndex = $(event.currentTarget).index();
            this._updatePagination();
          }, this));
        }

        // When clicking directly on a pagination circle set that one
        // as the active circle.
        this.$pagination.on("click", "li", _.bind(this._paginationClickHandler, this));
      }

      // Re-adjust height once created. Settimeout to ensure the calculations
      // can be made correctly. FIXME!
      this.on("createend", function() {
        setTimeout(_.bind(this._setHeight, this), 100);
      }, this);

      // Re-apply ellipsis/height on resize
      $win.on("resize.carousel" + this.cid, _.throttle(_.bind(function() {
        if(this.options.refreshOnResize) {
          this._setHeight();
          this._refresh();
        }
      }, this), 50));
    },

    _paginationClickHandler: function(event) {
      event.preventDefault();
      this.currentIndex = $(event.currentTarget).index();
      this._updatePagination();
    },

    // Calculates which pagination circle should be active if the
    // moveBy option is a fraction. jCarousel can't handle it.
    _calculatePagination: function(event, jcarousel, target) {
      // Bail if the target is a jquery object
      if(_.isObject(target)) {
        return;
      }

      var $pages = this.$pagination.find("li");

      if(typeof target === "string") {
        this.currentIndex += (target.charAt(0) === "+" ? 1 : -1);
      }

      if(this.currentIndex > $pages.length - 1) {
        this.currentIndex = $pages.length - 1;
      } else if(this.currentIndex < 0) {
        this.currentIndex = 0;
      }

      this._updatePagination();
    },

    _updateArrowState: function() {
      var hasNext = this.$carousel.jcarousel("hasNext");
      var hasPrev = this.$carousel.jcarousel("hasPrev");
      this.$next.toggleClass(DISABLED_CLASS, this.options.rtl ? !hasPrev : !hasNext);
      this.$prev.toggleClass(DISABLED_CLASS, this.options.rtl ? !hasNext : !hasPrev);
    },

    _createPagination: function(opts) {
      opts = $.extend({}, {
        carousel: this.$carousel,
        perPage: this.options.moveBy,
        item: function(page) {
          return '<li' + (parseInt(page, 10) === 1 ? ' class="' + ACTIVE_CLASS + '"' : "") + '><a href="#' + page + '"></a>';
        }
      }, opts || {});

      this.$pagination.jcarouselPagination(opts);

      setTimeout(_.bind(function() {
        this.$nextprev.addClass("run-animation");
      }, this), 50);
    },

    _updatePagination: function() {
      var $pages = this.$pagination.find("li");
      var $active = $pages.eq(this.currentIndex);
      $active.addClass(ACTIVE_CLASS);
      $active.siblings().removeClass(ACTIVE_CLASS);
    },

    _lazyload: function() {
      var $visible = this.$carousel.jcarousel("visible");

      if(!$visible) {
        return;
      }

      $visible.find(".thumb img").each(function(i, elem) {
        var $elem = $(elem);

        if(isIE) {
          $elem.show();
        }

        if(this.complete) {
          $elem.addClass("h-fade-in-run");
        } else {
          $(this).one("load", function() {
            $elem.addClass("h-fade-in-run");
          });
        }

        var newSrc = $elem.data("src");

        if(this.src !== newSrc) {
          $elem.attr("src", newSrc);
        }
      });
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
        console.log('main ' + event.currentTarget.value);
        this.sortOrder = event.currentTarget.value;
      this.$items.sortElements(_.bind(this._comparator, this));
      this.$carousel.jcarousel("scroll", 0, false);
      this.currentIndex = 0;

      if(this._hasMultiplePages()) {
        this._updatePagination();
      }

      this._adjustLastItemWidths();
    },

   
    // This method adjusts the width of the last list item and
    // each .item element inside it to ensure all everything aligns
    // flush with their edges
    _adjustLastItemWidths: function() {
      this.$items.removeAttr("style");
      var $last = this.$li.last();
      var itemWidth = this._calculateLastLiItemsWidths();

      if(itemWidth != null) {
        $last.find(this.itemClass).css("width", itemWidth - 2 + "%");
      }
    },

    _adjustLastLiWidth: function() {
      var $last = this.$li.last();
      var liWidth = this._calculateLastLiWidth();

      if(liWidth != null) {
        $last.css("width", liWidth + "%");
      }
    },

    // These calculation functions are meant to be overwritten
    // by each carousel implementation

    _calculateLastLiWidth: function() {
      var $last = this.$li.last();
      var liWidth = $last.width();
      var itemWidth = $last.find(this.itemClass).outerWidth();
      var totalWidth = this.$li.width() * this.$li.length; // total width of the carousel
      var diff = liWidth - itemWidth;
      var newWidth = liWidth - (liWidth * (diff / liWidth)); // new width of the last item in px
      return (newWidth / totalWidth) * 100; // new width expressed in percent
    },

    _calculateLastLiItemsWidths: function() {
      var $lastLi = this.$li.last();
      var $lastItems = $lastLi.find(this.itemClass);
      return (100 / $lastItems.length);
    },

    _onImageError: function(event) {
      event.currentTarget.src = app.webroot + "/images/thumbnail-missing.jpg";
    }
  });
});
