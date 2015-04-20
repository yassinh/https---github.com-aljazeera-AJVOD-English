// Shows model

define(["app"], function(app) {
  "use strict";

  var cache = {};

  return Backbone.Model.extend({
    url: function() {
      return this.get("url");
    },

    sync: function(method, model) {
      var url = model.get("url");

      if(cache[url]) {
        console.log('Returning cached show', url);
        return cache[url];
      }

      // only cache it once it's completed successfully
      var dfd = Backbone.sync.apply(this, arguments).done(function() {
        cache[url] = dfd;
      });

      return dfd;
    }
  });
});
