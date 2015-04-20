define([ "app" ], function(app) {
  "use strict";

  return Backbone.Collection.extend({
    url: "/api/preferences",

    sync: function() {
      return $.get(this.url + "/get").pipe(function(resp) {
        return resp;
      });
    },

    save: function(data) {
      return $.ajax(this.url + "/set", {
        type: "post",
        data: data
      }).pipe(function(resp) {
        if(resp.updated === false || resp.status !== 200) {
          return $.Deferred().reject();
        }
      });
    }
  });
});
