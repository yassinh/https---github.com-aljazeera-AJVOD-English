define(["views/base/carousel"], function(CarouselView) {

  describe("Basic Carousel", function(){
    afterEach(destroy);

    /*
     * Events
     */

    describe("Events", function() {
      beforeEach(create(CarouselView));

      it("should fire a scroll event", function(done) {
        view.once("scroll", function() {
          expect(true).to.be.true;
          done();
        }).next();
      });

      it("should fire a scrollend event", function(done) {
        view.once("scrollend", function() {
          expect(true).to.be.true;
          done();
        }).next();
      });
    });

    /*
     * Methods
     */

    describe("Methods", function() {
      beforeEach(create(CarouselView));

      // next
      describe("next", function() {
        it("should move to the next slide", function(done) {
          expect(view.getTarget().index()).to.equal(0);

          view.once("scrollend", function() {
            expect(view.getTarget().index()).to.equal(1);
            done();
          }).next();
        });
      });

      // prev
      describe("prev", function() {
        it("should move to the previous slide", function(done) {
          view.once("scrollend", function() {
            expect(view.getTarget().index()).to.equal(1);

            view.once("scrollend", function() {
              expect(view.getTarget().index()).to.equal(0);
              done();
            }).prev();
          }).next();
        });
      });
    });

    /*
     * Next/prev links
     */

    describe("Next/Previous Buttons", function() {
      beforeEach(create(CarouselView));

      describe("next", function() {
        it("should advance the carousel when clicked", function(done) {
          view.on("scrollend", function() {
            expect("everything").to.be.ok;
            done();
          });

          getNext().trigger("click");
        });

        it("should be disabled once at the end of the carousel", function(done) {
          view.on("scrollend", function() {
            expect(getPrev().hasClass("is-disabled")).to.be.false;
            expect(getNext().hasClass("is-disabled")).to.be.true;
            done();
          });

          var carousel = view.getCarousel();
          carousel.scroll(carousel.items().last()[0]);
        });
      });

      describe("prev", function() {
        it("should be disabled when the carousel is created", function(done) {
          expect(getPrev().hasClass("is-disabled")).to.be.true;
          expect(getNext().hasClass("is-disabled")).to.be.false;
          done();
        });
      });

    });

    /*
     * Pagination
     */

    describe("Pagination", function() {
      beforeEach(create(CarouselView, {
        pagination: true,
        wrap: "circular",
        animation: 50
      }));

      function pages() {
        return view.$el.find("footer ul").children();
      }

      it("should have list items in the footer element", function(done) {
        expect(pages()).to.have.length.above(0);
        done();
      });

      it("should automatically select the first page as active", function(done) {
        expect(pages().first().hasClass("is-active")).to.be.true;
        done();
      });

      it("should move the carousel when choosing a pagingation circle", function(done) {
        var index = 1;

        view.once("animateend", function() {
          expect(view.getTarget().index()).to.equal(index);
          done();
        });

        pages().eq(index).trigger("click");
      });

      /*
      it("should loop around back to the first page in circular mode", function(done) {
        var i = 1;

        view.on("animateend", function() {
          // Keep moving until we've gone through each slide in the
          // carousel and are back on the first
          if(i++ < 3) {
              view.next();
          } else {
            expect(pages().first().hasClass("is-active")).to.be.true;
            done();
          }
        });

        // kick off
        view.next();
      });
      */
    });

    /*
     * Lazy loading images
     */

    describe("Lazy loading images", function() {
      beforeEach(create(CarouselView, {
        numItems: 1,
        lazyLoadImages: true
      }));

      describe("First item in the carousel", function() {
        it("should have its src attribute set to the real image when the carousel is created", function(done) {
          var img = view.$("li:first img");
          expect(img.attr("src")).to.equal(img.data("src"));
          done();
        });
      });

      describe("Second item in the carousel", function() {
        it("should not have its src attribute set when the carousel is created", function(done) {
          var img = view.$("li:eq(1) img");
          expect(img.attr("src")).to.be.an("undefined");
          done();
        });

        it("should have its src attribute set when next() is called", function(done) {
          view.next();
          var img = view.$("li:eq(1) img");
          expect(img.attr("src")).to.equal(img.data("src"));
          done();
        });
      });
    });


    /*
     * Select boxes
     */

    describe("Select menus", function() {
      beforeEach(create(CarouselView));

      describe("The carousel", function() {
        it("should have a select menu child view", function(done) {
          expect(view.views.selectmenu).to.be.an("object");
          done();
        });
      });

      describe("The select menu view", function() {
        it("should have an $el property equal to a select box element", function(done) {
          expect(view.views.selectmenu.$el.is("select")).to.be.true;
          done();
        });
      });
    });



  });
});
