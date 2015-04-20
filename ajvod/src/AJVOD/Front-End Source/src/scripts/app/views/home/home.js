/*
 * Top-level home page view
 */

define([
  "app",
  "views/common/page",
  "views/common/gigya",
  "views/common/backgroundPlayer"
], function(app, PageView, GigyaView, BackgroundPlayer) {
  "use strict";

  return PageView.extend({

    initialize: function() {
      PageView.prototype.initialize.call(this);

      var $gridItems = this.$(".grid li").not(".h-hidden").each(function(i, elem) {
        setTimeout(function() {
          elem.className += " h-fade-in-run";
        }, i * 100);
      });

      console.log('Fading in grid items ' + $gridItems.length);

      GigyaView.getUserInfo().done(_.bind(function(data) {
        if(data.user.isLoggedIn) {
          this._onGigyaLogin(data);
        }
      }, this));
    },

    _initSubviews: function() {
      PageView.prototype._initSubviews.call(this);

      this.player = new BackgroundPlayer(
        $(".bgplayer")[0]
      );
    },

    _onGigyaLogout: function() {
      GigyaView.renderHeader();
      app.trigger("spinner:hide");
      this.$(".your-channel").addClass("h-hidden");
    },

    _onGigyaLogin: function(data) {
      GigyaView.renderHeader();

      // Show the "Your Channel" menu item
      var $item = this.$(".your-channel").removeClass("h-hidden");
      this.$(".your-channel").removeClass("theme-orange");
      var scope = data.user || data.profile;

      // Add the user's thumbnail to the menu item
      var $thumb = $item.find("img");
      var src = scope.photoURL ?
        scope.photoURL :
        (window.app.webroot + "/images/avatar-lg.png");

      $thumb.attr("src", src);

      setTimeout(function() {
        $item.addClass("h-fade-in-run");
      }, 100);

      if(scope && scope.photoURL) {
        $item.find(".hover img").attr("src", scope.photoURL);
      }
    }
  });

});
