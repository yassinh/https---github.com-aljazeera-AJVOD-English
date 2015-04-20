define([
  "app",
  "views/allshows/allshows"
], function(app, AllShowsPageView) {
  "use strict";

  $(document).ready(function() {
      app.views.allShowsPageView = new AllShowsPageView();
  });
});
