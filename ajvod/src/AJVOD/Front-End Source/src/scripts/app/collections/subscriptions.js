// Subscriptions collection

define([
 "app",
 "views/common/gigya"
], function(app, GigyaView) {
  "use strict";

  return Backbone.Collection.extend({
    url: "/api/subscriptions",

    // Override sync to handle our get request. Only get runs through
    // sync; add/remove have their own methods. The main reason is because 
    // we need access to the jqxhr through collection.create() which 
    // backbone doesn't support.
    sync: function(method, model, options) {
      return $.ajax(this.url + "/get", {
        contentType: "application/json"
      });
    },

    addSubscription: function(params) {
      // Don't pass the prefix key in the ajax request
      var ajaxParams = _.extend({}, params);
      if(ajaxParams.prefix) {
        delete ajaxParams.prefix;
      }

      return $.ajax(this.url + "/add", {
        contentType: "application/json",
        type: "post",
        data: JSON.stringify(ajaxParams)
      }).always(function(resp, textStatus, jqxhr) {
        // Args shift depending if this xhr was successful or not
        if(resp.responseText) {
          jqxhr = resp;
        }

        $.extend(params, resp || {});

        app.trigger("subscriptions:add:complete", params);

        switch(resp.status) {
          case 201:
            app.trigger("subscriptions:add:success", params);
            break;
          case 200:
            app.trigger("subscriptions:add:exists", params);
            break;
          case 409:
            app.trigger("subscriptions:add:limitreached", params);
            break;
          case 401:
            app.trigger("subscriptions:add:unauthorized", params);
            break;
          default:
            app.trigger("subscriptions:add:error", params);
        }
      });
    },

    removeSubscription: function(id) {
      var params = { id: id };

      return $.ajax(this.url + "/remove", {
        type: "post",
        data: params
      }).pipe(function(resp) {
        params.removed = resp.removed;
        return resp.removed ? resp : $.Deferred().reject(resp);
      }).done(function() {
        app.trigger("subscriptions:remove:success", params);
      }).fail(function() {
        app.trigger("subscriptions:remove:error", params);
      }).always(function() {
        app.trigger("subscriptions:remove:complete", params);
      });
    },

    reorder: function(params) {
      return $.ajax(this.url + "/order", {
        type: "post",
        contentType: "application/json",
        data: JSON.stringify(params)
      }).done(function() {
        app.trigger("subscriptions:reorder:success", params);
      }).fail(function() {
        app.trigger("subscriptions:reorder:error", params);
      });
    },

    // Check for videos newer than the current stored timestamp.
    checkForNew: function() {
      var lastDate = localStorage.getItem(app.consts.LATEST_SUBSCRIPTION_VIDEO_DATE);
      var dfd = new $.Deferred();
      var reqs = [];
      var consts = app.consts;
      var gigSession;

      // if nothing is stored then bail
      if(!lastDate) {
        console.warn('Not checking for new subscriptions because there isn\'t a stored timestamp');
        return dfd.reject();
      }

      // no need to keep this thing around any longer..
      // localStorage.removeItem(app.consts.LATEST_SUBSCRIPTION_VIDEO_DATE);
      
      // parse to number for comparison
      lastDate = new Date(parseInt(lastDate, 10));
      lastDate.setHours(0, 0, 0, 0);

      // Only do this check in between sessions. the user has to logout first.
      if(GigyaView.getSession() == null) {
        console.warn('Not checking for new subscriptions because the user is in the same session or is logged out');
        dfd.reject();
        return dfd.promise();
      }

      // Clear the user session in localStorage since we're about to make a request.
      // This way we won't do another one until a new session is created.
      GigyaView.clearSession();

      // fetch subscriptions from ALJ API
      this.fetch().done(function(resp) {
        // Function to fetch each subscription from the media API using the stored query
        var mediaFetch = (function() {
          // Was a newer video found? If so we stop the API requests
          var found = false;

          // Function to compare timestamps and handle a newer video
          var compareTimestamp = function(video) {
            video.lastModifiedDate = new Date(parseInt(video.lastModifiedDate, 10));
            video.lastModifiedDate.setHours(0, 0, 0, 0);

            console.log('Comparing date:', video.lastModifiedDate, lastDate);
            if(video.lastModifiedDate.getTime() > lastDate.getTime()) {
              // Set this date as the latest date
              localStorage.setItem(app.consts.LATEST_SUBSCRIPTION_VIDEO_DATE, video.lastModifiedDate);
              console.log('New video found!', video, lastDate);
              dfd.resolve();
              found = true;
            }
          };

          // Ajax complete handler
          // Keep cycling through the subscriptions and performing media
          // API requests.
          var onAjaxComplete = function() {
            if(resp.subscriptions.length && !found) {
              mediaFetch();
            }
          };

          // Ajax success handler
          // Compare timestamps on all the videos
          var onAjaxSuccess = function(mediaResp) {
            if(!mediaResp.videos || !mediaResp.videos.length) return;
            _.each(mediaResp.videos, compareTimestamp);
          };

          return function() {
            var subscription = resp.subscriptions.shift();

            if(!subscription) {
              return;
            }

            return $.ajax("/api/mediaquery/", {
              data: {
                query: subscription.query,
                encrypted: false
              }
            }).done(onAjaxSuccess).always(onAjaxComplete);
          };
        })();

        // kick off fetch
        mediaFetch();
      });

      // Return a promise that will only resolve if new subscriptions are found
      return dfd.promise();
    }

  });
});
