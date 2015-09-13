(function($) {

	"use strict";

	var options = {
		events_source: 'events.json.php',
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
		tmpl_path: 'tmpls/',
		tmpl_cache: false,
		day: '2013-03-12',
        language: 'zh-CN',
		onAfterEventsLoad: function(events) {
			if(!events) {
				return;
			}
		},
		onAfterViewLoad: function(view) {
			$('.page-header h3').text(this.getTitle());
			$('.btn-group button').removeClass('active');
			$('button[data-calendar-view="' + view + '"]').addClass('active');
		},
		classes: {
			months: {
				general: 'label'
			}
		}
	};

	var calendar = $('#calendar').calendar(options);

	$('.btn-group button[data-calendar-nav]').each(function() {
		var $this = $(this);
		$this.click(function() {
			calendar.navigate($this.data('calendar-nav'));
		});
	});

	$('.btn-group button[data-calendar-view]').each(function() {
		var $this = $(this);
		$this.click(function() {
			calendar.view($this.data('calendar-view'));
		});
	});

}(jQuery));
