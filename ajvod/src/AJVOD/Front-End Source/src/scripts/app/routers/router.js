// Page router
//
// Currently only drives big screen functionality, but this
// router will be used for any page-level hash change/whatever
// functionality.

define([ "app" ], function(app) {
  "use strict";

  return Backbone.Router.extend({
    routes: {
      "": "index",
      "index": "index",
      "big-screen/(:id)(/:index)": "bigscreen",
      "time-machine/:id(/:index)": "timemachine"
    },

    index: function() {
      console.log('Navigated home');
    },

    // Tell whoever wants to respond to this that we routed.
    // Firing this on app rather than relying on
    // router events, because the event could have fired on the router
    // before we have a chance to bind to it.
    //
    // I'm doing it this way to avoid having to include time machine
    // on all pages when only 2 or 3 actually use it.

    bigscreen: function(id, index) {
      app.trigger("route:bigscreen", id, index || 0);
    },

    timemachine: function(id, index) {
      app.trigger("route:timemachine", id, index || 0);
    }
  });

});
