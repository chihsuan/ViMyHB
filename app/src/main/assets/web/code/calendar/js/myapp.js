(function($) {

	"use strict";
    $('.floater').height($('.fixed.menu').height());

    $('.sidebar').first()
      .sidebar('attach events', '.open.item')
    ;
	
	$('.layer_content.item').hide();

    $('.layer_title.item').on('click', function(){
        var cate = $(this).attr('cate');

        if(!parseInt($(this).attr('status'))) {
            $('.layer_content.item[cate="'+cate+'"]').slideDown();
            $(this).attr('status', '1');
        }
        else {
            $('.layer_content.item[cate="'+cate+'"]').slideUp();
            $(this).attr('status', '0');
        }

    })
	
    function convertTime(time) {
        var s = time.split('/');
        var year = parseInt(s[0])+1911;
        s.splice(0, 1, year);
        return s.join('-');
    }

    var tmpl_path = 'tmpls/';
    var ww = $(window).width(), wh = $(window).height();

if($(window).width() < $(window).height() || $(window).width()<768)
    tmpl_path = 'mobile_tmpls/';

var tmpl_names = ['year', 'month', 'events-list', 'month-day', 'year-month'];
var tmpl_string = {};

loadtmpls(0);

function loadtmpls(index) {
    var path = tmpl_path+tmpl_names[index]+'.html';
var data = DataManager.getTemplate(path);
        tmpl_string[tmpl_names[index]] = data;

        if(tmpl_names[index+1] != undefined) {
            loadtmpls(index+1);
        }
        else {
            calendar_init();
        }

};

function calendar_init() {
var data = JSON.parse(DataManager.getData());

        for(var k in data) {
            // console.log(data[k]);
            data[k].class = 'd-' + data[k].id;
            data[k].id = k;
            data[k].start = new Date(convertTime(data[k].time)).getTime();
            data[k].end = new Date(convertTime(data[k].time)).getTime();
            data[k].time = convertTime(data[k].time);
        }

        var options = {
            events_source: data,
            view: 'year',
            views: {
                 year:  {
                   enable: 1
                 },
                 month:  {
                   enable: 1 
                 },
                 week:  {
                   enable: 0
                 },
                 day:   {
                   enable: 0 //disabled
                 }
            },
            tmpl_path: tmpl_path,
            tmpl_string: tmpl_string,
            tmpl_cache: false,
            day: data.length? data[data.length-1].time:'2015-01-01',
            language: 'zh-CN',
            onAfterEventsLoad: function(events) {
                if(!events) {
                    return;
                }
            },
            onAfterViewLoad: function(view) {

                if($(window).width() < $(window).height() || $(window).width()<768) {
                    $('.cal-events-num').css({"margin-left":"40px"});
                }

                $('.page-header').text(this.getTitle());
                $('.control.button').removeClass('active');
                $('.button[data-calendar-view="' + view + '"]').addClass('active');
            },
            classes: {
                months: {
                    general: 'label'
                }
            }
        };

        var calendar = $('#calendar').calendar(options);

        $('.control.button[data-calendar-nav]').each(function() {
            var $this = $(this);
            $this.click(function() {
                calendar.navigate($this.data('calendar-nav'));
            });
        });

        $('.control.button[data-calendar-view]').each(function() {
            var $this = $(this);
            $this.click(function() {
                calendar.view($this.data('calendar-view'));
            });
        });





}

$('.top.button').on('click', function(){
    $('body,html').animate({
        scrollTop: 0 ,
    }, 700);
})

$(window).resize(function() {
    if($(window).width() != ww && $(window).height() != wh){
        if(tmpl_path == 'mobile_tmpls/') {
            if($(window).width() > $(window).height() && $(window).width() > 768)
                location.reload();
        }
        else if(tmpl_path == 'tmpls/') {
            if($(window).width() < $(window).height() || $(window).width() < 768)
                    location.reload();
        }
    }
    // location.reload();
})

}(jQuery));
