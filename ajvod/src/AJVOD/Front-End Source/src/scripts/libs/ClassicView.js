function genClassicView(Top)
{
    $('body').append('<div class=" ToggleControl"><div class="BetaWord">BETA</div></div>');
    scrollHandler(Top);
}

function scrollHandler(Top) {


    var $el = $('.ToggleControl');
    var $window = $(window);
    
    $window.bind("scroll resize", function () {
            $el.css({
                top: Top + 'px',
                bottom: "auto",
                position: "fixed"
            });
    }).scroll();
}