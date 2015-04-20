define([
  "app",
  "views/common/carousel",
  "views/common/spinner",
  "collections/taboola",
  "text!templates/yourchannel/recommendations.html"
], function (app, CarouselView, SpinnerView, TaboolaCollection, tmplRecommendations) {
    "use strict";

    var BaseView = app.views.BaseView;
    var cacheKey = app.consts.TABOOLA_CACHE_KEY;

    return BaseView.extend({
        template: _.template(tmplRecommendations),

        events: {
            "click li": "_storeVideoID"
        },

        initialize: function () {
            BaseView.prototype.initialize.call(this);
            console.log('Recommendations view initalized', this.el);
            this._fetch();
        },

        render: function (resp) {
            var didError = resp && resp.statusText && resp.statusText === "error";
            var hasVideos = resp && resp.list && resp.list.length;

            if (didError || !hasVideos) {
                console.warn('Taboola request failed or no recommendations were found', JSON.stringify(resp));

                if (this.spinner) {
                    this.spinner.remove();
                    this.spinner = null;
                }

                return;
            }

            resp.localization = app.localization;
            resp.storedVideo = this._getStoredVideo();

            console.log('recommended videos ' + JSON.stringify(resp));

            this.$el.html(this.template(resp));
            var $carousel = this.$el.find(".carousel");

            if (hasVideos) {
                this.views.carousel = new CarouselView({
                    el: $carousel
                });

                this._initItemTooltips($carousel);
            }
        },

        _fetch: function () {
            var localData = this._getStoredVideo();

            // Bail if a video ID doesn't exist in localStorage.
            if (!localData) {
                console.warn("Recommendations: no video found in localStorage; not making request to taboola");
                this.render();
                return;
            }

            // Create a spinner and show it
            var spinner = this.spinner = new SpinnerView({
                style: "dark"
            });

            // Inject spinner
            this.$el.append(spinner.show().el);

            // Create a taboola collection
            var collection = new TaboolaCollection();

            // Fetch results
            collection.fetch({ data: { id: localData.id, url: localData.url } })
              .done(_.bind(this.render, this))
              .done(_.bind(this._setBackground, this))
              .fail(_.bind(this.render, this));
        },

        _setBackground: function (resp) {
            if (!resp.list.length || !Modernizr.backgroundsize) return;
            var thumb = resp.list[0].thumbnail.url;
            var $background = this.$el.siblings(".background");
            $background.css("background-image", "url(" + thumb + ")");
        },

        _storeVideoID: function (event) {
            var id = $(event.currentTarget).data("video-id");
            if (!id) return;
            localStorage.setItem(app.consts.TABOOLA_CLICKED_VIDEO_ID, id);
        },

        _getStoredVideo: function () {
            var video = localStorage.getItem(cacheKey);
            return video ? JSON.parse(video) : null;
        }
    });

});

