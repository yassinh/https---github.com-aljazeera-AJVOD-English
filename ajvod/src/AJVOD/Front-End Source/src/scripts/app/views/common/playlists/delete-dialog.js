// Delete playlist dialog
//
// This is basically the same thing as the playlists remove dialog, but
// with a different template and it emits a different event.

define([
 "app",
 "views/common/playlists/dialog-remove",
 "text!templates/playlists/dialog-delete.html"
], function(app, RemoveDialogView, tmplDelete) {
  "use strict";

  return RemoveDialogView.extend({
    id: "playlists-delete-dialog",

    template: _.template(tmplDelete),

    _onContinueClick: function(event) {
      event.preventDefault();
      console.log('Emitting playlist delete', this.options);
      app.trigger("playlists:delete", {
        playlistId: this.options.playlistId,
        title: this.options.title
      });
    }
  });
});

