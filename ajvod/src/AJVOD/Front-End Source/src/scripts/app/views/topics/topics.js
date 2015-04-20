/*
 * Top-level topics page view
 */

define([
  "app",
  "views/common/videopage",
  "collections/topics"
], function(app, VideoPageView, TopicsCollection) {
  "use strict";

  return VideoPageView.extend({
    initialize: function() {
      VideoPageView.prototype.initialize.call(this);
      this.collection = new TopicsCollection();
    }
  });

});
