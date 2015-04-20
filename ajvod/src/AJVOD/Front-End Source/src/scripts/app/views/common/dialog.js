/*
 * Base dialog view from which all others extend.
 */

define([
  "app"
], function(app) {
  "use strict";

  var BaseView = app.views.BaseView;
  var instances = [];

  // Close all instances on esc. Not using native jquery ui dialog
  // functionality here because it only works if the dialog has
  // focus.
  $(document).on("keyup.dialog", function(event) {
    if(event.which === 27) {
      _.each(instances, function(obj) {
        if(obj.options.closeOnEscPress) {
          obj.close();
        }
      });
    }
  });

  // if there's an instance open reposition it on window scroll
  var $win = $(window).on("scroll.dialog resize.dialog", _.throttle(function() {
    _.each(instances, function(obj) {
      if(obj.options.fixed && obj.isOpen()) {
        obj.position();
      }
    });
  }, 25));

  return BaseView.extend({
    defaults: {
      // jQuery UI options
      autoOpen: false,
      minHeight: 1,
      minWidth: "auto",
      resizeable: false,
      closeOnEscape: false, // we implement this ourselves below
      animatePositioning: false, // CSS3 animate properties
      position: {
        using: function(pos, ui) {
          if(!ui) return;

          var width = ui.element.element.width();
          var height = ui.element.element.height();

          ui.element.element.css({
            position: "fixed",
            top: "50%",
            left: "50%",
            marginLeft: ((width / 2) * -1) + "px",
            marginTop: ((height / 2) * -1) + "px",
            zIndex: 9999 // TODO: move to css
          });
        }
      },

      // Custom options implemented by us
      closeOnEscPress: true,
      closeWhenOthersOpen: true,
      destroyOnClose: true
    },

    events: {
      "click .h-icon-close": "close"
    },

    _isOpen: false,

    initialize: function() {
      BaseView.prototype.initialize.call(this);

      // merge options
      this.options = $.extend({},
        this.defaults,
        this.options
      );

      // Open flag
      this.listenTo(this, "open", function() {
        this._isOpen = true;
      });
      this.listenTo(this, "close", function() {
        this._isOpen = false;
      });

      // Destroy this thing when it closes?
      if(this.options.destroyOnClose) {
        this.listenToOnce(this, "close", this.destroy);
      }

      // Close this dialog when other ones open?
      if(this.options.closeWhenOthersOpen) {
        this.listenTo(app, "dialog:open", function(obj) {
          if(obj.el !== this.el) {
            this.close();
          }
        });
      }

      // Add CSS transitions class?
      if(this.options.animatePositioning) {
        this.options.dialogClass += " ui-dialog-animate";
      }

      // proxy dialog events onto this object
      this._proxyEvents();

      // Add to the DOM if it's not already in there.
      if(!this.el.parentNode) {
        this.$el.appendTo(document.body);
      }

      // create the dialog
      this.$el.dialog(this.options);

      // remember this instance exists
      instances.push(this);
    },

    destroy: function() {
      console.log('Dialog: destroy');
      this.trigger("destroy");
      this.$el.dialog("destroy");
      this.remove();

      instances = _.reject(instances, _.bind(function(obj) {
        return obj.el === this.el;
      }, this));
    },

    position: function() {
      this.$el.data("uiDialog")._position();
      // if(!this.options.fixed) {
        // this._setFixedPosition();
        // this.$el.data("uiDialog")._position();
      // } else {
      // }
    },

    open: function() {
      this.$el.dialog("open");
    },

    close: function(event) {
      if(event) event.preventDefault();
      this.$el.dialog("close");
    },

    isOpen: function() {
      return this._isOpen;
    },

    // utils

    _proxyEvents: function() {
      var self = this;

      _.each(["create", "open", "beforeClose", "close", "destroy"], function(eventName) {
        self.$el.on("dialog" + eventName.toLowerCase(), function(event, ui) {
          self.trigger(eventName, [event, ui]);
          app.trigger("dialog:" + eventName, self);
        });
      });
    }
  });

});
