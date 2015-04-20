define([
  "app",
  "views/error/error"
], function(app, ErrorPageView) {
  "use strict";

  $(document).ready(function() {
    app.views.errorPageView = new ErrorPageView();
  });
});
