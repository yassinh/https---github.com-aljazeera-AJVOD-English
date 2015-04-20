/*
 * Top-level epg page view
 */

define([
  "app",
  "views/common/page",
  "views/common/episodepreviewdialog",
  "views/common/selectmenu",
  "text!templates/epg/epg.html"
], function(app, PageView, EpisodePreviewDialog, SelectMenuView, tmplEpg) {
  "use strict";

  var $win = $(window);
  var CELL_HEIGHT = 75;
  var CELL_SPACING = 8;

  return PageView.extend({
    events: _.extend({}, PageView.prototype.events, {
      "click .body": "_openPreviewDialog"
    }),

    template: _.template(tmplEpg),

    initialize: function() {
      _.bindAll(this, "_render");
      PageView.prototype.initialize.apply(this, arguments);
      this.$target = $("#target");
      this._fetch().done(this._render);
    },

    _initSubviews: function() {
      PageView.prototype._initSubviews.call(this);

      this.views.selectmenu = new SelectMenuView({
        el: this.$(".top select"),
        theme: "schedule",
        buttonIcon: "h-icon-schedule"
      });
    },

    _bindEvents: function() {
      PageView.prototype._bindEvents.call(this);

      this.listenTo(app, "menu:open", function() {
        this._closeDialog();
      });

      this.listenTo(this.views.selectmenu, "change", function(value) {
        this._closeDialog();
        app.trigger("spinner:show");
        var parts = window.location.pathname.split("/");
        parts.splice(-2, 1, value);
        window.location = parts.join("/");
      });
    },

    _fetch: function(params) {
      params = _.extend({
        cid: location.pathname.split("/").slice(-2, -1)[0],
        offset: (new Date()).getTimezoneOffset()
      }, params || {});

      app.trigger("spinner:show");

      return $.Deferred(_.bind(function(dfd) {
        $.getJSON("/api/epg", params).done(_.bind(function(resp) {
          var sticky = this.$target.find(".headers").data("sticky");
          if(sticky) sticky.destroy();

          // Find a thumbnail in the feed
          resp.days = _.map(resp.days, function(day) {
            day.shows = _.map(day.shows, function(show) {
              var keys = _.keys(show.thumbnails);
              show.thumbnail = keys.length ? show.thumbnails[keys[0]] : null;
              return show;
            });

            return day;
          });

          resp.dir = params.cid === "eng" ? "ltr" : "rtl";
          resp.error = !resp.days.length || !resp.times.length || resp.status !== 200;
          dfd.resolve(resp);
        }, this))
        .always(function() {
          app.trigger("spinner:hide");
        })
        .fail(_.bind(function() {
          dfd.resolve({ error: true });
        }, this));
      }, this)).promise();
    },

    _render: function(context) {
      context.localization = app.localization;
      this.$target.html(this.template(context));
      console.log('Rendering EPG context', context);

      var base = 30;
      var subtract_offset = 0;

      if(!context.error) {
        this.$target.find(".cell").each(function() {
          var mins = parseInt(this.getAttribute("data-mins"), 10);
          var multiplier = mins / base;
          var height = CELL_HEIGHT * multiplier;

          // If a show is longer than the base number of minutes, we have to
          // add the width of the gap to the height
          if(multiplier > 1) {
            height += CELL_SPACING * (multiplier - 1);
          }

          height = Math.floor(height);
          this.style.height = height + "px";

          // Shows whose height is less than the natural height of the cell
          // with border, padding, etc.
          if(height < (CELL_SPACING * 2)) {
            this.className += " tiny item";

            var $cell = $(this);
            var $tooltip = $('<div class="tooltip"></div>').html(function() {
              var html = '<div class="details">';
              html += '<p class="title">' + $cell.find('.title').text() + '</p>';
              html += '<p class="desc">' + $cell.data("description") + '</p>';
              html += '</div>';
              return html;
            });

            $cell.append($tooltip);
          }

          // console.group('cell');
          // console.log('Mins', mins);
          // console.log('multiplier', multiplier);
          // console.log('Height', height);
          // console.log('Cell spacing adjustment', (CELL_SPACING * (multiplier - 1)));
          // console.log('Subtract offset', subtract_offset);
          // console.groupEnd();
        });

        this._initItemTooltips(null, {
          position: {
            my: "center-66 bottom-25",
            at: "center top",
            collision: "fit",
            using: function(position) {
              position.width = 300;
              $(this).css(position);
              $("<div />").addClass("arrow h-icon h-icon-tooltip-arrow").appendTo(this);
            }
          }
        }, ".cell");
        this._initStickyHeader();
        this._initFluidTable();
      }
    },

    _openPreviewDialog: function(event) {
      // Allow links to pass
      if(event.target.tagName === "A") {
        return;
      }

      var dialog = this.views.dialog;

      // Lazy load the creation of this thing
      if(!dialog) {
        dialog = this.views.dialog = new EpisodePreviewDialog({
          width: 600,
          destroyOnClose: false,
          show: 150,
          hide: 150
        });
      }

      var $target = $(event.currentTarget.parentNode);
      var context = $target.data();

      if(dialog.isOpen() && dialog.options.context.videoId === context.videoId) {
        console.warn('A dialog is already open for this video');
        return;
      }

      context.title = $target.find(".title").text();
      context.show = $target.find(".show").text();
      context.showLink = $target.find(".show a").attr("href");

      dialog.options.context = context;
      dialog.render();
      dialog.open();
    },

    _initStickyHeader: function() {
      var $table = $("table");
      var $nav = $("#header .h-sticky");
      var $header = $(".bottom header");
      var $main = $("#main");

      var setCSS = function($container) {
        var container = $container[0];
        // container.style.position = "absolute";
        // container.style.width = $main.outerWidth(true) + "px";
        // container.style.left = "auto";
        // container.style.top = $nav.offset().top + $nav.height() + "px";
        container.style.zIndex = 2;
      };

      $header.sticky({
        onStick: function($container) {
          console.log('container', $container);
          setCSS($container);
        },
        onUnStick: function($container) {
          $container.removeAttr("style");
        }
      });

      $win.off(".epgheader").on("scroll.epgheader resize.epgheader", function() {
        var sticky = $header.data("sticky");

        if(sticky && sticky.stuck) {
          setCSS(sticky.$container);
        }
      });
    },

    _initFluidTable: function() {
      var $table = this.$("table");
      var $headers = this.$(".headers section");
      var $wrapper = $table.closest(".h-content");
      var offset = $table.find("td").eq(0).width();

      $win.off(".epgwidth").on("resize.epgwidth", function() {
        $table[0].style.width = $headers[0].style.width = "auto";
        $table[0].style.width = $headers[0].style.width = $wrapper.width() + offset + "px";
      }).trigger("resize.epgwidth");

      $table.add($headers).addClass("is-loaded");
    },

    _closeDialog: function() {
      if(this.views.dialog) {
        this.views.dialog.close();
      }
    }
  });
});
