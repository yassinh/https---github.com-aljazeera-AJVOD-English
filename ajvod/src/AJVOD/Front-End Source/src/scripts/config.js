// Configuration options used by all pages
// during build.

require.config({
  baseDir: ".",

  optimize: "uglify",

  wrap: true,

  // Additional paths, relative to baseUrl
  paths: {
    // app file which all other modules should
    // depend on. not to be confused with the 
    // app directory.
    app: "app/app",
    api: "app/api",

    // libs
    underscore: "libs/underscore",
    backbone: "libs/backbone",
    jquery: "libs/jquery",
    jqueryui: "libs/jquery.ui",
    jqueryuiselect: "libs/jquery.ui.selectmenu",
    jqueryraf: "libs/jquery.requestAnimationFrame",
    jquerynotify: "libs/jquery.notify",
    carousel: "libs/jquery.jcarousel",
    sortElements: "libs/jquery.sortelements",
    sticky: "libs/jquery.sticky",
    iscroll: "libs/iscroll",

    // Shortcuts to app source
    views: "app/views",
    models: "app/models",
    collections: "app/collections",
    routers: "app/routers"
  },

  // Shim non-AMD scripts
  shim: {
    underscore: {
      exports: "_"
    },
    backbone: {
      deps: ["jquery", "underscore"],
      exports: "Backbone"
    },
    iscroll: {
      exports: "iScroll"
    }
  }
});
