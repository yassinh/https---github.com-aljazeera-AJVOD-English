/*
 * Top-level search page view
 */

define([
  "app",
  "views/common/videopage",
  "views/common/selectmenu",
  "collections/results",
  "jqueryui",
  "jqueryuiselect"
], function(app, VideoPageView, SelectMenuView, ResultsCollection) {
  "use strict";

  var DISABLED_CLASS = app.consts.DISABLED_CLASS;
  var ACTIVE_CLASS = app.consts.ACTIVE_CLASS;

  /**
   * el: #main
   */
  return VideoPageView.extend({
    events: _.extend({}, VideoPageView.prototype.events, {
      "click .pages a": "changePage",
      "click #filters a": "onFilterChange"
    }),

    // If this is true we have to drop page num from the ajax request
    isDirty: false,

    initialize: function() {
      this._cacheElems();

      VideoPageView.prototype.initialize.call(this);

      // Sticky pagination
      this.$(".pagination").sticky().on("stuck unstuck", _.bind(function() {
        _.each(this.views.selectmenus, function(view) {
          view.close();
        });
      }, this));

      this.collection = new ResultsCollection([], {
        url: this.$form.attr("action")
      });

      // default sorting/filter
      this.currentSort = this.$sort.val();
      this.currentFilter = this.getCurrentFilter();
    },

    _cacheElems: function() {
      this.$form = this.$("form.sort");
      this.$sort = this.$form.find("select");
      this.$pagination = this.$(".pages");
      this.$results = this.$("#results");
      this.$term = this.$("#term");
      this.$filter = this.$("#filters");
      this.$bigscreen = this.$(".do-big-screen");
    },

    _initSubviews: function() {
      VideoPageView.prototype._initSubviews.call(this);

      this.views.selectmenu = new SelectMenuView({
        el: this.$sort[0],
        insideStickyHeader: true
      });
    },

    _bindEvents: function() {
      VideoPageView.prototype._bindEvents.call(this);

      // Re-sort when this select changes
      this.listenTo(this.views.selectmenu, "change", this.onSortChange);

      // TODO: move to CSS
      this.listenTo(app, "menu:open", function() {
        this.$(".pagination").data("sticky")._adjustZindex(200);
      });
      this.listenTo(app, "menu:close", function() {
        this.$(".pagination").data("sticky")._adjustZindex(220);
      });
    },

    onFilterChange: function(event) {
      event.preventDefault();

      $(event.currentTarget)
        .closest("li")
        .addClass(ACTIVE_CLASS)
        .siblings()
        .removeClass(ACTIVE_CLASS);

      var filter = this.getCurrentFilter();

      if(this.currentFilter !== filter) {
        this.changePage(0);
        this.currentFilter = filter;
        this.hasBuiltPages = false;
        this.isDirty = true;
      }

      this.fetch();
    },

    onSortChange: function() {
      var sort = this.$sort.val();

      if(this.currentSort !== sort) {
        this.changePage(0);
        this.currentSort = sort;
        this.isDirty = true;
      }

      this.fetch();
    },

    getCurrentFilter: function() {
      return this.$filter.find("." + ACTIVE_CLASS).data("filter") || "";
    },

    changePage: function(eventOrPageNum) {
      var indexSpecified = typeof eventOrPageNum === "number";
      var $target;

      if(!indexSpecified) {
        eventOrPageNum.preventDefault();
      }
      if(this.dfd && this.dfd.state() === "pending") {
        return;
      }

      // If a page number was passed in find the associated anchor
      if(indexSpecified) {
        $target = this.$pagination
          .find("li")
          .not(".next, .prev")
          .eq(eventOrPageNum)
          .find("a");

      // Otherwise an event object is available
      } else {
        $target = $(eventOrPageNum.currentTarget);
      }

      var $li = $target.closest("li");
      var $lis = $li.closest("ul").children();
      var $numbers = $lis.not(".next, .prev");

      // stop here if the next/prev arrow is disabled
      if($li.hasClass("is-disabled")) {
        return;
      }

      // reassign the li to the appropriate number if this
      // is next or prev
      if($li.hasClass("next")) {
        $li = $numbers.filter("." + ACTIVE_CLASS).next();
      }
      if($li.hasClass("prev")) {
        $li = $numbers.filter("." + ACTIVE_CLASS).prev();
      }

      // stop here if this item is hidden
      if($li.hasClass("h-hidden") && !indexSpecified) {
        return;
      }

      // make this li active
      $li
        .addClass(ACTIVE_CLASS)
        .siblings()
        .removeClass(ACTIVE_CLASS);

      if(typeof eventOrPageNum === "object") {
        this.fetch();
      }
    },

    fetch: function() {
      if(this.dfd && this.dfd.state() === "pending") {
        return;
      }

      var $results = this.$results;
      var $resultsLength = this.$(".results-length");
      var $startIndex = this.$(".results-start-index");
      var $endIndex = this.$(".results-end-index");
      var term = this.$term.text();
      var sort = this.$sort.val();
      var filter = this.currentFilter;
      var page;

      var params = {
        st: term,
        sort: sort,
        cid: this.$('input[name="cid"]').val()
      };

      // only include page param if the sort/filter wasn't changed
      // and if there's actually a page to pass through.
      if(!this.isDirty) {
        page = this.$pagination.find("." + ACTIVE_CLASS).text();

        if(page) {
          params.page = page;
        }
      }
      if(filter.length) {
        params.vidtype = filter;
      }

      app.trigger("spinner:show");

      this.dfd = this.collection.fetch({ data: params }).done(_.bind(function(resp) {
        var $html = $('<div>' + resp + '</div>');
        var numThisPage = $html.find("li").length;
        console.log('Number of results this page:', numThisPage);

        if(numThisPage === 0) {
          resp = app.localization.noSearchResults || "";
       }

        $resultsLength.text($html.find("#totalVideoCount").text());
        $startIndex.text($html.find("#videoStartIndex").text());
        $endIndex.text($html.find("#videoEndIndex").text());
        $results.html(resp);

        var totalPages = parseInt($html.find("#totalPages").text(), 10);
        if(!this.hasBuiltPages && totalPages) {
          this._buildPages(totalPages);
          this.hasBuiltPages = true;
        }

        this._centerPagination();
        this._toggleArrowsDisabled();
        this._toggleElements(numThisPage > 0);
      }, this)).fail(function(xhr, textStatus) {
        if(textStatus !== "abort") {
          $results.html("<p>" + app.localization.generic_error_text + "</p>");
        }
      }).always(function() {
        app.trigger("spinner:hide");
      });

      this.isDirty = false;
    },

    _buildPages: function(numPages) {
      var $lis = this.$pagination.find("li");
      var $numbers = $lis.not(".prev, .next");
      var activeIndex = $lis.filter(".is-active").index() - 1;
      var $template = $numbers.first().clone();
      var newLis = [];
      $numbers.remove();

      _.times(numPages, function(i) {
        var clone = $template.clone();
        clone.find("a").text(i + 1);
        clone.addClass("h-hidden");
        clone.data("dynamically-generated", true);
        clone.toggleClass("is-active", i === activeIndex);
        newLis[newLis.length] = clone[0];
      });

      $(newLis).insertAfter($lis.first());
    },

    _centerPagination: function() {
      var $lis = this.$pagination.find("li").not(".prev, .next");
      var numPages = $lis.length;
      var activeIndex = $lis.filter(".is-active").index() - 1;

      // Show 5 pages by default, the active page centered
      var visibleIndexes = [
        activeIndex - 2,
        activeIndex - 1,
        activeIndex,
        activeIndex + 1,
        activeIndex + 2
      ];

      // If on page 1 or 2, show 5 pages without the active page centered
      if(activeIndex <= 2) {
        visibleIndexes = [0, 1, 2, 3, 4];
      }

      // If on the last/second to last page, show 5 pages w/o the active
      // page centered.
      if(activeIndex === numPages - 2 || activeIndex === numPages - 1) {
        visibleIndexes = [
          numPages - 5,
          numPages - 4,
          numPages - 3,
          numPages - 2,
          numPages - 1,
          numPages
        ];
      }

      // Toggle visiblity of the pages
      $lis.each(function(i, elem) {
        var flag = _.indexOf(visibleIndexes, i) > -1;
        $(elem).toggleClass("h-hidden", !flag);
      });
    },

    _toggleArrowsDisabled: function() {
      var $lis = this.$pagination.find("li");
      var $numbers = $lis.not(".next, .prev");
      var $prev = $lis.first();
      var $next = $lis.last();
      var index = $numbers.filter("." + ACTIVE_CLASS).index();

      if($numbers.length === 1) {
        $prev.add($next).addClass(DISABLED_CLASS);
      // toggle prev/next disabled
      } else if(index === $numbers.length) {
        $next.addClass(DISABLED_CLASS);
        $prev.removeClass(DISABLED_CLASS);
      } else if(index <= 1) {
        $prev.addClass(DISABLED_CLASS);
        $next.removeClass(DISABLED_CLASS);
      } else {
        $prev.add($next).removeClass(DISABLED_CLASS);
      }
    },

    /**
     * Toggle element visiblity based on whether or not there's search
     * results
     */
    _toggleElements: function(flag) {
      this.$bigscreen.toggleClass("h-hidden", !flag);
      this.$(".pagination").toggleClass("no-pages", !flag);
    }

  });

});
