var domain = "http://localhost:8080";

$(document).ready(function() {
    $("#btnStatus").on("click", function() {
        updateStatus();
    });
    updateStatus();
});

function updateStatus() {
    $.ajax({
        url: domain + "/capture/status",
        type: "GET",
        success: function(data) {
            $('#statusTable > tbody').html("");
            for (var i = 0; i < data.length; i++) {
                var capture = data[i];
                var id = capture["id"];
                var status = capture["status"];
                var button = "";
                console.log("ID: " + id +
                    "\nStatus: " + status);
                if (status == "Running") {
                    button =
                        "<a href=\"#\" id=\"" + id +
                        "\" class=\"btn btn-default btn-stop\">Stop Capture</a>";
                }
                addToTable(id, status, button);
                // adds stop functionality to each button added
                $(String('#' + id)).on("click", function() {
                    stopCapture(this.id);
                });
            }
        },
        error: function(err) {
            console.log(err);
        }
    });
}

function addToTable(id, status, button) {
    // manually append html string
    $('#statusTable > tbody').append(
        "<tr><td>" + id +
        "</td><td>" + status +
        "</td><td>" + button +
        "</td></tr>");
}

function stopCapture(id) {
    var url = "/capture/stop";
    var body = {
        id: id,
        rds: null,
        s3: null,
        startTime: null,
        endTime: null,
        status: ""
    };
    $.ajax({
        url: domain + url,
        type: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        data: JSON.stringify(body),
        success: function() {
            $("#lblStatus").html("Stopped Successfully.");
            updateStatus();
        },
        error: function(err) {
            console.log(err);
        }
    });
}
