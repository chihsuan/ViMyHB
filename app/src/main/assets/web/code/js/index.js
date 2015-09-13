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

    $('.dimmable.image').dimmer({
      on: 'hover'
    });
})();


