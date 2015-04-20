define([
  "app",
  "views/detail/detail"
], function(app, DetailPageView) {
  "use strict";

  $(document).ready(function() {
    app.views.detailPageView = new DetailPageView();
  });
});
