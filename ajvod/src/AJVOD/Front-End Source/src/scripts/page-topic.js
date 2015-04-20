define([
  "app",
  "views/topic/topic"
], function(app, TopicPageView) {
  "use strict";

  $(document).ready(function() {
    app.views.topicPageView = new TopicPageView();
  });
});
