/**
 * Menu overlay singleton
 */
define(["app"], function(app) {
  "use strict";

  var FADE_SPEED = app.consts.OVERLAY_FADE_SPEED;
  var instance;

  var OverlayView = app.views.BaseView.extend({
    isOpen: false,

    id: "overlay",

    events: {
      "click": "_onClick"
    },

    initialize: function() {
      this.$el.appendTo(document.body);
    },

    open: function(callback) {
      if(this.isOpen) {
        return;
      }

      this.isOpen = true;
      this.$el.fadeIn(FADE_SPEED);
    },

    close: function() {
      if(!this.isOpen) {
        return;
      }

      this.isOpen = false;
      this.$el.fadeOut(FADE_SPEED);
    },

    // Notify other parts of the code that the overlay
    // was clicked on
    _onClick: function() {
      this.trigger("click");
    }
  });

  // Expose this view as a singleton
  return function() {
    if(!instance) {
      instance = new OverlayView();
    }

    return instance;
  };
});
