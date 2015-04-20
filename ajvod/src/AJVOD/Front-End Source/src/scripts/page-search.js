define([
  "app",
  "views/search/search"
], function(app, SearchPageView) {
  "use strict";

  $(document).ready(function() {
    app.views.searchPageView = new SearchPageView();
  });
});
