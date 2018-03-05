$(function() {
    updateStatus();
});

function updateStatus() {
    $.ajax({
        url: "/capture/status",
        type: "GET",
        success: function(data) {
            $('#accordion').html("");
            for (var i = 0; i < data.length; i++) {
                var capture = data[i];
                addToTable(capture)
            }
        },
        error: function(err) {
            console.log(err);
        }
    });
}

function addToTable(capture) {
    var id = capture["id"];
    var status = capture["status"];
    var startTime = new Date(capture["startTime"]);
    startTime.setHours(startTime.getHours() - 8);
    startTime = startTime.toISOString().replace("Z", "");
    var endTime = new Date(capture["endTime"]);
    endTime.setHours(endTime.getHours() - 8);
    endTime = endTime.toISOString().replace("Z", "");
    var fileSizeLimit = capture["fileSizeLimit"];
    var transactionLimit = capture["transactionLimit"];
    console.log("ID: " + id +
        "\nStatus: " + status);
        
    $("#accordion").append(
        "<div class=\"card\">"+
        "<div class=\"card-header\" role=\"tab\" id=\"\">"+
        "    <h5 class=\"mb-0\">"+
        "        <a data-toggle=\"collapse\" data-parent=\"#accordion\" href=\"#" + id + "\" aria-expanded=\"true\" aria-controls=\"collapseOne\">"+
        "            " + id + ""+
        "        <\/a>"+
        "    <\/h5>"+
        "<\/div>"+
        "<div id=\"" + id + "\" class=\"collapse\" role=\"tabpanel\" aria-labelledby=\"headingOne\">"+
        "    <div class=\"card-block\">"+
        "        <label class=\"input-label\">Start Time:<input class=\"txtStartTime form-control\" type=\"datetime-local\" value=\"" + startTime + "\"><\/label>"+
        "        <label class=\"input-label\">End Time:<input class=\"txtEndTime form-control\" type=\"datetime-local\" value=\"" + endTime + "\"><\/label>"+
        "        <label class=\"input-label\">Max Capture Size (mB):<input class=\"txtMaxSize form-control\" type=\"text\" value=\"" + fileSizeLimit + "\"><\/label>"+
        "        <label class=\"input-label\">Max Number of Transactions:<input class=\"txtMaxTrans form-control\" type=\"text\" value=\"" + transactionLimit + "\"><\/label>"+
        "        <a href=\"#\" class=\"btn btn-sm btn-default save\">Save<\/a>"+
        "    <\/div>"+
        "<\/div>"+
        "<\/div>"
    );
    $("#" + id + " .save").on("click", function() {
        updateCapture(id);
    });
}

function updateCapture(id) {
    var body = {
        id: id,
        startTime: $("#" + id + " .txtStartTime").val(),
        endTime: $("#" + id + " .txtEndTime").val(),
        fileSizeLimit: $("#" + id + " .txtMaxSize").val(),
        transactionLimit: $("#" + id + " .txtMaxTrans").val()
    };
    
    $.ajax({
        url: "/capture/update",
        type: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        data: JSON.stringify(body),
        success: function() {
            console.log(id + " success")
            updateStatus();
        },
        error: function(err) {
            $("#lblStatus").html("Startup failure.");
            console.log(err);
        }
    });
}