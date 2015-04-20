// Unsubscribe dialog
//
// This is basically the same thing as the playlists remove dialog, but
// with a different template and it emits a different event.

define([
 "app",
 "views/common/playlists/dialog-remove",
 "text!templates/subscriptions/dialog-remove.html"
], function(app, RemoveDialogView, tmplRemove) {
  "use strict";

  return RemoveDialogView.extend({
    id: "unsubscribe-dialog",

    template: _.template(tmplRemove),

    _onContinueClick: function(event) {
      event.preventDefault();
      app.trigger("subscriptions:remove", this.options);
    }
  });
});
