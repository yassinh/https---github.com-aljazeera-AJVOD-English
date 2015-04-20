define([
  "app",
  "views/home/home"
], function(app, HomePageView) {
  "use strict";

  $(document).ready(function() {
    app.views.homePageView = new HomePageView({
      el: document.body
    });
  });
});
