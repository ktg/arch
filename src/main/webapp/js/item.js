(function poll() {
    $.ajax({
        url: "https://arch-lite.appspot.com/item",
        type: "GET",
        success: function(data) {
            console.log("polling: " + data);
        },
        dataType: "json",
        complete: setTimeout(function() {poll()}, 500),
        timeout: 2000
    })
})();