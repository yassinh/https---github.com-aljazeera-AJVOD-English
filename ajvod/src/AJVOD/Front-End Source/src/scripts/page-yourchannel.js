define([
  "app",
  "views/yourchannel/yourchannel"
], function(app, YourChannelPageView) {
  "use strict";

  $(document).ready(function() {
    app.views.yourChannelPageView = new YourChannelPageView();
  });
});
