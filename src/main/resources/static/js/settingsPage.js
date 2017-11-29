var domain = "http://localhost:8080";

$(document).ready(function() {
    $("#btnCaptureStart").on("click", function() {
        var body =
            {
                id: $("#txtID").val(),
                rds: $("#txtRDS").val(),
                s3: $("#txtS3").val(),
                startTime: null,
                endTime: null,
                status: ""
            }
        console.log(body);
        sendCapture("/capture/start", body);
    });

    $("#btnCaptureStop").on("click", function() {
        var body =
            {
                id: $("#txtID").val(),
                rds: $("#txtRDS").val(),
                s3: $("#txtS3").val(),
                startTime: null,
                endTime: null,
                status: ""
            }
        console.log(body);
        sendCapture("/capture/stop", body);
    });

    $("#btnStatus").on("click", function() {
        console.log("getting status of captures...");
        $.ajax({
            url: domain + "/capture/status",
            type: "GET",
            success: function(data) {
                var status;
                for (var i = 0; i < data.length; i++) {
                    var capture = data[i];
                    var id = capture["id"];
                    status = capture["status"];
                    console.log("ID: " + id + "\nStatus: " + status);
                }
                $("#lblStatus").html(status);
            },
            error: function(err) {
                console.log(err);
            }
        });
    });
});

function sendCapture(url, body) {
    console.log("sending capture...");
    $.ajax({
        url: domain + url,
        type: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        dataType: "json",
        data: JSON.stringify(body),
        success: function() {
            console.log("success");
        },
        error: function(err) {
            console.log(err);
        }
    });
}