// Search overlay collection

define(["app"], function(app) {
  "use strict";
  var cache = {};

  return Backbone.Collection.extend({
    clearCache: function() {
      cache = null;
      cache = {};
    },
    sync: function(method, model, options) {
      var term = options.data.match(/keyword=(.*?)(?:$|\&)/);
      term = term && term.length >= 1 ? term[1] : null;
      
      if(term && cache[term]) {
        console.log('Returning cached xhr for term', term);
        return cache[term];
      }

      return (cache[term] = Backbone.sync.apply(this, arguments));
    },
    parse: function(results) {
        results.videos = _.map(results.videos || [], function (video) {
        video.date = app.utils.formatDate(video.date);
        video.duration = app.utils.formatDuration(video.duration);
        return video;
      });

      return results;
    }
  });
});
