define([
  "app",
  "views/events/events"
], function(app, EventsPageView) {
  "use strict";

  $(document).ready(function() {
    app.views.eventsPageView = new EventsPageView();
  });
});
