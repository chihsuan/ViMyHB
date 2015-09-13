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
        if(data != null) {
            var table = {
              "medical_credit_sum": '總健保支付點數',
              "times_per_year": '年就醫次數'
            }

            var html = '';
            for(var k in data) {
                 html += (k == 'medical_credit_sum') ? '<div class="side active">' : '<div class="side">';
                 html += '<div class="content">';
                 html += '<div class="center">';

                 html += '<div class="ui statistic">';
                 html += '<div class="value" style="color: red;">';
                 html += data[k];
                 html += '</div>';
                 html += '<div class="label" style="font-size: 0.7em;">';
                 html += table[k];
                 html += '</div></div>';

                 html += '</div></div></div>';
            }

            $('.sides').html(html);
            $('.shape').shape();

            $('.ui.card').click(function() {
                $('.shape').shape('flip right');
            });
        }
        else {
            var html = '<div class="inner-floater"></div>' + 
                '<h2 class="ui center aligned icon header"><i class="meh icon"></i>在資料區間內沒有任何的數據</h2>';
            $('.main.segment').html(html);
            $('.inner-floater').height($(window).height()/4);
        }


    /*function groupByDate(data) {
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
    }*/

    // $('.title.segment').on('click', function(){
    //     $('.content.segment').slideDown();
    // })
})()
