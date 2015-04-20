// Big Screen dialog view
define([
  "app",
  "views/common/dialog",
  "views/common/carousel",
  "views/common/player",
  "text!templates/big-screen.html"
], function(app, DialogView, CarouselView, PlayerView, tmplBigScreen) {
  "use strict";

  var $win = $(window);

  return DialogView.extend({
    template: _.template(tmplBigScreen),

    defaults: _.extend({}, DialogView.prototype.defaults, {
      dialogClass: "big-screen",
      closeWhenOthersOpen: false,
      autoOpen: false,
      fixed: false,
      show: 250,
      hide: 250,
      position: {
        using: function(pos, ui) {
          ui.element.element.css({
            position: "fixed",
            top: 0,
            left: 0,
            zIndex: 1000,
            width: "100%",
            height: "100%"
          }).find(".ui-dialog-content").css("height", "100%");
        }
      }
    }),

    events: _.extend({}, DialogView.prototype.events, {
      "click .bs-tab a": "_toggleCarousel",
      "click .carousel-hook li": "_onItemClick",
      "mouseenter .bs-tab a": "_onTabHover",
      "mouseleave .bs-tab a": "_onTabHover",
      "mouseenter .bs-carousel": "_stopTimer",
      "mouseleave .bs-carousel": "_startTimer"
    }),

    initialize: function() {
      this.currentIndex = this.options.currentIndex || 0;
      // this._bindEvents();
      DialogView.prototype.initialize.call(this);
      this.open();
      console.log('Initializing big screen view', this);
    },

    destroy: function() {
      this._stopTimer();
      if(this.views.carousel) {
        this.views.carousel.destroy();
        this.views.carousel = null;
      }
      if(this.views.player) {
        this.views.player.destroy();
        this.views.player = null;
      }
      $win.off(".bigscreen");
      DialogView.prototype.destroy.call(this);
    },

    close: function() {
      DialogView.prototype.close.apply(this, arguments);
      app.router.navigate("index", true);
    },

    _render: function() {
      var context = this._createContext();
      console.log('Rendering big screen context', context);
      this.$el.html(this.template(context));
      this._cacheElements();
      this._initItemTooltips(this.$el, {
        tooltipClass: "tooltip-big-screen"
      });
    },

    _cacheElements: function() {
      this.$lis = this.$(".carousel-hook li");
      this.$items = this.$lis.find(".item");
      this.$playlist = this.$(".carousel");
      this.$playlistContainer = this.$(".bs-carousel");
    },

    _bindEvents: function() {
      DialogView.prototype._bindEvents.call(this);

      this.listenTo(this, "open", this._onDialogOpen);
      var throttleResize = _.throttle(_.bind(this._onWindowResize, this), 100);
      $win.on("resize.bigscreen", throttleResize);

      // IE/FF on windows doesn't fire resize correctly when the minimize/max
      // buttons are clicked on, so we have to debounce it.
      var debounceResize = _.debounce(_.bind(this._onWindowResize, this), 100);
      $win.on("resize.bigscreen", debounceResize);
    },

    _onDialogOpen: function() {
      app.trigger("bigscreen:open");
      this._render();
      this._createCarousel();
      this._createPlayer();
      this._setActiveItem(this.currentIndex);
      this._setPlayerHeight();
      this._startTimer();
      if("brightcove" in window) {
        brightcove.createExperiences();
      }
    },

    _onItemClick: function(event) {
      if($(event.target).hasClass("show")) {
        return;
      }

      event.preventDefault();
      this.currentIndex = $(event.currentTarget).index();
      this._setActiveItem(this.currentIndex);
    },

    _onTabHover: function(event) {
      this.$(".bs-tab").toggleClass("is-hover", event.type === "mouseenter");
    },

    _onWindowResize: function() {
      this._setPlayerHeight();
    },

    /**
     * Loads a video into the player and sets the active carousel item.
     * @param {Number} id - the video ID to load
     */
    _loadVideo: function(id) {
      console.log('Loading video', id);
      this.views.player.loadVideo(id);
    },

    /**
     * Serializes a carousel items metadata into JSON so that the carousel
     * can be re-rendered in the overlay dynamically.
     */
    _createContext: function() {
      var $items = this.options.$items;
      var ret = {};
      ret.player_id = app.player_id;
      ret.player_key = app.player_key;
      ret.title = this.options.title;

      // Serialize carousel into an object so the template can be rendered
      ret.videos = $items.map(function(i, elem) {
        var $elem = $(elem);
        var date = $elem.find(".date").contents();
        var showLink = $elem.find(".footer a").not(".h-icon");

        return {
          id: $elem.data("video-id"),
          title: $elem.find(".title").text(),
          desc: $elem.find(".desc").text(),
          duration: date[0].innerText || "",
          created: date[1].textContent || date[1].nodeValue || "",
          show: showLink.text(),
          showUrl: showLink.attr("href"),
          thumb: $elem.find(".thumb img").map(function() {
            return (/^data/).test(this.src) ? this.getAttribute("data-src") : this.src;
          })[0]
        };
      }).get();

      return ret;
    },

    /**
     * Sets the active carousel item.
     * @param {Number|jQuery Object} index - 0-based item index or the item itself
     */
    _setActiveItem: function(index) {
      this.$items.removeClass("is-active");
      this.$items.eq(index).addClass("is-active");
      this._loadVideo(this.$lis.eq(this.currentIndex).data("video-id"));
    },

    /**
     * Set the height of the player
     */
    _setPlayerHeight: function() {
      var el = this.views.player.el;
      var playlistHeight = this.$playlistContainer.outerHeight(true);
      var height = this.$el.height() - playlistHeight;
      var width = height * (16 / 9); // 16:9
      var top;

      if(width > window.innerWidth) {
        width = this.$el.width();
        height = width * (9 / 16);
        top = ((window.innerHeight - playlistHeight) / 2) - (height / 2) + "px";
      } else {
        top = "auto";
      }

      el.style.top = top;
      el.style.width = width + "px";
      el.style.height = height + "px";
    },

    /********** Carousel methods **********/

    /**
     * Creates a new carousel view
     */
    _createCarousel: function() {
      this.views.carousel = new CarouselView({
        el: this.$(".carousel")
      });
    },

    /**
     * Create the player view and auto advance functions
     */
    _createPlayer: function() {
      this.views.player = new PlayerView({
        el: this.$(".bs-player")
      });

      this.listenTo(this.views.player, "complete", this._advanceCarousel);

      this.listenTo(this.views.player, "enter_fullscreen", function() {
        this.options.closeOnEscPress = false;
      });

      // Prevent the esc key from closing this dialog when in full screen
      // mode. The timeout here is hacky, but necessary because the player
      // is already closed by the time the event is fired.
      this.listenTo(this.views.player, "exit_fullscreen", function () {
        setTimeout(_.bind(function() {
          this.options.closeOnEscPress = true;
        }, this), 100);
      });
    },

    /**
     * Toggles the carousel open/closed
     * @param {Event object} event
     */
    _toggleCarousel: function(event) {
      if(event) event.preventDefault();
      var flag = this.$el.hasClass("is-contracted");
      this.isOpen = flag;
      this._stopTimer();
      this.$el.toggleClass("is-contracted", !flag);
      var carousel = this.views.carousel;
      var setPlayerHeight = _.bind(this._setPlayerHeight, this);

      this.$playlist[flag ? "slideDown" : "slideUp"]({
        start: function() {
          if(carousel) {
            carousel._setHeight();
          }
        },
        progress: function() {
          setPlayerHeight();
        },
        done: _.bind(function() {
          if(carousel) {
            carousel.options.refreshOnResize = flag;
          }
          setPlayerHeight();
        }, this)
      }, {
        easing: "linear"
      });
    },

    /**
     * Scroll the carousel to the next item and load it into
     * the video player.
     */
    _advanceCarousel: function() {
      this.currentIndex++;
      var $nextLi = this.$lis.eq(this.currentIndex);
      var id = $nextLi.data("video-id");

      if(!id) {
        console.warn('Tried to advance the carousel to a null id', id, $nextLi);
      }

      if($nextLi.length) {
        this.views.carousel.scrollToElem($nextLi);
        this._setActiveItem(this.currentIndex);
      }
    },

    /* *********** timer stuff *************/

    /**
     * Start the timer to slide the carousel down
     */
    _startTimer: function() {
      if(!this.isOpen) return;
      this._stopTimer();
      this._hideTimer = setTimeout(_.bind(function() {
        this._toggleCarousel();
      }, this), 2 * 1000);
    },

    /**
     * Stop the timer that slides the carousel down
     */
    _stopTimer: function() {
      clearTimeout(this._hideTimer);
    }
  });

});
