define(["app"], function(app) {
  "use strict";
  
  return Backbone.Collection.extend({
    url: function() {
      var letter = this.letter === "all" ? "" : this.letter;
      return "/topics-index/" + (letter ? (letter + "/") : "");
    }
  });

});
