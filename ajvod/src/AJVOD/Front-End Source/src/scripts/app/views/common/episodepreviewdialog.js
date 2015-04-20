define([
  "app",
  "views/common/dialog",
  "text!templates/episode-preview.html"
], function(app, DialogView, tmplPreview) {
  "use strict";

  return DialogView.extend({
    id: "episode-preview-dialog",

    template: _.template(tmplPreview),

    defaults: _.extend({}, DialogView.prototype.defaults, {
      animatePositioning: true
    }),

    render: function() {
      var context = this.options.context || {};
      console.log('Dialog: rendering context', context);
      this.$el.html(this.template(context));

      this.$el.find("img").attr("src", function() {
        this.src = this.getAttribute("data-src");
      });
    },

    open: function() {
      DialogView.prototype.open.call(this);
      this.position();
    }
  });
});
