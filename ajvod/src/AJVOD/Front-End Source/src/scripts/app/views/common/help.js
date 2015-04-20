define([
  "app",
  "views/common/dialog",
  "text!templates/help.html"
], function(app, DialogView, tmplHelp) {
  "use strict";

  return DialogView.extend({
    id: "dialog-help",

    template: _.template(tmplHelp),

    _bindEvents: function() {
      DialogView.prototype._bindEvents.call(this);

      this.listenTo(app, "menu:open", function() {
        this.close();
      });
      this.listenTo(app, "menu:close", function(menuID) {
        if(menuID === "preferences") {
          this.close();
        }
      });
    },

    render: function(context) {
      if(!("title" in context)) {
        throw new Error("Cannot render help without a title");
      }
      if(!("description" in context)) {
        throw new Error("Cannot render help without a description");
      }

      console.log('Rendering help dialog with context', context);
      this.$el.html(this.template(context));
    }
  });
});
