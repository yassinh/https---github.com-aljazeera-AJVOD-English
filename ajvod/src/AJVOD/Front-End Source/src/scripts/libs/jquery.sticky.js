// This depends on jQuery and Underscore

(function($) {
  // We'll be using this reference a lot
  var $win = $(window);

  // Top offset for each sticky element so they stack below each other
  var stackOffset = 0;

  // Number of sticky instances created
  var instances = 0;

  function Sticky(elem, options) {
    this.id = instances = instances + 10;
    this.$elem = $(elem);
    this.options = options;
    this.stackOffset = stackOffset;
    this.stuck = false;
    this._init();
  }

  Sticky.prototype = {
    _init: function() {
      this._prepareClone();
      this._prepareElement();

      // If this instance should stack below other instances...
      if(this.options.stacks) {
        stackOffset += this.$elem.outerHeight(true);
        this.$container.addClass(this.options.stacksClass);

        this.$elem.one("stick", $.proxy(function() {
          this._adjustZindex();
        }, this));
      }

      this.enable();
      this.options.onCreate.call(this, this.$container);
    },

    // Clone the element to use when the real element is in sticky mode.
    // The clone will take its place in the flow so things don't jump
    // around.
    _prepareClone: function() {
      var $elem = this.$elem;
      var $clone = this.$clone = $elem.clone();
      this._setCloneDimensions();
      $clone.removeAttr("class");
      // $clone.removeData();
      $clone.attr("data-sticky-clone", true);
      $clone.removeAttr("id");
      $clone.hide();
      $clone.empty();
      $clone.insertBefore($elem);
    },

    _setCloneDimensions: function() {
      this.$clone.width(this.$elem.outerWidth());
      this.$clone.height(this.$elem.outerHeight());
    },

    _prepareElement: function() {
      this.$elem.wrap('<div class="' + this.options.containerClass + '"></div>');
      this.$container = this.$elem.parent();
    },

    _calculate: function() {
      var stackOffset = this.options.stacks ?
        this.stackOffset :
        0;

      var offset = this.$container.hasClass(this.options.fixedClass) ?
        this.$clone.offset() :
        this.$elem.offset();

      var scrollTop = $win.scrollTop();
      var isVisible = scrollTop >= (offset.top - this.options.offset - stackOffset);
      this[isVisible ? "stick" : "unstick"]();

      if(isVisible) {
        this.$clone.width(this.$elem.outerWidth());
      }
    },

    _adjustZindex: function(val) {
      if(val == null) {
        val = parseInt(this.$container.css("z-index"), 10) + this.id;
      }

      this.$container.css("z-index", val);
    },

    stick: function() {
      if(this.stuck) return;
      this.$clone.show();
      this.$container.css("top", this.stackOffset + "px");
      this.$container.addClass(this.options.fixedClass);
      this.stuck = true;
      this.options.onStick.call(this, this.$container);
      this.$elem.trigger("stick");
    },

    unstick: function() {
      if(!this.stuck) return;
      this.$clone.hide();
      this.$container.removeClass(this.options.fixedClass);
      this.stuck = false;
      this.options.onUnStick.call(this, this.$container);
      this.$elem.trigger("unstick");
    },

    enable: function() {
      var events = "scroll.sticky" + this.id;
      events += " resize.sticky" + this.id;
      events += " touchmove.sticky" + this.id;
      $win.on(events, $.proxy(this._calculate, this));
    },

    disable: function() {
      $win.off(".sticky" + this.id);

      if(this.stuck) {
        this.unstick();
      }
    },

    destroy: function() {
      this.disable();
      this.$clone.remove();
      stackOffset -= this.stackOffset;
      // TODO: unwrap
    }
  };

  // Expose as a jQuery plugin
  $.fn.sticky = function(opts) {
    return this.each(function() {
      if($.data(this, "sticky")) {
        return $.data(this, "sticky");
      }

      var options = $.extend(
        {},
        $.fn.sticky.defaults,
        opts || {}
      );

      return $.data(this, "sticky", new Sticky(this, options));
    });
  };

  // Default options
  $.fn.sticky.defaults = {
    stacks: true,
    offset: 0,
    containerClass: "h-sticky",
    fixedClass: "h-sticky-on",
    stacksClass: "h-sticky-stacks",
    onStick: $.noop,
    onUnStick: $.noop,
    onCreate: $.noop
  };

})(jQuery);
