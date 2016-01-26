(function poll() {
    $.ajax({
        url: "/item",
        type: "GET",
        ifModified: true,
        success: function(data) {
            //console.log(data);
            if(data != null)
            {
				if(data['in'] != null)
				{
					$('#in').text(data['in'])
				}

				if(data['out'] != null)
				{
					$('#out').text(data['out'])
				}

				if(data.shake != null)
				{
					$('#detail').empty()
					for (var i = 0; i < data.shake.length; i++)
					{
						$('#detail').append('<div>').append(data.shake[i]).append('</div>');
                    }
                    $('#detail').append('<div>2\'10\"</div>');
				}
			}
        },
        dataType: "json",
        complete: setTimeout(function() {poll()}, 100),
        timeout: 200
    });
})();

$(document).keypress(function(e)
{
    switch(e.which) {
        case 122:
            $('#in').css('opacity', '0');
            $('#out').css('opacity', '0');
            $('#detail').css('opacity', '0');
            break;

        case 120:
            $('#in').css('opacity', '1');
            $('#out').css('opacity', '0');
            $('#detail').css('opacity', '0');
            break;

        case 99:
            $('#in').css('opacity', '0');
            $('#out').css('opacity', '1');
            $('#detail').css('opacity', '1');
            break;
        }
    });
