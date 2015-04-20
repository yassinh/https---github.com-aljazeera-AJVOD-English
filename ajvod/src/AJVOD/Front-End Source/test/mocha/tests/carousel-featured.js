define(["views/common/featuredcarousel"], function(FeaturedCarouselView) {

  describe("Featured Carousel", function() {
    afterEach(destroy);

    /*
     * Auto scroll
     */

    describe("Auto scrolling w/ stop on manual slide", function() {
      beforeEach(create(FeaturedCarouselView, {
        autoScroll: true,
        autoScrollSpeed: 250,
        stopAutoScrollOnClick: true
      }));

      describe("The carousel", function() {
        it("should have an instance of the jcarouselAutoScroll plugin created on it", function(done) {
          expect(getAutoScrollObj()).to.be.an("object");
          setTimeout(done, view.options.fadeSlidesInTimeout);
        });

        it("should auto advance", function(done) {
          view.on("scrollend", function() {
            expect("everything").to.be.ok;
            setTimeout(done, view.options.fadeSlidesInTimeout);
          });
        });

        it("should destroy jcarouselAutoscroll when prev/next is clicked on", function(done) {
          getNext().trigger("click");
          expect(getAutoScrollObj()).to.be.an("undefined");
          setTimeout(done, view.options.fadeSlidesInTimeout);
        });
      });
    });
  });

});
