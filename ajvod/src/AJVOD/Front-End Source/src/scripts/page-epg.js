define([
  "app",
  "views/epg/epg"
], function(app, EPGPageView) {
  "use strict";

  $(document).ready(function() {
    app.views.epgPageView = new EPGPageView();
  });
});
