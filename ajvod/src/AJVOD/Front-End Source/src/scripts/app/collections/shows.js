define(["app"], function(app) {
  "use strict";

  return Backbone.Collection.extend({
    url: function() {
      return "http://psdev.brightcove.com:8080/aljazeera-vod/fake-ajax/searchresults-ajax";
      // return "/searchresults-ajax/";
    }
  });

});
