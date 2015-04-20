define([
  "app",
  "views/topics/topics"
], function(app, TopicsPageView) {
  "use strict";

  $(document).ready(function() {
    app.views.topicsPageView = new TopicsPageView();
  });
});
