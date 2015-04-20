/*
 * Top-level Your Channel page view
 */

define([
  "app",
  "views/common/videopage",
  "views/common/carousel",
  "views/yourchannel/recommendations",
  "views/common/gigya",
  "text!templates/yourchannel/empty-carousel.html"
], function (app, VideoPageView, CarouselView, RecommendationsView, GigyaView, tmplEmptyCarousel) {
    "use strict";

    var DIRTY_CACHE_KEY = "alj-was-dirty";
    tmplEmptyCarousel = _.template(tmplEmptyCarousel);

    return VideoPageView.extend({
        initialize: function () {
            VideoPageView.prototype.initialize.call(this);

            this.carousels = {};
            this.$unfinishedItems = this.$("#tab-unfinished li");
            this._isDirty = false;
            this._initTabs();
            this._createCarousels();
            this._storeLatestSubscriptionDate();

            // Set the Your Channel tile active in the menu when logged in
            GigyaView.isLoggedIn().done(_.bind(this._setActiveChannelTile, this));

            // Set unfinished videos height
            var _setItemHeight = _.bind(this._setItemHeight, this);
            $(window).on("resize", _.throttle(_setItemHeight, 100));
            this.$unfinishedItems.filter(":nth-child(4)").addClass("nth-child-4");
        },

        _bindEvents: function () {
            VideoPageView.prototype._bindEvents.call(this);

            // Add RTL support on tooltips if the item the tooltip is displaying
            // for is RTL.
            $(document).on("tooltipopen", function (event, ui) {
                var $target = $(event.target);
                var $item = $target.find(".item");
                var $carousel = $target.closest("[dir]");

                if ($carousel.attr("dir") === "rtl" || $item.attr("dir") === "rtl") {
                    ui.tooltip.attr("dir", "rtl");
                }
            });

            this.listenTo(app, "subscriptions:reorder:success", this._reorderCarousels);
            this.listenTo(app, "subscriptions:remove:success", this._removeCarousel);
            this.listenTo(app, "playlists:rename:success", this._renameCarousel);
            this.listenTo(app, "playlists:reorder:success", this._reorderCarousels);
            this.listenTo(app, "playlists:create:success", this._addCarousel);
            this.listenTo(app, "playlists:delete:success", this._removeCarousel);
            this.listenTo(app, "playlists:remove:success", this._removeCarouselItem);
            this.listenTo(app, "playlists:remove:success", this._setDirty);
            this.listenTo(app, "playlists:add:success", this._setDirty);

            this.listenTo(app, "tabs:activate", $.proxy(function () {
                if (this.views.help) {
                    this.views.help.close();
                }

                this._refreshCarousels();
            }, this));

            $(window).on("hashchange", _.bind(function () {
                this._activateUnfinishedVideos();
            }, this));
        },

        _initSubviews: function () {
            VideoPageView.prototype._initSubviews.call(this);

            this.views.recommendations = new RecommendationsView({
                el: this.$("#recommendations")
            });
        },

        _initTabs: function () {
            var opts = {
                active: 0,
                beforeActivate: _.bind(function (event, ui) {
                    if (ui.newPanel.attr("id") === "tab-playlists" && this._isDirty) {
                        console.warn('Playlist tab activated, reloading page..');
                        app.trigger("spinner:show");
                        window.location.reload();
                        localStorage.setItem(DIRTY_CACHE_KEY, true);
                        return false;
                    }
                }, this),
                activate: _.bind(function (event, ui) {
                    this.$activePanel = ui.newPanel;
                    this._createCarousels();
                    this._setItemHeight();
                    app.trigger("tabs:activate");
                }, this),
                create: _.bind(function (event, ui) {
                    this.$activePanel = ui.panel;
                    this._activateUnfinishedVideos();
                }, this)
            };

            if (localStorage.getItem(DIRTY_CACHE_KEY)) {
                localStorage.removeItem(DIRTY_CACHE_KEY);
                opts.active = 1;
            }

            this.$(".ui-tabs").tabs(opts);
        },

        _storeLatestSubscriptionDate: function () {
            // Find the latest timestamp across all subscriptions
            var lastDate = 0, date;
            var storedDate = localStorage.getItem(app.consts.LATEST_SUBSCRIPTION_VIDEO_DATE);
            this.$("#tab-subscriptions .date").contents().each(function () {
                if (this.nodeType === 3) {
                    date = new Date(this.nodeValue).getTime();
                    if (date > lastDate) lastDate = date;
                }
            });

            // If there is a date store it so we can determine whether or not
            // there are subscriptions newer than this one.
            if ((!storedDate && lastDate) || (storedDate && lastDate > storedDate)) {
                console.log('Recording last subscription video date', lastDate);
                localStorage.setItem(app.consts.LATEST_SUBSCRIPTION_VIDEO_DATE, lastDate);
            }
        },

        _renameCarousel: function (data) {
            this.$activePanel
              .find('[data-playlist-id="' + data.playlistId + '"]')
              .find(".heading .title")
              .text(data.name);
        },

        _removeCarouselItem: function (data) {
            var activePanel = this.$activePanel.attr("id");

            // Only want to this this on the playlists tab
            if (activePanel !== "tab-playlists") {
                return;
            }

            var $carousel = this.$(".carousel").filter(function () {
                return $(this).data("playlist-id") === data.playlistId;
            });
            var $item = $carousel.find(".item").filter(function () {
                return $(this).data("video-id") === data.videoId;
            });

            if (!$item.length) {
                console.error('Tried to remove a carousel item but it could not be found', data);
                return;
            }

            _.each(this.carousels[activePanel], function (carousel) {
                // skip if we're not in the right carousel for this video id
                if (carousel.$el.data("playlist-id") === data.playlistId) {
                    carousel.removeItem($item, function ($items) {
                        if ($items.length === 0) {
                            var $body = $carousel.find(".body");
                            $body.addClass("h-hidden");
                            $body.filter(".empty").removeClass("h-hidden");
                            $carousel.find(".do-big-screen").fadeOut();
                        }
                    });
                }
            });
        },

        /**
         * Re-orders the carousels inside the active panel
         * @param {object} data
         */
        _reorderCarousels: function (data) {
            var ids = data.ids;
            var $activePanel = this.$activePanel;
            var $bar = $activePanel.children(".top");
            var $carousels = $activePanel.find(".carousel");
            var $source, $target, view, i;

            // Kill off all carousels
            this._destroyCarousels();

            // Change DOM order
            _.each(ids.reverse(), _.bind(function (id, i) {
                $carousels.filter(function () {
                    return $(this).data("id") === id;
                }).prependTo($activePanel);
            }, this));

            // Make sure the unsubscribe link bar is above everything
            $bar.prependTo($activePanel);

            // Re-create carousels
            setTimeout(_.bind(function () {
                this._createCarousels();
            }, this), 100);
        },

        /**
         * Add a new playlist carousel
         * @param {object} data - data about the new playlist
         */
        _addCarousel: function (data) {
            var html = tmplEmptyCarousel({
                id: data.playlistId,
                name: data.playlistName || "",
                dir: "ltr",
                localization: app.localization
            });

            $("#playlists-target").prepend(html);
            this._createCarousels();
            this.$activePanel.find(".no-playlists").addClass("h-hidden");
        },

        /**
         * Remove a carousel
         * @param {object} data - data about the carousel that was removed
         */
        _removeCarousel: function (data) {
            var tabId = this.$activePanel.attr("id");
            var id = data.id || data.playlistId;
            var view;

            // Alter the carousels subview to remove this carousel,
            // but also save a reference to it
            this.carousels[tabId] = _.reject(this.carousels[tabId], function (carousel) {
                if (carousel.$el.data("id") === id) {
                    view = carousel;
                    return true;
                }

                return false;
            });

            // Remove the carouel
            if (view) {
                view.$el.fadeOut(250, function () {
                    view.destroy();
                    view.remove();

                    // tell the dialog to reposition itself
                    app.trigger("yourchannel:carouselRemoved");
                });
            }

            // Show the no subscriptions text if the user removed the
            // last carousel
            if (!this.carousels[tabId].length) {
                this.$activePanel.find(".no-playlists").removeClass("h-hidden");
                this.$activePanel.find(".edit-subscriptions").parent().addClass("h-hidden");
            }
        },

        /**
         * Create carousel instances inside of the currently active panel
         */
        _createCarousels: function () {
            if (!this.$activePanel) {
                console.error('Tried to create carousels without an active panel');
                return;
            }

            var tabId = this.$activePanel.attr("id");

            this.carousels[tabId] = this.$activePanel.find(".carousel").map(function () {
                var $hook = $(this).find(".carousel-hook");
                var obj = $hook.data("bcc-carousel");

                if (obj) {
                    return obj;
                }

                obj = new CarouselView({ el: this });
                $.data(this, "bcc-carousel", obj);
                return obj;
            }).get();
        },

        /**
         * Destroy carousel instances inside the currently active panel
         */
        _destroyCarousels: function () {
            if (!this.$activePanel) {
                console.error('Tried to create carousels without an active panel');
                return;
            }

            var tabId = this.$activePanel.attr("id");

            _.each(this.carousels[tabId], function (obj) {
                obj.destroy();
            });

            delete this.carousels[tabId];
        },

        /**
         * Refresh carousels in the active panel
         */
        _refreshCarousels: function () {
            var tabId = this.$activePanel.attr("id");

            _.each(this.carousels[tabId], function (obj) {
                obj._refresh();
            });
        },

        /**
         * Set the height of the large item inside of the unfinished
         * tab correctly.
         */
        _setItemHeight: function () {
            if (this.$activePanel.attr("id") !== "tab-unfinished") {
                return;
            }

            this.$unfinishedItems.filter(".first-child").height(function () {
                var $smallLi = $(this).siblings().first();
                return ($smallLi.height() * 2) + (Math.floor(parseFloat($smallLi.css("margin-top"))) * 2);
            });
        },

        _setActiveChannelTile: function () {
            this.views.navigation.setActiveTile("channel", ".your-channel");
        },

        _setDirty: function () {
            this._isDirty = true;
        },

        _activateUnfinishedVideos: function () {
            if (location.hash.replace("#", "") === "unfinished-videos") {
                this.$(".ui-tabs").tabs("option", "active", 2);
                location.hash = "index";
            }
        }

    });

});
