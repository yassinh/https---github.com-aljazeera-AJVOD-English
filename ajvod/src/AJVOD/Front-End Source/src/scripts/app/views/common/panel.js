define(["app"], function(app) {
  "use strict";

  var BaseView = app.views.BaseView;
  var ACTIVE_CLASS = app.consts.ACTIVE_CLASS;

  /*
   * Panel items
   * el: div#{panel-id}
   */
  var PanelItemView = BaseView.extend({
    initialize: function() {
      BaseView.prototype.initialize.apply(this, arguments);
      
      this.$navItem = this.options.$navItem.find("a");

      // Create tooltips on this nav item
      this.options.$navItem.tooltip({
        tooltipClass: "tooltip-panel",
        track: true,
        position: {
          my: "left+15 center+7"
        }
      });
    },

    show: function() {
      this._toggle(true);
      this.$navItem.addClass(ACTIVE_CLASS);

      app.trigger("panel:open", {
        panel: this
      });
    },

    hide: function() {
      this._toggle(false);
      this.$navItem.removeClass(ACTIVE_CLASS);

      app.trigger("panel:close", {
        panel: this
      });
    },

    setNavItemText: function(text) {
      this.options.$navItem.find("a").text(text);
    },
    
    _toggle: function(open) {
      if(this.options.slide) {
        this.$el.stop();
        this.$el[open ? "slideDown" : "slideUp"]("fast");
      } else if(this.options.fade) {
        this.$el.stop();
        this.$el[open ? "fadeIn" : "fadeOut"]("fast");
      } else {
        this.$el.toggleClass(ACTIVE_CLASS, open);
      }
    }
  });

  /*
   * Panel view
   * el: div#{panel-container-id}
   */
  var PanelView = BaseView.extend({
    events: _.extend({}, BaseView.prototype.events, {
      "click .panel-nav a": "_changePanel",
      "click #panel-sharing a": "openSharePopup"
    }),

    initialize: function() {
      BaseView.prototype.initialize.apply(this, arguments);

      var $navItems = this.$("nav li");

      // create a panel view for each item
      _.each(this.options.panels || [], function(panel) {
        var $navItem = $navItems.filter(function(i, elem) {
          return $(elem).data("for") === panel;
        });

        this.views[panel] = new PanelItemView({
          el: $("#panel-" + panel),
          $navItem: $navItem,
          slide: this.options.slide,
          fade: this.options.fade
        });
      }, this);

      // active is the main panel by default
      this.activePanel = this.views.main;
    },
    
    getOpenPanel: function() {
      return this.activePanel;
    },

    getPanel: function(panelName) {
      var panel;

      if(!(panel = this.views[panelName])) {
        throw new Error("Attempted to retrieve panel " + panelName + "but it doesn't exist");
      }

      return panel;
    },

    _changePanel: function(event) {
      event.preventDefault();

      var panelName = $(event.currentTarget).closest("li").data("for");
      var panelView = this.views[panelName];

      // Close this panel if it's already open
      if(this.activePanel && this.options.closeOnOpenClick && this.activePanel.el === panelView.el) {
        panelView.hide();
        this.activePanel = null;
        return;
      }

      // Close all panels
      _.each(this.views, function(panel) {
        panel.hide();
      });

      // Show the new panel
      if(panelView) {
        this.activePanel = panelView;
        panelView.show();
      }
    },

    openSharePopup: function(event) {
      var sizes = {
        twitter: {
          width: 525, height: 400 },
        linkedin: {
          width: 600, height: 450 },
        facebook: {
          width: 525, height: 400 },
        "google+": {
          width: 525, height: 400 }
      };

      var provider = $(event.currentTarget).text().toLowerCase();
      var width = sizes[provider].width;
      var height = sizes[provider].height;
      var top = (screen.height / 2) - (height / 2);
      var left = (screen.width / 2) - (width / 2);

      var popup = window.open(
        event.currentTarget.href,
        "Share",
        "resizable=1,scrollbar=1,status=1,width=%w,height=%h,chrome=1,centerscreen=1,top=%t,left=%l%"
          .replace("%w", width)
          .replace("%h", height)
          .replace("%t", top)
          .replace("%l", left)
      );

      // If the popup actually opened, prevent the link from activating
      if(popup) {
        event.preventDefault();
      }
    }

  });

  return PanelView;
});

