define([
  "app",
  "views/channel/channel"
], function(app, ChannelPageView) {
  "use strict";

  $(document).ready(function() {
    app.views.channelPageView = new ChannelPageView();
  });
});
