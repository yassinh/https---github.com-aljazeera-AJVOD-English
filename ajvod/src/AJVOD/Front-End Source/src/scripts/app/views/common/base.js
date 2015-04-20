/**
 * This is the base view from which all other views
 * should extend.
 */
define([ "app" ], function(app) {
  "use strict";

  return Backbone.View.extend({
    constructor: function() {
      this.views = {};
      Backbone.View.apply(this, arguments);
    },

    initialize: function() {
      this._initSubviews();
      this._bindEvents();
      this._genClassicView();

     
    },

    _genClassicView: function () {

        if ($('.ToggleControl').length === 0) {
            $('body').append('<div class=" ToggleControl"><div class="BetaWord">BETA</div></div>');

            var $el = $('.ToggleControl');
            var $window = $(window);

            $window.bind("scroll resize", function () {
                $el.css({
                    top: 0 + 'px',
                    bottom: "auto",
                    position: "fixed"
                });
            }).scroll();
        }
    },

    _initSubviews: function() {
      // No-op. Overwrite this to initialize any subviews.
    },

    _bindEvents: function() {
      // No-op. Overwrite this to init event listeners
    },

    /**
     * Initialize small, helper tooltips on various links/help
     * icons throughout the VOD portal.
     * @private
     * @param $context {jQuery object} An ancestor containing element that contains
     * elements with a title attribute.
     */
    _initTitleTooltips: function($context) {
      $context = $context || this.$el;

      $context.find("[title]").tooltip({
        tooltipClass: "tooltip-title",
        show: {
          delay: 250
        },
        beforeopen: function(event) {
          // jQuery UI dialog will automatically reassign focus to the element
          // that had focus when the dialog was opened. If that element is a tooltip
          // trigger than the tooltip will appear even though your mouse isn't over it.
          if(event.originalEvent.type === "focusin") {
            return false;
          }
        },
        position: {
          my: "center bottom-20",
          at: "center top",
          collision: "flip",
          using: function(position) {
            $(this).css(position);
            $("<div />").addClass("arrow h-icon h-icon-tooltip-arrow-sm").appendTo(this);
          }
        }
      });
    },

    /**
     * Initialize the large, item tooltips on individual videos.
     * @private
     * @param $context {jQuery object} An ancestor containing element that contains
     * elements with a title attribute.
     * @param opts {Object} Object of tooltip options
     *
     * TODO: improve this API, passing containerSelector is lame
     */
    _initItemTooltips: function($context, opts, containerSelector) {
      // Shift args
      if($.isPlainObject($context)) {
        opts = $context;
        $context = null;
      }
      if(!$context) {
        $context = this.$el;
      }
      if(!containerSelector) {
        containerSelector = "li";
      }

      opts = _.extend({}, {
        items: ".item",
        position: {
          my: "center bottom-5",
          at: "center top",
          collision: "fit",
          using: function(position) {
            position.width = 300;
            $(this).css(position);
            $("<div />").addClass("arrow h-icon h-icon-tooltip-arrow").appendTo(this);
          }
        },
        show: {
          delay: 500
        },
        beforeopen: function(event) {
          // Don't open the tooltip if the mouse is sitting on an add/remove
          // from playlist icon
          if(event.originalEvent.target.className.indexOf("h-icon-playlist") > -1) {
            return false;
          }
          if(event.originalEvent.type === "focusin") {
            return false;
          }
        },
        content: function() {
          return $(this).find(".tooltip").html();
        }
      }, opts);

      $context.find(".item").closest(containerSelector).each(function() {
        var $elem = $(this);

        // No tooltips in carousel promo list items
        if(this.className.indexOf("is-promo") > -1 || !$elem.find(".tooltip").length) {
          return;
        }

        $elem.tooltip(opts);
      });
    }

  });

});
