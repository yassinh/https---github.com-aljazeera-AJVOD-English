/*
 * Navigation module
 */

define([
  "app",
  "views/base/menu",
  "views/common/overlay"
], function(app, MenuView, overlayView) {
  "use strict";

  var BaseView = app.views.BaseView;
  var FADE_SPEED = app.consts.OVERLAY_FADE_SPEED;

  /*
   * Navigation menu view
   */
  var NavMenuView = MenuView.extend({
    initialize: function() {
      MenuView.prototype.initialize.call(this);

      // Prepare menu for animating in in the grid items
      if(this.options.dropInGridItems) {
        this.$lis = this.$("li").addClass("h-drop-in-item");
        this.$el.addClass("h-drop-in");
      }
    },

    open: function() {
      MenuView.prototype.open.apply(this, arguments);
      if(!this.options.dropInGridItems) return;

      var $lis = this.$lis;

      $lis.each(function(i) {
        setTimeout(function() {
          $lis.eq(i).addClass("h-drop-in-run");
        }, 30 * i);
      });
    },

    // Reset list item transitions on close
    close: function() {
      MenuView.prototype.close.apply(this, arguments);
      if(!this.options.dropInGridItems) return;
      this.$lis.removeClass("h-drop-in-run");
    }
  });

  /*
   * Navigation item view
   * Responsible for each list item and it's menu
   */
  var NavItemView = BaseView.extend({
    _initSubviews: function() {
      BaseView.prototype._initSubviews.call(this);

      var menuName = this.options.menuName;
      console.log('Creating nav menu item for', menuName);

      this.views.menu = new NavMenuView({
        el: $('menu[data-for="' + menuName + '"]'),
        dropInGridItems: true,
        fade: false
      });
    },

    _bindEvents: function() {
      BaseView.prototype._bindEvents.call(this);

      // Make this nav item inactive if the close originated on the menu
      this.listenTo(this.views.menu, "close", function() {
        this.close();
      });
    },

    isOpen: function() {
      return this.views.menu.isOpen;
    },

    getMenu: function() {
      return this.views.menu;
    },

    open: function() {
      this.views.menu.open();
      this.$el.addClass("open");
    },

    close: function() {
      this.$el.removeClass("open");

      if(this.isOpen()) {
        this.views.menu.close();
      }
    }
  });

  /*
   * Top-level navigation mediator
   * el: $("#header")
   */
  return BaseView.extend({
    events: {
      "click li": "_openMenu"
    },

    initialize: function() {
      BaseView.prototype.initialize.call(this);

      // Grab a reference to the overlay singleton
      this.views.overlay = overlayView();

      // cache the nav element and make it sticky
      this.$nav = this.$("nav").sticky({
        onStick: function($container) {
          $container.wrapInner('<div class="h-container"></div>');
        },
        onUnStick: function($container) {
          $container.find(".h-container").children().unwrap();
        }
      });

      // Create a NavItemView for each list item that has
      // a sub menu.
      this.$el.find("li[data-menu]").each(_.bind(function(i, elem) {
        var menuName = $(elem).data("menu");

        this.views[menuName] = new NavItemView({
          el: elem,
          menuName: menuName
        });
      }, this));
    },

    getMenu: function(menuName) {
      return this.views[menuName] ? this.views[menuName].getMenu() : null;
    },

    setActiveTile: function(menuName, selector) {
      var menu = this.getMenu(menuName);
      var $tile = menu.$el.find(selector);
      $tile.addClass("is-selected");
      $tile.siblings().removeClass("is-selected");
    },

    _openMenu: function(event) {
      var menuName = $(event.currentTarget).data("menu");
      var navItem = this.views[menuName];

      if(!navItem) {
        return;
      }

      event.preventDefault();

      if(!navItem.isOpen()) {
        navItem.open();
      } else {
        navItem.close();
      }
    }
  });

});
