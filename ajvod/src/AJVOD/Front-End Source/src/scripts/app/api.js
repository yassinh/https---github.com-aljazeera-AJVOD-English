// ALJ player API
//
// The player will call these functions directly, when then broadcast an
// event out for some other part of the code to react to.
define(["app"], function(app) {
  "use strict";

  // If an API method needs to call a callback, this fn makes sure that the
  // request (trigger) and response (listener to the complete version of the
  // event) are synced. Otherwise the user could fire off multiple async API
  // requests in rapid fire, but they could come back in a different order, so
  // one request could respond with another's payload.
  var eventLoop = function(eventName, data, callbackName) {
    var loopId = new Date().getTime();
    app.once(eventName + ":complete", function(data, id) {
      if(id === loopId) {
        var player = document.querySelector("object");
        
        if(player && player[callbackName]) {
          player[callbackName](data);
        } else {
          console.error("Tried to fire callback but it doesn't exist on the player:", callbackName);
        }
      }
    });
    app.trigger(eventName, data, loopId);
  };

  // Expose all methods on the app.api object, which is exposed to the global
  // scope in teh app.js file.
  app.api = {};

  // Add a video to a playlist. This will trigger the playlist selection dialog
  // to open. It is assumed that the video ID exists as a video somewhere in the
  // page so that addl. metadata can be retrieved for the add to playlist
  // functionality to work properly.
  app.api.addToPlaylist = function(videoID) {
    app.trigger("api:addToPlaylist", videoID);
  };

  // Open the global search panel overlay
  app.api.openSearchPanel = function() {
    app.trigger("api:openSearchPanel");
  };

  // Save the current playhead position in Gigya
  app.api.saveVideoProgress = function(videoID, seekTime) {
    app.trigger("api:saveVideoProgress", videoID, seekTime);
  };

  // Find the current playhead position
  app.api.retrieveVideoProgress = function(videoID, callbackName) {
    eventLoop("api:retrieveVideoProgress", videoID, callbackName);
  };

  // Subscribe to a query
  app.api.subscribe = function(videoID, callbackName) {
    eventLoop("api:subscribe", videoID, callbackName);
  };

  // Unsubscribe from a query
  app.api.unSubscribe = function(videoID, callbackName) {
    eventLoop("api:unsubscribe", videoID, callbackName);
  };

  // Determine if a video is subscribed to a query
  app.api.isSubscribed = function(videoID, callbackName) {
    eventLoop("api:isSubscribed", videoID, callbackName);
  };
});
