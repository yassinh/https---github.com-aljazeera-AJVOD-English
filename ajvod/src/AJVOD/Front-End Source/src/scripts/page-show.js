define([
  "app",
  "views/show/show"
], function(app, ShowPageView) {
  "use strict";

  $(document).ready(function() {
    app.views.showPageView = new ShowPageView();
  });
});
