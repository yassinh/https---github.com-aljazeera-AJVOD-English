// select menu view
// one instance of this view per select element!

define([
  "app",
  "jqueryui",
  "jqueryuiselect"
], function(app) {
  "use strict";

  var BaseView = app.views.BaseView;
  var $win = $(window);

  return BaseView.extend({
    isOpen: false,

    defaults: {
      buttonIcon: "h-icon-selectarrow",
      theme: "default" // default || schedule
    },

    initialize: function() {
      BaseView.prototype.initialize.call(this);

      var self = this;
      var $el = this.$el;
      var options = _.extend({}, this.defaults, this.options);

      var position = {};
      position.my = "left top+1";
      position.at = "left bottom";

      if(app.context.isRTl) {
        position.my = "right top+1";
        position.at = "right bottom";
      }

      // create widget
      $el.selectmenu({
        icons: {
          button: options.buttonIcon
        },
        position: position,
        open: function(event, ui) {
          ui.button.addClass("active");
          self.trigger("open");
          self.isOpen = true;
        },
        close: function(event, ui) {
          ui.button.removeClass("active");
          self.trigger("close");
          self.isOpen = false;
        },
        change: function() {
          $el.trigger("change", $el.val());
          self.trigger("change", $el.val());
        },
        create: function() {
          var classes = "ui-selectmenu-theme-" + options.theme;

          if(options.insideStickyHeader) {
            classes += " is-inside-sticky";
          }
          if(options.insideOverlay) {
            classes += " is-inside-overlay";
          }
          if(options.insidePreferences) {
            classes += " is-inside-preferences";
          }

          $(this)
            .selectmenu("menuWidget")
            .parent()
            .addClass(classes);
          $(this)
            .selectmenu("widget")
            .addClass(classes);
        }
      });
    },

    _bindEvents: function() {
      // position menu on resize/scroll
      $(window).on("resize scroll", _.bind(function() {
        if(this.isOpen) {
          this.$el.selectmenu("positionMenu");
        }
      }, this));
    },

    getValue: function() {
      return this.el.value;
    },

    close: function() {
      this.$el.selectmenu("close");
    },

    destroy: function() {
      this.$el.selectmenu("destroy");
      // TODO
      // $win.off("resize scroll", this.position);
    }
  });
  
});
