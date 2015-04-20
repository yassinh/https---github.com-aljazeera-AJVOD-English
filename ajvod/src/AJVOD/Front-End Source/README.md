# Al Jazeera Front-end

The ALJ front-end code base exists separately from the back-end code base. The high-level integration workflow
is that the front-end is created using stub or test data, a build tool produces production-ready
static assets (HTML, CSS, JavaScript, and images) which are then integrated into the back-end
JSP templates by hand.

## Set up

To set up the front-end for development:

1. Ensure node.js is installed. If you're on OSX use Homebrew: `brew install nodejs`.
2. Clone this repository and `cd` into the `Al-Jazeera-VOD-Front-End` directory.
3. Install local dependencies: `npm install`.
4. Run `grunt` to kick off a build
5. Point your browser to a static HTML file in the `dist/` directory.

#### Troubleshooting

* If you get an error that says `grunt: command not found` run the command `which grunt`. If it doesn't find anything try running `npm install -g grunt-cli`.
* If `npm install` throws an error, make sure you are `cd`'d into the `Al-Jazeera-VOD-Front-End` directory.
* The Bourbon CSS library is packaged in with this repo. You should get it simply by cloning the repo, but if you run into problems, try installing it manually: `gem install bourbon`

## Directory structure

* `src/` - Location of the front-end source code
* `src/scripts/` - Location of all the JavaScript files
* `src/scripts/config.js` - Require.js configuration file
* `src/scripts/app/app.js` - The main application file where many configuration options are stored, and which facilitates
functionality across the rest of the code base. This app.js file is a dependency for every other Require.js module.
* `src/scripts/app/page-*.js` - Includes the dependencies for that page and boots it
* `src/scripts/app/views/` - View code
* `src/scripts/app/views/common/` - Common views that are not specific to a particular page, like a carousel or pagination.
* `src/scripts/app/collections/` - Backbone collections
* `src/scripts/app/models/` - Backbone models
* `src/scripts/templates/` - Templates that the JavaScript is responsible for rendering
* `src/scripts/libs/` - Third party JavaScript libraries
* `dist/` - Statically-built version of the front-end
* `data/` - Where stub `.json` files are stored for rendering the front-end Dust templates
* `test/` - Unit tests
* `node_modules` - Where `npm` installed all the dependencies
* `./Gruntfile.js` - The build file
* `./package.json` - The configuration file that defines all build/server dependencies
* `./jshintrc` - Configuration options for JSHint

## Libraries & Frameworks Used

The front-end is build on top of the following libaries and frameworks:

**Dust Templates**
All HTML is written inside of Dust templates, which allows for functionality like partials, variables, dynamic rendering, and more.

**jQuery**
Used to facilitate cross-browser DOM and Ajax interactions.

**jQuery UI**
Provides the foundation for many widgets and interactions throughout the portal, like dialogs, drag and drop, etc.

**Underscore.js**
Underscore provides a number of utility JavaScript functions, and is a required dependency of Backbone.js.

**Backbone.js**
Backbone is used to provide an inheritance structure to the application. Most everything you see in the portal is a Backbone view that inherits functionality from other views.
For example, within the `src/scripts/views/common` directory exists a single view named "page" which is the base view from which all other pages extend. The home page, detail page,
etc. inherit functionality from the page page view. This architecture allows you to place functionality common to all pages in a single view, and override/customize the functionality
later on a per-page basis.

**Require.js**
Require is the JavaScript dependency management system. It allows (for example) the detail page to define a list of dependency components: a carousel, navigation, etc.

**Grunt.js**
Grunt is responsible for building the application: compiling SASS files into stylesheets, resolving the Require.js dependencies, minifying the JavaScript, etc.

**SASS**
The CSS is written using the Scss variant of SASS, which allows the CSS to be architected in a reusable and maintainble way.

**Bourbon**
Bourbon is a library written in SASS that provides a number of utilties for making CSS easy to write and less verbose.

## Development Process

1. Add, edit, or delete CSS (Sass files), HTML (Dust files), etc. in the `src/` files.
2. Re-build the page where you've made the change and want to re-test. For example, run `grunt page:detail` to re-build the video detail page, or run `grunt page` to re-build all pages.
3. Point your browser to the `dist/` directory and look at one of the HTML files.

If an image is changed or added, you must run the `grunt copy` to copy the image into the `dist` directory.

### Creating a release build

Once development of a bug/feature is done you need to sync the static files to GitHub so that the back-end engineers can pull in the changes. This is done by creating a release build and then checking in the `dist/` folder. See the section below for what exactly a release build does.

1. Checkout the release branch for the current epic. For example, `git checkout release-epic-8`
1. Run the command `grunt release`
2. Check in the files outputted into the `dist/` folder. Since `dist` is in the .gitignore file, check in with the `--force` flag: `git add --force dist/`

The only time you should check in the `dist/` directory is after running `grunt release` - do not check in at any other time unless you enjoy resolving merge conflicts.

### Building an individual page

If you only want to build one specific page, you can do so by running this command:

```js
grunt page:detail
```

This example will only build the CSS, HTML, and JavaScript for the detail page. Available pages for building are: `home`, `detail`, `channel`, `yourchannel`, `allshows`, `show`, `topics`, `topic`, `live`, `error`, and `search`.

### What the build process is doing

When you run the command `grunt` the following happens:

1. Runs all JavaScript files in the `src/` directory against JSHint to ensure code quality and consistency.  If JSHint detects an error then the build will fail.
2. Runs the require.js build tool against all files in the `src/scripts` directory, and outputs the results into the `dist/scripts` directory. Each page in the portal will have a single .js file with all the relevant modules.
3. Processes all the Sass files in `src/styles` and outputs generated CSS files to `dist/styles`.
4. Renders all the Dust templates in `src/templates` against the data files in `data/`. The result is static HTML files which are placed in the `dist/` directory.
5. Injects a build number into the JavaScript files based upon what is defined in `package.json`.
6. (RELEASE ONLY) runs any images through an image optimizer.
7. (RELEASE ONLY) minifies the Javascript and CSS.
8. (RELEASE ONLY) removes all comments from the Javascripts.
9. (RELEASE ONLY) runs the unit tests.

## Unit Tests

Tests are written using the [http://visionmedia.github.com/mocha/](Mocha) framework with [http://chaijs.com/](Chai) for assertions. Run the tests by opening up `/tests/mocha/runner.html` in your browser, or run them on the command line via the `npm test` command.

## Third-party Service Interactions

### Taboola

Taboola powers the recommended videos hero unit on the Your Channel page. All interaction with the Taboola API exists inside the `app/scripts/collections/common/taboola.js` file.
The API key is defined inside the `app/scripts/app.js` file.

In order to support recommendations, a heartbeat call is sent to Taboola with current session data and the video ID each time a video begins to play on the detail page. The code that performs the API call is located in the `src/scripts/app/views/detail/detail.js` view.

### Gigya

###### Social login
Available on every page through the "Create an Account" and "Login" links in the header. Users can initate social login at any time
by using these links, but trying to perform various actions (like subscribe, playlist creation, etc.) will prompt the user to login.
The view that powers this integration is loged at `src/scripts/app/views/common/gigya.js`.

###### Social login - linked networks
Authenticated users see a "social networks" link inside the Preferences pane.

###### Comments
The comments panel on the detail page is powered through Gigya.

###### Configuration
The screensets Gigya is capable of rendering are defined inside of the `src/scripts/app/app.js` file as constants.
Each page contains the Gigya embed snippet before the closing `</body>` tag, which is where the API key, site name,
enabled social login providers, and other configuration options are set.

## VOD API Interactions

The front-end makes a number of API calls to the back-end to support various pieces of functionality. These API calls are:

##### User Authentication
`/api/login` - Creates a new user session in the back-end when the user authenticates with Gigya. This API method is not called unless the login to Gigya was successful.
`/api/logout` - Destroys the back-end user session when the user logs out of Gigya.

##### Preferences
`/api/preferences/get` - Retrieves a user's saved preferences, as well as the data required for rendering the select boxes inside the preferences pane (timezones, countries, etc.). If the user is not currently logged into Gigya, this method will return the default user preferences.
`/api/preferences/set` - Set user preferences.

##### Playlists
`/api/playlists/create` - Create a new playlist. If a video ID is passed to this method, the video will be added to the newly created playlist.
`/api/playlists/get` - Get all playlists created by the authenticated user.
`/api/playlists/add` - Add a video to a playlist.
`/api/playlists/remove` - Remove a video from a playlist.
`/api/playlists/delete` - Delete a playlist.
`/api/playlists/rename` - Rename a playlist.
`/api/playlists/reorder` - Change the order in which playlists appear on the "Your Channel" page.

##### Subscriptions
`/api/subscriptions/get` - Get the users subscriptions.
`/api/subscriptions/add` - Subscribe to a new query.
`/api/subscriptions/remove` - Remove a subscription.
`/api/subscriptions/order` - Change subscription order.

##### Auto Resume
`/api/positions/get` - Get the last watched position for a specific video.
`/api/positions/set` - Set the last watched position for a specific video.
`/api/positions/remove` - Remove the last watched position for a specific video.

##### Events
`/api/events/timeline` - Get all videos associated with a particular event.

##### Media Query
`/api/mediaquery` - Query the Brightcove media API. This method is a simple proxy to Brightcove, and its purpose is to hide Al Jazeera's read API key.
