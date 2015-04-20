define([
  "app",
  "text!templates/notifications.html",
  "jquerynotify"
], function(app, tmplNotifications) {
  "use strict";

  var BaseView = app.views.BaseView;

  return BaseView.extend({
    initialize: function() {
      BaseView.prototype.initialize.apply(this, arguments);
      this.listenTo(app, "notify", this.createNotification);
    },

    createNotification: function(opts) {
      // lazy instantiate plugin
      if(!$.contains(document.body, this.el)) {
        this.$el.html(tmplNotifications);
        this.$el.appendTo(document.body);
        this.$el.notify();
      }

      this.$el.notify("create", opts, {
        beforeopen: function() {
          if(opts.style) {
            $(this).addClass("ui-notify-" + opts.style);
          }

          console.log('Notification: open', opts, this);
        },
        click: function(event, instance) {
          instance.close();
        }
      });
    }
  });

});
