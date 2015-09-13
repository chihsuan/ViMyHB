(function(){
    $('.floater').height($('.fixed.menu').height());

    $('.sidebar').first()
      .sidebar('attach events', '.open.item')
    ;
    $('.open.item')
      .removeClass('disabled')
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

var data = JSON.parse(DataManager.getData());
        if(data.length) {
            data = groupByDate(data);

            var html = "";
            for(var k in data) {
                html += '<div class="ui basic day segment">' +
                    '<h2 class="ui header">' + data[k].date + '</h2>';

                for(var i in data[k].entry) {
                    var color = data[k].entry[i].important? "red" : "";
                    var title = data[k].entry[i].title;
                    var content = data[k].entry[i].content;

                    html += '<div class="ui basic entry segment">' +
                       '<div class="ui ' + color + ' title attached segment">' +
                        '<p>' + title + '</p></div>' +
                        '<div class="ui bottom attached content secondary segment" style="display:none;">' +
                        '<p>' + content + '</p></div></div>';

                }

                html += '</div>';
            }

            $('.main.segment').html(html);

            $('.title.segment').on('click', function(){
                var i = $('.title.segment').index(this);

                if(!$(this).attr('open')) {
                    $(this).attr('open', true);    
                    $('.content.segment').eq(i).slideDown();
                }

                else {
                    $(this).attr('open', false);
                    $('.content.segment').eq(i).slideUp();
                }
                
            })
        }
        else {
            var html = '<div class="inner-floater"></div>' + 
                '<h2 class="ui center aligned icon header"><i class="meh icon"></i>在資料區間內沒有任何的推播數據</h2>';
            $('.main.segment').html(html);
            $('.inner-floater').height($(window).height()/4);
        }


    function groupByDate(data) {
        var result = [];
        var added = [];
        var day = null;

        for(var k in data) {
            if(added.indexOf(data[k].date) != -1){
                day.entry.push(data[k]);  
            } 
            else {
                if(day)
                    result.push(day);
                day = {date:data[k].date, entry:[]};
                added.push(data[k].date);
                day.entry.push(data[k]);
            }
        }
        result.push(day);

        return result;
    }

    // $('.title.segment').on('click', function(){
    //     $('.content.segment').slideDown();
    // })
})()
