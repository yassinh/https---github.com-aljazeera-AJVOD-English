/*
 * This file contains variables/utilities
 * that are common to all files across all
 * tests.
 *
 * Things are exposed as global vars, but this
 * should be changed.
 *
 * App is defined as a dependency to pull in underscore,
 * jquery, etc.
 */

define(["app"], function() {
  // Reference to the Backbone view created in each test
  window.view;

  // Carousel template
  window.tmpl = _.template(
    '<div class="carousel">' +
      '<header class="heading">' +
        '<div class="h-content">' +
          '<p class="title">Test Carousel</p>' +
          '<div class="links">' +
            '<form>' +
              '<select></select>' +
            '</form>' +
          '</div>' +
        '</div>' +
      '</header>' +
      '<div class="next-prev">' + 
        '<a class="next">next</a>' +
        '<a class="prev">prev</a>' +
      '</div>' +
      '<div class="carousel-hook">' +
        '<ul>' +
          '<li>' +
            '<div class="thumb">' +
              '<img data-src="http://placehold.it/10x10" alt="">' + 
            '</div>' +
          '</li>' +
          '<li>' +
            '<div class="thumb">' +
              '<img data-src="http://placehold.it/10x10" alt="">' + 
            '</div>' +
          '</li>' +
          '<li>' +
            '<div class="thumb">' +
              '<img data-src="http://placehold.it/10x10" alt="">' + 
            '</div>' +
          '</li>' +
        '</ul>' +
      '</div>' +
      '<footer>' +
        '<ul></ul>' +
      '</footer>' +
    '</div>'
  );

  // Create a carousel instance. Pass in the constructor
  // for the carousel you're currently testing
  window.create = function(Obj, opts) {
    return function(done) {
      var $target = $(tmpl()).appendTo("body");

      $target.on("createend.jcarousel", function() {
        setTimeout(done, 50);
      });

      view = new Obj({
        el: $target,
        options: _.extend({
          liPerPage: 1,
          moveBy: 1
        }, opts || { animation: 0 })
      });
    };
  };

  window.destroy = function(done) {
    view.destroy();
    view = null;
    done();
  };

  window.getNext = function() {
    return view.$el.find(".next");
  };

  window.getPrev = function() {
    return view.$el.find(".prev");
  };

  window.getAutoScrollObj = function() {
    return view.$carousel.data("jcarouselAutoscroll");
  };

});
