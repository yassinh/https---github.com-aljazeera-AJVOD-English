// Taboola recommendations collection

define(["app"], function(app) {
  "use strict";

  var isArabic = function(text) {
    return (/[\u0600-\u06FF]/).test(text);
  };

  return Backbone.Collection.extend({
    url: "http://api.taboola.com/1.1/json/{publisherId}" +
      "/recommendations.get?app.type=desktop" +
      "&app.apikey={apikey}" +
      "&rec.count=10" +
      "&ra=url" + // only return videos with a url
      "&source.type=video" +
      "&source.id={sourceId}" +
      "&source.url={sourceUrl}" +
      "&ts={timestamp}" +
      "&trc_variant=visible",

    sync: function(method, model, options) {
      console.log('Taboola collection sync', options);
      var session = app.utils.getTaboolaSession();

      var url = this.url
        .replace("{apikey}", app.consts.TABOOLA_API_KEY)
        .replace("{publisherId}", app.consts.TABOOLA_PUBLISHER_ID)
        .replace("{sourceId}", options.data.id) // "/news/asia-pacific/2013/06/201362562410137991.html") // options.data.videoID
        .replace("{sourceUrl}", options.data.url.replace(/#/g, "")) // "http://www.aljazeera.com/news/asia-pacific/2013/06/201362562410137991.html");
        .replace("{timestamp}", new Date().getTime());

      // If there is an active taboola session use it
      if(session) {
        url += "&user.session=" + session.session;
        url += "&user.id=" + session.id;
      } else {
        url += "&user.session=init";
      }

      return $.ajax(url, {
        dataType: "jsonp",
        jsonp: "rec.callback"
      }).done(this.parse, this.cacheSession);
    },

    parse: function(results) {
      console.log('taboola results', results);
      var loc = window.location;

      results.list = _.map(results.list || [], function(video) {
        var date = new Date(video.created);
        video.created = "m-d-y".replace("m", date.getMonth() + 1)
          .replace("d", date.getDate())
          .replace("y", date.getFullYear());

        video.duration = app.utils.formatDuration(video.duration, {
          isSeconds: true
        });

        console.log("url before :" + video.url);

        var newURL = video.url.split('channels')[1];
        newURL = loc.protocol + "//" + loc.host + "/channels" + newURL;

        video.url = decodeURIComponent(newURL);

          // Replace taboola domains with the current domain
          //video.url = decodeURIComponent(video.url).replace("http%3A%2F%2Fajvod-env-dev.elasticbeanstalk.com", function (match, p1, p2) {
          //    return encodeURIComponent(loc.protocol + "//" + loc.host + ":" + loc.port);
          //}).replace("http%3A%2F%2Fvideo.aljazeera.com", function (match, p1, p2) {
          //    return encodeURIComponent(loc.protocol + "//" + loc.host +':'+ loc.port);
          //});
        console.log("url after :" + video.url);

        // Set dir based on the category
        if(_.indexOf(video.categories, "ara") > -1 || isArabic(video.name)) {
          video.dir = "rtl";
        } else {
          video.dir = "ltr";
        }

        // Go through each thumb and return the last one (largest one)
        // that has a url
        var thumb;
        while(thumb = video.thumbnail.reverse().shift()) {
          if(thumb.url) {
            video.thumbnail = thumb;
            break;
          }
        }

        return video;
      });

      return results;
    },

    cacheSession: function(results) {
      console.log('Caching taboola session', results.session, results.id);

      sessionStorage.setItem(app.consts.TABOOLA_SESSION_CACHE_KEY, JSON.stringify({
        session: results.session,
        id: results.id
      }));
    }
  });
});
