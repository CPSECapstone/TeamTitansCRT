var domain = "http://localhost:8080";

$(document).ready(function() {
    var now = new Date();
    $("#txtStartTime").val(String(now.toISOString().replace("Z", "")));
    // TODO: fix bug about 24 + 2
    now.setHours(now.getHours() + 2);
    $("#txtEndTime").val(String(now.toISOString().replace("Z", "")));

    $("#btnCaptureStart").on("click", function() {
        var startTime = null;
        if ($("#txtStartTime").val()) {
            startTime = String($("#txtStartTime").val());
        }

        var endTime = null;
        if ($("#txtStartTime").val()) {
            endTime = String($("#txtEndTime").val());
        }

        var body = {
            id: $("#txtID").val(),
            rds: $("#txtRDS").val(),
            s3: $("#txtS3").val(),
            startTime: startTime,
            endTime: endTime,
            status: ""
        };
        startCapture("/capture/start", body);
    });

    $("#btnCaptureStop").on("click", function() {
        var body = {
            id: $("#txtID").val(),
            rds: $("#txtRDS").val(),
            s3: $("#txtS3").val(),
            startTime: null,
            endTime: null,
            status: ""
        };
        stopCapture("/capture/stop", body);
    });

    $("#btnStatus").on("click", function() {
        updateStatus();
    });
});

function updateStatus() {
    $.ajax({
        url: domain + "/capture/status",
        type: "GET",
        success: function(data) {
            // var targetID = $("#txtID").val();
            $('#statusTable > tbody').html("");
            var dataString = "";
            for (var i = 0; i < data.length; i++) {
                var capture = data[i];
                var id = capture["id"];
                var status = capture["status"];
                console.log("ID: " + id +
                    "\nStatus: " + status);
                dataString = dataString.concat(
                    "<tr><td>" + id +
                    "</td><td>" + status +
                    "</td></tr>");
            }
            $('#statusTable > tbody').append(
                dataString);
        },
        error: function(err) {
            console.log(err);
        }
    });
}

function startCapture(url, body) {
    $.ajax({
        url: domain + url,
        type: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        data: JSON.stringify(body),
        success: function() {
            $("#lblStatus").html("Started Successful.");
            updateStatus();
        },
        error: function(err) {
            console.log(err);
        }
    });
}

function stopCapture(url, body) {
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
