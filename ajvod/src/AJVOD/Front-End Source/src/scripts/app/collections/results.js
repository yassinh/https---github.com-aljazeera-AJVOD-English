define(["app"], function(app) {
  "use strict";

  return Backbone.Collection.extend({
    sync: function(method, model, options) {
      return $.ajax(this.url, {
        data: options.data,
        dataType: "html"
      });
    }
  });

});
