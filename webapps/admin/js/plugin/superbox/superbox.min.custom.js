/*! SmartAdmin - v1.4.0 - 2014-06-04 */
!function (a) {
    a.fn.SuperBox = function () {
        var b = a('<div class="superbox-show"></div>'),
            c = a('<img src="" class="superbox-current-img"><div id="imgInfoBox" class="superbox-imageinfo inline-block"> <h1>Image Title</h1><span><p><em>http://imagelink.com/thisimage.jpg</em></p><p class="superbox-img-description">Image description</p><p><a href="javascript:void(0);" class="btn btn-primary btn-sm edit-image">Crop Image</a></span> </div>'),
            d = a('<div class="superbox-close txt-color-white"><i class="fa fa-times fa-lg"></i></div>');
        b.append(c).append(d);
        a(".superbox-imageinfo");

        var basicInfoFromUrl = function(url) {
            var items = url.split('/') ;
            return {
                domainid: items[3],
                catid: items[4],
                galid: items[5]
            };
        }

        return this.each(function () {
            a(".superbox-list").click(function () {
                $this = a(this);
                var d = $this.find(".superbox-img"), e = d.data("img"), f = d.attr("alt") || "", g = e, h = d.attr("title") || "No Title";
                var imgEl = d[0] ;

                var basicData = basicInfoFromUrl(e) ;

                // crop modal by airkjh. cropHandler defined in gallery.html
                c.attr("src", e), a(".superbox-list").removeClass("active"), $this.addClass("active"), c.find("em").text(g), c.find(">:first-child").text(h), c.find(".superbox-img-description").text(f), 0 == a(".superbox-current-img").css("opacity") && a(".superbox-current-img").animate({opacity: 1}), a(this).next().hasClass("superbox-show") ? (a(".superbox-list").removeClass("active"), b.toggle()) : (b.insertAfter(this).css("display", "block"), $this.addClass("active")), a("html, body").animate({scrollTop: b.position().top - d.width()}, "medium");
                c.find('a.edit-image').click({basic: basicData, src: e, originalWidth: c.naturalWidth, originalHeight: c.naturalHeight}, cropHandler);

            }), a(".superbox").on("click", ".superbox-close", function () {
                a(".superbox-list").removeClass("active"), a(".superbox-current-img").animate({opacity: 0}, 200, function () {
                    a(".superbox-show").slideUp()
                })
            })
        })
    }
}(jQuery);