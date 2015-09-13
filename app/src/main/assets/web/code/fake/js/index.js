(function() {
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
	
  var data, svg, zoom, xscale, margin, width, height, color, cur, maxmin;
  var hospitals = [];
  var dataTimeFormat = d3.time.format("%Y%m%d");
  // var outputFormat = d3.time.format("%Y %b %d");
  var outputFormat = function(time) {
    var y = time.getYear()+1900;
    var m = time.getMonth()+1;
    var d = time.getDate();
    return y+'年'+m+'月'+d+'日';
  };

  var yearColor = d3.scale.category10();

  var zoomInitScale = 7;
  var currentScale = zoomInitScale;

  var itemHeight = 40;
  var interval = 5;
  var show = [];

var source = JSON.parse(DataManager.getData());
    if(source.length) {
        var people = Object.keys(source);
        data = source;
        preprocess();
        initPlot();
    }
    else {
        var html = '<div class="inner-floater"></div>' + 
            '<h2 class="ui center aligned icon header"><i class="meh icon"></i>在資料區間內沒有任何的領藥記錄</h2>';
        $('.main.segment').html(html);
        $('.inner-floater').height($(window).height()/4);
    }



  function initPlot(){
    margin = {top: 5, right: -5, bottom: -5, left: -5};
    width = $('.main.segment').width() - margin.left - margin.right;
    height = (itemHeight+interval)*hospitals.length - margin.top - margin.bottom + 56;

    zoom = d3.behavior.zoom()
      .scaleExtent([1, 10])
      .scale(zoomInitScale)
      .on("zoom", zoomed);

    var minDate = d3.min(data, function(d) { return d.startDate; });
    var maxDate = d3.max(data, function(d) { return d.endDate; });
    maxmin = (maxDate - minDate)/(60 * 60 * 24 * 1000);

    var rawMax = maxDate;
    var rawMin = minDate;

    var infotext = 'From&nbsp;&nbsp;&nbsp;&nbsp;<text style="color:' + yearColor(minDate.getYear())
      + ';">' + outputFormat(minDate) +
      '</text>&nbsp;&nbsp;&nbsp;&nbsp;to&nbsp;&nbsp;&nbsp;&nbsp;<text style="color:' + yearColor(maxDate.getYear())
      + ';">' + outputFormat(maxDate)+'</text>';

    $('.timeinfo.header').html(infotext);

    color = d3.scale.ordinal()
      .range(["#76CF7F", "#2C8283", "#56D1D8", "#FDE859", "#FF7F5C", "#FEC65A", "#9BA49C"]);

    xscale = d3.time.scale()
      .domain([minDate, maxDate])
      .range([0, width]);

    var xAxis = d3.svg.axis()
      .scale(xscale)
      .orient('bottom')
      .ticks(d3.time.months)
      .tickFormat(function(d, i){
        return  (d.getMonth()+1)+'月';
      })
      .tickSize(16, 0);

    svg = d3.select(".plot").append("svg")
      .attr("width", width + margin.left + margin.right)
      .attr("height", height + margin.top + margin.bottom -40)
    .append("g")
      .attr("transform", "translate(" + margin.left + "," + margin.right + ")")
      .call(zoom)
      // .on("dblclick.zoom", null)

    var xa = svg.append('g')
      .attr('class', 'x axis')
      .attr('transform', 'translate(0, ' + (margin.top) + ')')
      .call(xAxis)

    xa.selectAll(".tick text")
      .style("text-anchor", "start")
      .attr("x", 6)
      .attr("y", 6)
      .style('fill', function(d){
        return yearColor(d.getYear());
      });

    svg.append('rect')
      .attr('class', 'zoom')
      .attr('x', 0)
      .attr('y', 0)
      .attr('width', width)
      .attr('height', height)
      .attr('opacity', 0)

    svg.selectAll('.display rect')
      .data(data)
      .enter()
      .append('rect')
      .attr('class', 'display')
      .on('mouseover', function(d){
        cur = d;
      })
      // .attr('fill', function(d){ return color(d[8]); })
      .attr('fill', function(d){
        // if(d[9] == 'f'){
        //   // var t = textures.lines()
        //   // .orientation("3/8", "7/8")
        //   // .stroke("red")
        //   // .background(color(d[8]));

        //   var t = textures.circles()
        //     .radius(4)
        //     .size(10)
        //     .fill('red')
        //     .background(color(d[8]));

        //   svg.call(t);
        //   return t.url();
        // }
        // else
          return color(d.hospital)
      })
      .on('click', function(d){
        var coordinates = [0, 0];
        coordinates = d3.mouse(this);

        $('.cards').html('');

        svg.selectAll('.display')
          .each(function(d, i){
            var iy = parseFloat(d3.select(this).attr('y'));
            var ix = parseFloat(d3.select(this).attr('x'));
            var iw = parseFloat(d3.select(this).attr('width'));

            if(Math.abs(iy-coordinates[1])<itemHeight && coordinates[1]>iy){
              // console.log(ix + iw);
              if(ix <= coordinates[0] && coordinates[0] <= (ix+iw))
                updateCards(d, i);
            }
          })

        var ctop = $('.cards').offset().bottom;
        $('body,html').animate({
          scrollTop: $(document).height()-$(window).height() ,
        }, 700);

        $('.top.button').show();
      })

    $('.top.button').on('click', function(){
      $('body,html').animate({
        scrollTop: 0 ,
      }, 700);
    })

    $('.zoom.button').on('click', function(){
      var z = parseInt($(this).attr('zoom'));
      var extent = zoom.scaleExtent();
      if(!((currentScale+z) < extent[0]||(currentScale+z) > extent[1]))
        currentScale += z;

      // console.log(currentScale);
      var reverse = d3.time.scale()
        .domain([0, width])
        .range([minDate, maxDate]);

      var coordinates = [width/2, 0];
      var middate = new Date(reverse(coordinates[0]));
      var half = Math.round(maxmin/currentScale/2);

      max = new Date(middate);
      max.setDate(middate.getDate()+half);
      maxDate = max;
      min = new Date(middate);
      min.setDate(middate.getDate()-half);
      minDate = min;

      if(maxDate.getTime() >= rawMax.getTime())
        maxDate = rawMax;
      else if(minDate.getTime() <= rawMin.getTime())
        minDate = rawMin;

      if(currentScale==1 && lastLevel==1){
        maxDate = rawMax;
        minDate = rawMin;
      }

      if ( isNaN( maxDate.getTime() ) ) {
        maxDate = rawMax;
      }
      if ( isNaN( minDate.getTime() ) ) {
        minDate = rawMin;
      }

      xscale.domain([minDate, maxDate]);

      var diff = Math.round((maxDate-minDate)/(60 * 60 * 24 * 1000));
      if(diff<50){
        xAxis.ticks(d3.time.sunday)
          .tickFormat(d3.time.format('%m/%d'))
      }
      else{
        xAxis.ticks(d3.time.months)
          .tickFormat(function(d, i){
            return  (d.getMonth()+1)+'月';
          })
      }

      xa.call(xAxis);

      xa.selectAll(".tick text")
        .style("text-anchor", "start")
        .attr("x", 6)
        .attr("y", 6)
        .style('fill', function(d){
          return yearColor(d.getYear());
        });

      var infotext = 'From&nbsp;&nbsp;&nbsp;&nbsp;<text style="color:' + yearColor(minDate.getYear())
        + ';">' + outputFormat(minDate) +
        '</text>&nbsp;&nbsp;&nbsp;&nbsp;to&nbsp;&nbsp;&nbsp;&nbsp;<text style="color:' + yearColor(maxDate.getYear())
        + ';">' + outputFormat(maxDate)+'</text>';

      $('.timeinfo.header').html(infotext);

      updatePlot();
    })

    zoomInit(zoomInitScale);

    function zoomInit(sc) {
      var reverse = d3.time.scale()
        .domain([0, width])
        .range([minDate, maxDate]);

      var coordinates = [width, 0];
      var middate = new Date(reverse(coordinates[0]));
      var half = Math.round(maxmin/sc/2);

      max = new Date(middate);
      max.setDate(middate.getDate()+half);
      maxDate = max;
      min = new Date(middate);
      min.setDate(middate.getDate()-half);
      minDate = min;

      if(maxDate.getTime() >= rawMax.getTime())
        maxDate = rawMax;
      else if(minDate.getTime() <= rawMin.getTime())
        minDate = rawMin;

      if(sc==1 && lastLevel==1){
        maxDate = rawMax;
        minDate = rawMin;
      }

      if ( isNaN( maxDate.getTime() ) ) {
        maxDate = rawMax;
      }
      if ( isNaN( minDate.getTime() ) ) {
        minDate = rawMin;
      }

      xscale.domain([minDate, maxDate]);

      var diff = Math.round((maxDate-minDate)/(60 * 60 * 24 * 1000));
      if(diff<50){
        xAxis.ticks(d3.time.sunday)
          .tickFormat(d3.time.format('%m/%d'))
      }
      else{
        xAxis.ticks(d3.time.months)
          .tickFormat(function(d, i){
            return  (d.getMonth()+1)+'月';
          })
      }

      xa.call(xAxis);

      xa.selectAll(".tick text")
        .style("text-anchor", "start")
        .attr("x", 6)
        .attr("y", 6)
        .style('fill', function(d){
          return yearColor(d.getYear());
        });

      var infotext = 'From&nbsp;&nbsp;&nbsp;&nbsp;<text style="color:' + yearColor(minDate.getYear())
        + ';">' + outputFormat(minDate) +
        '</text>&nbsp;&nbsp;&nbsp;&nbsp;to&nbsp;&nbsp;&nbsp;&nbsp;<text style="color:' + yearColor(maxDate.getYear())
        + ';">' + outputFormat(maxDate)+'</text>';

      $('.timeinfo.header').html(infotext);

      updatePlot();

    }

    var lastLevel = null;
    var lastShift = 0;
    function zoomed(){
      var reverse = d3.time.scale()
        .domain([0, width])
        .range([minDate, maxDate]);

      var coordinates = [0, 0];
      coordinates = d3.mouse(this);

      // if(d3.event.translate[1]==0){
        $('body,html').scrollTop($('html').offset().top-d3.event.translate[1]/2);
      var shift = d3.event.translate[0] - lastShift;
      var newMax = xscale(maxDate);
      var newMin = xscale(minDate);

      newMax -= shift;
      newMin -= shift;

      if(maxDate.getTime() >= rawMax.getTime() && shift<0)
        maxDate = rawMax;
      else if(minDate.getTime() <= rawMin.getTime() && shift>0)
        minDate = rawMin;
      else{
        maxDate = new Date(reverse(newMax));
        minDate = new Date(reverse(newMin));
      }

      xscale.domain([minDate, maxDate]);

      var diff = Math.round((maxDate-minDate)/(60 * 60 * 24 * 1000));
      if(diff<50){
        xAxis.ticks(d3.time.sunday)
          .tickFormat(d3.time.format('%m/%d'))
      }
      else{
        xAxis.ticks(d3.time.months)
          .tickFormat(function(d, i){
            return  (d.getMonth()+1)+'月';
          })
      }

      xa.call(xAxis);

      xa.selectAll(".tick text")
        .style("text-anchor", "start")
        .attr("x", 6)
        .attr("y", 6)
        .style('fill', function(d){
          return yearColor(d.getYear());
        });

      lastShift =  d3.event.translate[0];
      lastLevel = d3.event.scale;

      var infotext = 'From&nbsp;&nbsp;&nbsp;&nbsp;<text style="color:' + yearColor(minDate.getYear())
        + ';">' + outputFormat(minDate) +
        '</text>&nbsp;&nbsp;&nbsp;&nbsp;to&nbsp;&nbsp;&nbsp;&nbsp;<text style="color:' + yearColor(maxDate.getYear())
        + ';">' + outputFormat(maxDate)+'</text>';

      $('.timeinfo.header').html(infotext);

      updatePlot();
    }
  }

  function preprocess(){
    for(var k in data){
      var duration = parseInt(data[k].medical_long);
      if(duration == 0) {
          duration = 3;
          data[k].medical_long = duration;
      }
      if(data[k].drug_name_en)
          data[k].drug_name_en += '<br>';
      var year = data[k].medical_date.split('/')[0];
      var month = data[k].medical_date.split('/')[1];
      var date = data[k].medical_date.split('/')[2];
      year = parseInt(year)+1911;

      data[k].startDate = dataTimeFormat.parse(year+month+date);
      data[k].endDate = new Date(data[k].startDate);
      data[k].endDate.setDate(data[k].startDate.getDate()+duration);
      if(hospitals.indexOf(data[k].hospital) == -1)
        hospitals.push(data[k].hospital);
    }

    data.sort(function(a, b){
      if(a.startDate.getTime() == b.startDate.getTime()){
        if(a.drug_name_cht > b.drug_name_cht) return 1;
        else return -1;
      }
      else
        return a.startDate.getTime() - b.startDate.getTime();
    });

    for(var k=0; k<data.length-1; k++) {
        // var count = 1;
        // while(true) {
        //     if(data[k+count]) {
        //         if(data[k].drug_name_en == data[k+count].drug_name_en) {
        //             data[k+count].remove = true;
        //             var duration = parseInt(data[k+count].medical_long);
        //             data[k].endDate.setDate(data[k].endDate.getDate()+duration);
        //         }
        //         else
        //             break;
        //     }
        //     else
        //         break;
        //     count += 1;
        // }
        if(data[k].drug_name_cht == data[k+1].drug_name_cht &&
            data[k].hospital == data[k+1].hospital &&
            data[k].startDate.getTime() >= data[k+1].startDate.getTime()) {
            
            data[k+1].startDate = new Date(data[k].endDate);
            var duration = parseInt(data[k+1].medical_long);
            data[k+1].endDate = new Date(data[k+1].startDate);
            data[k+1].endDate.setDate(data[k+1].startDate.getDate() + duration);
        }
    }
    // var i = data.length;
    // while(i--) {
    //     if(data[i].remove)
    //         data.splice(i, 1);
    // }
  }

  function updatePlot(){
    var lastEnd = null;
    var lastY = 0;

    svg.selectAll('.drugicon')
      .remove();

    svg.selectAll('.itext')
      .remove();

    var iconlocation = [];
    svg.selectAll('.display')
      .data(data)
      .attr('height', itemHeight)
      .attr('width', function(d){
        // console.log(d.endDate);
        // console.log(d.startDate);
        return xscale(d.endDate) - xscale(d.startDate);
      })
      .attr('x', function(d){ return xscale(d.startDate); })
      .attr('y', function(d){
        return 31+(itemHeight+interval)*hospitals.indexOf(d.hospital);
      })
      .style('opacity', 0.3)
      .each(function(d){

        var i = d3.select(this);
        if(parseInt(i.attr('width')) < 55)
          return;

        var ix = parseInt(i.attr('x'))+0.5*parseInt(i.attr('width'))-10;
        var iy = parseInt(i.attr('y'))+0.5*parseInt(i.attr('height'))-22;

        if(iconlocation.indexOf(ix+','+iy) == -1) {
          svg.append("svg:image")
            .attr('class', 'drugicon')
            .attr('x', ix-20)
            .attr('y', iy)
            .attr('width', 40)
            .attr('height', 40)
            .attr("xlink:href","images/medicine.svg")

          svg.append('text')
            .attr('class', 'itext')
            .attr('iloc', ix+','+iy)
            .attr('icount', 1)
            .attr('x', ix+15)
            .attr('y', iy+30)
            .style("font-size","25px")
            .text('1')

          iconlocation.push(ix+','+iy);
        }
        else {
          svg.selectAll('.itext')
            .text(function(){
              if (d3.select(this).attr('iloc') == (ix+','+iy)) {
                var c = parseInt(d3.select(this).attr('icount'));
                d3.select(this).attr('icount', c+1);
                return c+1;
              }
              else {
                return d3.select(this).attr('icount');
              }
            })
          return;
        }

      })

      d3.selectAll('.drugicon, .itext')
        .on('click', function(d){
        var coordinates = [0, 0];
        coordinates = d3.mouse(this);

        $('.cards').html('');

        svg.selectAll('.display')
          .each(function(d, i){
            var iy = parseFloat(d3.select(this).attr('y'));
            var ix = parseFloat(d3.select(this).attr('x'));
            var iw = parseFloat(d3.select(this).attr('width'));

            if(Math.abs(iy-coordinates[1])<itemHeight && coordinates[1]>iy){
              if(ix <= coordinates[0] && coordinates[0] <= (ix+iw))
                updateCards(d, i);
            }
          })

        var ctop = $('.cards').offset().bottom;
        $('body,html').animate({
          scrollTop: $(document).height()-$(window).height(),
        }, 700);

        $('.top.button').show();
      })
  }

  function updateCards(d, i ){
    var html = $('.cards').html();

    html += '<div class="ui card"><div class="content">' +
      '<div class="header" style="color:'+
      color(d.drug_name_cht) + '">' +
      d.drug_name_cht + '</div><div class="meta"><span>'+
      outputFormat(d.startDate) + '</span></div><p>'+
      '<br><span style="color:Blue">英文藥名</span>&nbsp;:&nbsp;<br>'+d.drug_name_en+
      '<span style="color:Red">病症</span>&nbsp;:&nbsp;<br>'+d.disease_name+
      '<br><span style="color:green">服用天數</span>&nbsp;:&nbsp;'+d.medical_long+
      '<br><span style="color:purple">醫院</span>&nbsp;:&nbsp;'+d.hospital+
      '</p></div></div></div>';

    $('.cards').html(html);

    show.push(i);
  }

})();
