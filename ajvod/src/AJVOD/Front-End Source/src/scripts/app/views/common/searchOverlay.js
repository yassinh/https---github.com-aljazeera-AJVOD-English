define([
  "app",
  "views/base/menu",
  "views/common/selectmenu",
  "collections/search",
  "text!templates/searchoverlay/results.html"
], function(app, MenuView, SelectMenuView, SearchCollection, tmplResults) {
  "use strict";

  // el: form element
  return MenuView.extend({

    tmplResults: _.template(tmplResults),

    events: _.extend({}, MenuView.prototype.events, {
      "submit": "submit",
      "keyup input[type='text']": "submitOnKeyEvent",
      "keypress input[type='text']": "clearError",
      "click .input-container a": "submitOnMouseClick"
    }),

    _tooltipOptions: _.extend({}, MenuView.prototype._tooltipOptions, {
      tooltipClass: "tooltip-search-overlay"
    }),

    currentTerm: null,

    initialize: function() {
      MenuView.prototype.initialize.call(this);

      this.views.selectmenu = new SelectMenuView({
        el: this.$("select"),
        insideOverlay: true
      });

      this.collection = new SearchCollection([], {
        url: this.$("form").data("ajax-action")
      });

      this.$select = this.$("select");
      this.$input = this.$('input[type="text"]');
      this.$target = this.$(".target");
    },

    _bindEvents: function() {
      MenuView.prototype._bindEvents.apply(this, arguments);
      this.listenTo(app, "api:openSearchPanel", this.open);
    },

    open: function() {
      MenuView.prototype.open.apply(this, arguments);
      this.reset();
      this.$input.focus();
    },

    // submit event on the form
    submit: function(event) {
      if($.trim(this.$input.val()).length === 0) {
        this.$input.addClass("is-error");
        event.preventDefault();
      }
    },

    // click on the magnifying glass
    submitOnMouseClick: function(event) {
      event.preventDefault();
      this.$("form").submit();
    },

    submitOnKeyEvent: _.debounce(function(event) {
      // ignore submits here
      if(event.which === 13) {
        return;
      }

      var value = $.trim(event.currentTarget.value);

      if(!value.length) {
        this.reset();
      } else if(value.length >= 3) {
        this.fetch();
      }
    }, 500),

    reset: function() {
      this.$target.empty();
      this.$input.val("");
      this.currentTerm = null;
      this.clearError();
    },

    clearError: function() {
      this.$input.removeClass("is-error");
    },

    fetch: function(event) {
      var $input = this.$input;
      this.clearError();

      // bust the cache if the select box caused this to fire
      if(event && event.type === "change") {
        this.collection.clearCache();
      }

      var value = $.trim($input.val());

      // exit early if the terms are the same
      if(this.currentTerm === value) {
        return;
      }

      // abort any ongoing requests
      if(this.dfd) {
        this.dfd.abort();
      }

      // if the search box is empty just clear and GTFO
      if(!value.length) {
        this.reset();
        return;
      }

      // Show spinner
      $input.addClass("has-spinner");

      // remember value
      this.currentTerm = value;

      this.dfd = this.collection.fetch({
        data: this.$("form").serialize()
      }).done(_.bind(function(resp) {
        var context = $.extend({}, { localization: app.localization }, resp);
        this.$target.html(this.tmplResults(context));
        this._initItemTooltips();
      }, this)).always(function() {
        $input.removeClass("has-spinner");
      });
    }
  });

});
