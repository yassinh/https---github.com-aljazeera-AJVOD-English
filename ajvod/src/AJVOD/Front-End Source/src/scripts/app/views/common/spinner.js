define(["app"], function(app) {
  "use strict";

  /**
   * @constructor Spinner
   * Create a new spinner
   */
  return app.views.BaseView.extend({
    className: "spinner h-hidden",

    /**
     * Spinner options
     */
    defaults: {
      /**
       * minimum amount of time to display for.
       * TODO: detect how long
       */
      minDisplayFor: 0,

      /**
       * Spinner style. Valid values are:
       *   null     = default style, suitable for light backgrounds
       *   dark     = spinner suitable for dark backgrounds
       *   overlay  = center on the screen
       */
      style: null,

      /**
       * Automatically append this view to the DOM?
       */
      append: false
    },

    initialize: function() {
      this.options = _.extend({},
        this.defaults,
        this.options
      );

      // Apply custom style?
      if(this.options.style) {
        this.el.className += " style-" + this.options.style;
      }
      // Append to DOM?
      if(this.options.append) {
        document.body.appendChild(this.el);
      }
    },

    /**
     * Show the spinner
     * @returns this
     */
    show: function() {
      this.$el.removeClass("h-hidden");
      return this;
    },

    /**
     * Hide the spinner, optionally with a timeout that
     * was specified during initialization.
     * @returns this
     */
    hide: function() {
      var $el = this.$el;
      var hide = function() { $el.addClass("h-hidden"); };

      if(this.options.minDisplayFor === 0) {
        hide();
      } else {
        setTimeout(hide, this.options.minDisplayFor);
      }

      return this;
    }
  });

});
