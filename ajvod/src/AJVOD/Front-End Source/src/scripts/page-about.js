define([
  "app",
  "views/about/about"
], function(app, AboutPageView) {
  "use strict";

  $(document).ready(function() {
    app.views.aboutPageView = new AboutPageView();
  });
});
