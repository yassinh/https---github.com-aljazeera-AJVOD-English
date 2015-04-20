define([
  "app",
  "views/live/live"
], function(app, LivePageView) {
  "use strict";

  $(document).ready(function() {
    app.views.livePageView = new LivePageView();
  });
});
