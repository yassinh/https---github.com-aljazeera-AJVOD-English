require.config({
  // Base dir is the source scripts directory
  baseUrl: "../../src/scripts",

  // Cache bust all the things
  urlArgs: 'cb=' + Math.random(),

  // Setup paths relative to the baseDir
  paths: {
    app: "app/app",
    views: "app/views",
    models: "app/models",
    collections: "app/collections",

    tests: "../../test/mocha/tests",

    underscore: "libs/underscore",
    backbone: "libs/backbone",
    jquery: "libs/jquery",
    jqueryui: "libs/jquery.ui",
    jqueryuiselect: "libs/jquery.ui.selectmenu",
    jqueryraf: "libs/jquery.requestAnimationFrame",
    jqueryellipsis: "libs/jquery.ellipsis",
    carousel: "libs/jquery.jcarousel",
    sortElements: "libs/jquery.sortelements",
    sticky: "libs/jquery.sticky"
  },

  shim: {
    jasmine: {
      exports: "jasmine"
    },
    "jasmine-html": {
      deps: ["jasmine"],
      exports: "jasmine"
    },
    underscore: {
      exports: "_"
    },
    backbone: {
      deps: ["jquery", "underscore"],
      exports: "Backbone"
    },
    carousel: ["jquery"],
    sortElements: ["jquery"],
    sticky: ["jquery"],
    jqueryui: ["jquery"],
    jqueryuiselect: ["jqueryui"],
    jqueryraf: ["jquery"],
    jqueryellipsis: ["jquery"]
  }

});

// Run all specs when the app is ready
require([
  "tests/common",
  "tests/carousel",
  "tests/carousel-featured"
], function() {
  if (window.mochaPhantomJS) { mochaPhantomJS.run(); }
  else { mocha.run(); }
});

