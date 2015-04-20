/*
 * Small carousel subclass
 * A small carousel is one row of items that spans 100%
 */

define([
  "app",
  "models/show",
  "views/common/carousel",
  "views/common/spinner",
  "text!templates/allshows/menu.html"
], function(app, ShowModel, CarouselView, SpinnerView, tmplMenu) {
  "use strict";

  // Speed that the menu should open/close
  var SPEED = 200;

  // Create a spinner
  var spinner = new SpinnerView();

  $(document).on("mousemove", _.throttle(function(event) {
    if(!MenuView.openMenu) {
      return;
    }

    var $target = $(event.target);
    var isCarouselItem = event.target.className.indexOf("carousel-item") > 0;
    var isWithinCarouselItem = $target.closest(".carousel-item").length > 0;
    var isWithinTooltip = $target.closest(".ui-tooltip").length > 0;
    
    if(!isCarouselItem && !isWithinCarouselItem && !isWithinTooltip) {
      ListItemView.reset();
      MenuView.openMenu.hide();
      MenuView.openMenu = null;
    }
  }, 100));

  /*
   * Menu view
   */

  var MenuView = app.views.BaseView.extend({
    tagName: "menu",

    template: _.template(tmplMenu),

    initialize: function() {
      // When another menu opens close this one.
      app.on("advancedmenu:open", _.bind(function(instance) {
        if(instance.el !== this.el) {
          this.hide();
        }
      }, this));
    },

    render: function() {
      console.log('rendering context', this.options.data);
      this.$el.html(this.template(this.options.data));
      this._initItemTooltips();
      return this;
    },

    show: function() {
      clearTimeout(this.showTimer);

      var $li = this.$el.closest("li");

      this.showTimer = setTimeout(_.bind(function() {
        // $li.addClass("run-animation");
        this.$el.addClass("run-animation");
      }, this), 50);

      this.origHeight = this.$el.outerHeight(true);
      this.$el.stop().animate({
        height: "100%"
      }, SPEED);

      this.isOpen = true;
      MenuView.openMenu = this;
      app.trigger("advancedmenu:open", this);
    },

    hide: function() {
      var done = _.bind(function() {
        this.$("li").filter(function() {
          return $.data(this, "tooltips");
        }).tooltip("destroy");

        this.remove();
        this.isOpen = false;
        app.trigger("menu:close", this);
      }, this);

      // this.$el.closest("li").removeClass("run-animation");
      this.$el.removeClass("run-animation");

      this.$el.stop().animate({
        height: this.origHeight
      }, SPEED, done);
    },

    _initItemTooltips: function() {
      this.$("li").tooltip({
        items: ":has(> .tooltip)",
        position: {
          my: "center bottom-25",
          at: "center top",
          collision: "none",
          hide: SPEED,
          using: function(position) {
            $(this).css(position);
            $("<div />").addClass("arrow h-icon h-icon-tooltip-arrow").appendTo(this);
          }
        },
        content: function() {
          return $(this).find(".tooltip").html();
        }
      });
    }

  });


  /*
   * List item view
   */

  var ListItemView = app.views.BaseView.extend({
    events: {
      "mouseenter": "onMouseEnter"
    },

    initialize: function() {
      this.model = new ShowModel({
        url: this.$el.data("url")
      });
    },

    onMouseEnter: function() {
      var $li = this.$el;
      var $target = $li.find(".item-menu");

      // exit early if the menu is already open
      if(this.menu && this.menu.isOpen) {
        return;
      }

      // reset timers/abort ajax requests
      ListItemView.reset();

      spinner.$el.appendTo(this.$el.find(".thumb"));
      spinner.show();

      // Fetch data and render the menu
      ListItemView.dfd = this.model.fetch().done(_.bind(function(resp) {
        // Create menu if not exists
        if(!this.menu) {
          var context = {};
          context.title = $li.find(".title").first().text();
          context.description = $li.find(".desc").first().text();
          context.url = $li.find(".title").first().attr("href");
          context.shows = resp;
          this.menu = new MenuView({ data: context });
        }

        $target.append(this.menu.render().el);
        this.menu.show();
      }, this)).always(function() {
        spinner.hide();
      });
    }
  }, {
    dfd: null,
    reset: function() {
      if(ListItemView.dfd) {
        ListItemView.dfd.abort();
      }
    }
  });


  var AdvancedMenuCarousel = CarouselView.extend({
    cache: {},

    defaults: _.extend({}, CarouselView.prototype.defaults, {
      moveBy: 3,
      liPerPage: 2.25
    }),

    itemClass: ".item-menu",

    _initSubviews: function() {
      CarouselView.prototype._initSubviews.call(this);

      this.views.listItems = this.$(".carousel-item").map(function() {
        return new ListItemView({ el: this });
      }).get();
    },
    
    _onImageError: function(event) {
      console.warn('Image error', event.currentTarget);
      event.currentTarget.src = app.webroot + "/images/thumbnail-missing-270x270.jpg";
    }
  });

  return AdvancedMenuCarousel;

});
