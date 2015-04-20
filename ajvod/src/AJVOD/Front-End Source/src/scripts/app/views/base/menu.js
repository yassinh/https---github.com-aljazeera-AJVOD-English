define([
  "app",
  "views/common/overlay"
], function(app, overlayView) {
  "use strict";
  var BaseView = app.views.BaseView;
  var FADE_SPEED = app.consts.OVERLAY_FADE_SPEED;
  var instances = [];
  var positionCache;

  var getOpenMenu = function() {
    var results = _.where(instances, { isOpen: true });
    return results ? results[0] : null;
  };

  $(window).on("resize.menu", function() {
    var openMenu = getOpenMenu();
    if(openMenu) openMenu._position();
  });

  $(window).on("scroll.menu", function() {
    var openMenu = getOpenMenu();
    if(openMenu) openMenu._position();
  });

  $(document).on("keyup.menu", function(event) {
    if(event.which === 27) {
      var openMenu = getOpenMenu();
      if(openMenu) openMenu.close();
    }
  });

  return BaseView.extend({
    isOpen: false,

    events: {
      "click": "_detectShouldClose"
    },

    defaults: {
      fade: true
    },

    initialize: function() {
      BaseView.prototype.initialize.call(this);

      this.options = _.extend({}, this.defaults, this.options);

      // The first time a menu is created, calculate and cache
      // dimensions we need to use for menu positioning
      if(!positionCache) {
        var $header = $("#header");
        var $nav = $header.find("nav").last();

        positionCache = {
          navTop: $nav.offset().top,
          navHeight: $nav.outerHeight(true),
          headerHeight: $header.height()
        };
      }

      

      instances.push(this);
     
        //hossam... remove BG color
      $(".your-channel").removeClass("theme-orange");
    },

    _initSubviews: function() {
      BaseView.prototype._initSubviews.call(this);
      this.views.overlay = overlayView();
    },

    toggle: function() {
      this[this.isOpen ? "close" : "open"]();
    },

    open: function() {
      // If there's a menu already open, close it but leave the
      // overlay open.
      var openMenu = getOpenMenu();

      if(openMenu) {
        openMenu.close({ closeOverlay: false });
      }

      this.$el.fadeIn(this.options.fade ? FADE_SPEED : 0);
      this.views.overlay.open();
      this.isOpen = true;
      this._position();
      this.trigger("open");
      app.trigger("menu:open", this.el.getAttribute("data-for"));
    },

    close: function(options) {
      options = _.extend({}, {
        closeOverlay: true
      }, options || {});

      // Close the overlay with this menu?
      if(options.closeOverlay) {
        this.views.overlay.close();
      }

      this.$el.fadeOut(this.options.fade ? FADE_SPEED : 0);
      this.isOpen = false;
      this.trigger("close");
      app.trigger("menu:close", this.el.id);
    },

    _detectShouldClose: function(event) {
      var target = event.target;
      var $target = $(target);
      var tagName = target.tagName;

      // Allow links to follow without closing the dialog.
      if(
        (tagName === "A" && !/close/.test(target.className)) ||
          target.parentNode.tagName === "A" ||
          target.parentNode.parentNode.tagName === "A" // This is a shortcut for a common case and avoids walking up the tree on each click
      ) {
        return;
      }

      event.preventDefault();

      // Close the menu if the click didn't originate on or inside a
      // <li> tag (for the menus) or a form (search, preferences)
      if(tagName !== "LI" && !$target.closest("li").length && !$target.closest("form").length) {
        this.close();
      }
    },

    _position: function() {
      if(!this.isOpen) return;

      var height = positionCache.navHeight;
      var offsetTop = positionCache.navTop + height;
      var scrollTop = (window.pageYOffset !== undefined) ?
        window.pageYOffset :
        (document.documentElement || document.body.parentNode || document.body).scrollTop;

      if(scrollTop >= offsetTop - height) {
        offsetTop = height + scrollTop;
      }

      var headerHeight = positionCache.headerHeight;
      var headerDiff = Math.max(headerHeight - scrollTop, height);
      this.$el.css("top", headerDiff);

      // calc height of menu
      // var overlayHeight = $(window).height() - headerDiff;
      // this.$el.css("height", overlayHeight);
    }


  });
});
