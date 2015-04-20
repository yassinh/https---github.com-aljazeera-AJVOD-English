/*
 * Top-level events page view
 */

define([
  "app",
  "views/topic/topic",
  "views/common/timemachinedialog"
], function(app, TopicPageView, TimeMachineDialogView) {
  "use strict";

  return TopicPageView.extend({
    events: _.extend({}, TopicPageView.prototype.events, {
      "click .item-event": "_navigateTimeMachine"
    }),

    _navigateTimeMachine: function(event) {
      if(event.target.className.indexOf("do-playlist") > -1) {
        return;
      }

      event.preventDefault();
      var $target = $(event.currentTarget);
      var id = $target.data("video-id") || $target.closest(".item").data("video-id");

      if(!id) {
        console.error('Tried to enter time machine without an id');
        this.router.navigate("", true);
        return;
      }

      this.router.navigate("time-machine/" + id, {
        trigger: true
      });
    },

    // Repond to the router moving to time machine
    _initTimeMachine: function(videoID, index) {
      if(this.views.timemachine) {
        console.warn('Tried to open time machine but it\'s already open');
        return;
      }

      var timemachine = this.views.timemachine = new TimeMachineDialogView({
        videoID: videoID,
        currentIndex: index
      });

      this.listenToOnce(timemachine, "error", function() {
        this.router.navigate("index", true);
        delete this.views.timemachine;
      });
      this.listenToOnce(timemachine, "close", function() {
        delete this.views.timemachine;
      });
    },

    _initSelectMenu: $.noop
  });

});
