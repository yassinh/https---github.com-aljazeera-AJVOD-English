/*
 * Top-level topic page view
 * 
 * The events page extends this one.
 */

define([
  "app",
  "views/common/videopage",
  "views/common/selectmenu"
], function(app, VideoPageView, SelectMenuView) {
  "use strict";

  return VideoPageView.extend({
    initialize: function() {
      VideoPageView.prototype.initialize.call(this);

      this._cacheElems();
      this._initItemHeight();
      this._initPagination();
      this._initSelectMenu();

      // Submit the form when the select element changes
      var $form = this.$form.on("change", "select", function() {
        $form.submit();
      });
    },

    _bindEvents: function() {
      VideoPageView.prototype._bindEvents.call(this);

      // TODO: move to CSS
      this.listenTo(app, "menu:open", function() {
        this.$pagination.data("sticky")._adjustZindex(200);
      });
      this.listenTo(app, "menu:close", function() {
        this.$pagination.data("sticky")._adjustZindex(220);
      });
    },

    _cacheElems: function() {
      var $main = $("#main");
      this.$items = $main.find(".grid li");
      this.$pagination = $main.find(".pagination");
      this.$form = $main.find("form");
    },

    _initPagination: function() {
      this.$pagination.sticky();
      this.$pagination.on("click", ".pages a", _.bind(this._changePage, this));
    },

    _initSelectMenu: function() {
      this.views.selectmenu = new SelectMenuView({
        el: this.$("select"),
        insideStickyHeader: true
      });
    },

    _initItemHeight: function() {
      var handler = _.bind(this._setItemHeight, this);
      handler();
      $(window).on("resize", _.throttle(handler, 100));
      this.$items.find("img").on("load", handler);
      this.$items.filter(":nth-child(4)").addClass("nth-child-4"); // FIXME
    },

    // Sets the height of the large item properly
    _setItemHeight: function() {
      this.$items.filter(".first-child").height(function() {
        var $smallLi = $(this).siblings().eq(0);
        return ($smallLi.height() * 2) + (Math.floor(parseFloat($smallLi.css("margin-top"))) * 2);
      });
    },

    _changePage: function(event) {
      event.preventDefault();

      var $li = $(event.currentTarget).closest("li");

      if($li.hasClass("is-disabled")) {
        return;
      }

      var $lis = $li.closest("ul").children();
      var $active = $lis.filter("." + app.consts.ACTIVE_CLASS);
      var numPages = this.$pagination.data("num-pages");
      var currPage = parseInt($active.text(), 10);

      if($li.hasClass("next")) {
        currPage++;
      } else if($li.hasClass("prev")) {
        currPage--;
      } else {
        currPage = parseInt($li.text(), 10);
      }

      // keep in bounds
      if(currPage <= 0) {
        currPage = 1;
      } else if(currPage > numPages) {
        currPage = $lis.length - 2;
      }

      this.$form.find('input[name="page"]').val(currPage);
      this.$form.submit();
    }
  });

});
