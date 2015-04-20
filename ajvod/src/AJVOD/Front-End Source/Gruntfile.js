/*global module:false*/
/*jshint evil:true sub:true*/
module.exports = function(grunt) {
  "use strict";
  var _ = grunt.util._;
  var path = require("path");

  // Make dust helpers available
  var dust = require("dustjs-linkedin");
  var dustHelpers = require("dustjs-helpers");

  // Generic way to evaluate javascript inside of Dust
  dust.helpers["eval"] = function(chunk, context, bodies, params) {
    var script = dust.helpers.tap(params.script, chunk, context);
    chunk.write(eval(script));
    return chunk;
  };

  // Iterate over an object
  dust.helpers["iterate"] = function(chunk, context, bodies, params) {
    params = params || {};
    var obj = params['on'] || context.current();
    for (var k in obj) {
      chunk = chunk.render(bodies.block, context.push({key: k, value: obj[k]}));
    }
    return chunk;
  };


  // Creates configuration options for the page passed in, and
  // changes the optimization settings depending if it's a dev
  // or dist target.
  var makeRequireConfig = (function() {
    var options = {
      name: "page-{page}", // Name of the module
      out: "dist/scripts/page-{page}.js", // Location of the built file
      mainConfigFile: "src/scripts/config.js", // Build config file
      almond: true, // Use almond so we don't have another dependency on require in production
      insertRequire: ["page-{page}"], // Insert a require call to kick off this module
      optimize: "none"
    };

    // This function loops through all the options above and replaces
    // the tokenized `page` variable with the actual page, like `home`.
    return function(page) {
      var opts = _.clone(options);

      function replace(str) {
        return str.replace("{page}", page);
      }

      for(var key in opts) {
        var value = opts[key];

        if(_.isArray(value)) {
          opts[key] = _.map(value, function(val) {
            return replace(val);
          });
        }
        if(_.isString(value)) {
          opts[key] = replace(value);
        }
      }

      return { options: opts };
    };
  })();

  // Creates Scss config object
  var makeSassConfig = (function() {
    return function(pageName) {
      var ret = { files: {} };
      ret.files["dist/styles/" + pageName + ".css"] = "src/styles/" + pageName  + ".scss";
      return ret;
    };
  })();

  // Creates a dust configuration object for the page specified.
  // pageName var is assumed to have the language in it, like home-ar.
  var makeDustConfig = (function() {
    return function(pageName, lang) {
      var ret = { options: {}, files: {} };
      ret.options.lang = lang;
      ret.files["dist/" + pageName + "-" + lang + ".html"] = ["src/templates/" + pageName + ".dust"];
      return ret;
    };
  })();

  // Project configuration.
  grunt.initConfig({

    // Read the package.json file
    pkg: grunt.file.readJSON("package.json"),

    // remove lingering dist files
    clean: {
      all: ["dist/"]
    },

    // copy images/other static files
    copy: {
      all: {
        files: [
          { src: ["src/scripts/libs/modernizr.js"], dest: "dist/scripts/libs/modernizr.js" },
          { src: ["src/.htaccess"], dest: "dist/.htaccess" },
          { src: ["src/backgroundsize.min.htc"], dest: "dist/backgroundsize.min.htc" },
          { src: ["**"], dest: "dist/images/", cwd: "src/images", expand: true },
          { src: ["**"], dest: "dist/fonts/", cwd: "src/fonts", expand: true }
        ]
      }
    },

    // compress images
    smushit: {
      all: {
        src: [
          "dist/images/**/*.png",
          "dist/images/**/*.jpg"
        ]
      }
    },

    // Require JS compilation
    requirejs: {
      home: makeRequireConfig("home"),
      about: makeRequireConfig("about"),
      channel: makeRequireConfig("channel"),
      yourchannel: makeRequireConfig("yourchannel"),
      detail: makeRequireConfig("detail"),
      allshows: makeRequireConfig("allshows"),
      show: makeRequireConfig("show"),
      topic: makeRequireConfig("topic"),
      topics: makeRequireConfig("topics"),
      search: makeRequireConfig("search"),
      live: makeRequireConfig("live"),
      events: makeRequireConfig("events"),
      epg: makeRequireConfig("epg"),
      error: makeRequireConfig("error")
    },

    // Sass compilation
    sass: {
      options: {
        style: "expanded"
        // sourcemap: true
      },
      home: makeSassConfig("home"),
      about: makeSassConfig("about"),
      channel: makeSassConfig("channel"),
      yourchannel: makeSassConfig("yourchannel"),
      detail: makeSassConfig("detail"),
      allshows: makeSassConfig("allshows"),
      show: makeSassConfig("show"),
      topic: makeSassConfig("topic"),
      topics: makeSassConfig("topics"),
      search: makeSassConfig("search"),
      live: makeSassConfig("live"),
      events: makeSassConfig("events"),
      epg: makeSassConfig("epg"),
      error: makeSassConfig("error")
    },

    // Render dust templates to HTML
    dusthtml: {
      options: {
        whitespace: true,
        basePath: "src/templates",
        context: [
          "data/<%= grunt.task.current.target %>.json",
          "data/navigation.json",
          "data/localization.json"
        ]
      },

      // Home Page
      "home-en": makeDustConfig("home", "en"),
      "home-ar": makeDustConfig("home", "ar"),

      // About Page
      "about-en": makeDustConfig("about", "en"),
      "about-ar": makeDustConfig("about", "ar"),

      // Channel Page
      "channel-en": makeDustConfig("channel", "en"),
      "channel-ar": makeDustConfig("channel", "ar"),

      // Your channel Page
      "yourchannel-en": makeDustConfig("yourchannel", "en"),
      "yourchannel-ar": makeDustConfig("yourchannel", "ar"),

      // Search Page
      "search-en": makeDustConfig("search", "en"),
      "search-ar": makeDustConfig("search", "ar"),

      // Detail Page
      "detail-en": makeDustConfig("detail", "en"),
      "detail-ar": makeDustConfig("detail", "ar"),

      // All shows Page
      "allshows-en": makeDustConfig("allshows", "en"),
      "allshows-ar": makeDustConfig("allshows", "ar"),

      // Individual show Page
      "show-en": makeDustConfig("show", "en"),
      "show-ar": makeDustConfig("show", "ar"),

      // Topic Page
      "topic-en": makeDustConfig("topic", "en"),
      "topic-ar": makeDustConfig("topic", "ar"),

      // Topics Page
      "topics-en": makeDustConfig("topics", "en"),
      "topics-ar": makeDustConfig("topics", "ar"),

      // Error Page
      "error-en": makeDustConfig("error", "en"),
      "error-ar": makeDustConfig("error", "ar"),

      // Live Page
      "live-en": makeDustConfig("live", "en"),
      "live-ar": makeDustConfig("live", "ar"),

      // Events Page
      "events-en": makeDustConfig("events", "en"),
      "events-ar": makeDustConfig("events", "ar"),

      // EPG Page
      "epg-en": makeDustConfig("epg", "en"),
      "epg-ar": makeDustConfig("epg", "ar")
    },

    // Allow building of an entire page
    page: {
      options: {
        langs: ["en", "ar"]
      },
      home: true,
      about: true,
      channel: true,
      yourchannel: true,
      detail: true,
      allshows: true,
      show: true,
      topics: true,
      topic: true,
      search: true,
      live: true,
      events: true,
      epg: true,
      error: true
    },

    // Remove console logging statements
    groundskeeper: {
      glob_to_multiple: {
        expand: true,
        cwd: "dist/scripts",
        src: ["*.js"],
        dest: "dist/scripts"
      }
    },

    // Compress javascript
    uglify: {
      options: {
        preserveComments: "some",
        report: "min",
        banner: "/*! <%= pkg.name %> - <%= grunt.template.today('yyyy-mm-dd') %> */"
      },
      all: {
        files: [
          { expand: true, cwd: "dist/scripts", src: ["*.js"], dest: "dist/scripts" }
        ]
      }
    },

    // Compress CSS
    cssmin: {
      options: {
        keepSpecialComments: 1,
        report: "min"
      },
      all: {
        files: [
          { expand: true, cwd: "dist/styles", src: ["*.css"], dest: "dist/styles" }
        ]
      }
    },

    // Auto kick off builds when files change
    // Tasks are empty just to appease grunt. The heavy
    // lifting is done in the watch event handler.
    watch: {
      options: {
        nospawn: true,
        interrupt: true
      },
      styles: {
        files: ["src/**/*.scss"],
        tasks: [ "page:events", "page:detail" ]
      },
      scripts: {
        files: ["src/**/*.js", "src/**/*.html"],
        tasks: [ "page:events", "page:detail" ]
      },
      html: {
        files: ["src/**/*.dust", "data/**"],
        tasks: [ "page:events", "page:detail"]
      },
      config: {
        files: ["Gruntfile.js", "package.json"],
        tasks: ["jshint"]
      }
    },

    shell: {
      runTests: {
        command: "npm test",
        options: {
          stdout: true
        }
      }
    },

    concurrent: {
      options: {
        logConcurrentOutput: true
      },
      preprocess: ["sass", "dusthtml", "requirejs"],
      compress: ["uglify", "cssmin", "smushit"]
    },

    jshint: {
      all: {
        files: [
          { src: ["Gruntfile.js"] },
          { src: ["**/*.js", "!libs/**", "!**/templates.js", "!text.js"], expand: true, cwd: "src/scripts/" }
        ]
      },
      options: {
        jshintrc: ".jshintrc"
      }
    }
  });

  // Clear Dust's cache each time a build is run so we can actually
  // see changes made to partials.
  grunt.event.on("watch", function() {
    dust.cache = {};
  });

  /*
   * This is a helper task to build an individual page.
   *   grunt page:home
   */
  grunt.registerMultiTask("page", function() {
    var page = this.target;

    grunt.config.set("concurrent", {
      page: [
        "requirejs:" + page,
        "sass:" + page,
        "dusthtml:" + page + "-en",
        "dusthtml:" + page + "-ar"
      ]
    });

    grunt.task.run("concurrent:page");
  });

  grunt.loadNpmTasks("grunt-contrib-jshint");
  grunt.loadNpmTasks("grunt-contrib-copy");
  grunt.loadNpmTasks("grunt-contrib-sass");
  grunt.loadNpmTasks("grunt-contrib-watch");
  grunt.loadNpmTasks("grunt-contrib-clean");
  grunt.loadNpmTasks("grunt-contrib-uglify");
  grunt.loadNpmTasks("grunt-contrib-cssmin");
  grunt.loadNpmTasks("grunt-groundskeeper");
  grunt.loadNpmTasks("grunt-concurrent");
  grunt.loadNpmTasks("grunt-requirejs");
  grunt.loadNpmTasks("grunt-dust-html");
  grunt.loadNpmTasks("grunt-smushit");
  grunt.loadNpmTasks("grunt-shell");

  // Define tasks to run

  grunt.registerTask("default", [
    "jshint",
    "clean",
    "copy",
    "concurrent"
  ]);

  grunt.registerTask("release", [
    "jshint",
    "shell:runTests",
    "clean",
    "copy",
    "concurrent:preprocess",
    "groundskeeper",
    "concurrent:compress"
  ]);
};
