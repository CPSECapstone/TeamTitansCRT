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
            };
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
            };
        sendCapture("/capture/stop", body);
    });

    $("#btnStatus").on("click", function() {
        $.ajax({
            url: domain + "/capture/status",
            type: "GET",
            success: function(data) {
                var targetID = $("#txtID").val();

                for (var i = 0; i < data.length; i++) {
                    var capture = data[i];
                    var id = capture["id"];
                    var status = capture["status"];
                    console.log("ID: " + id + "\nStatus: " + status);
                    if(id === targetID) {
                        $("#lblStatus").html(status);
                    }
                }
            },
            error: function(err) {
                console.log(err);
            }
        });
    });
});

function sendCapture(url, body) {
    $.ajax({
        url: domain + url,
        type: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        data: JSON.stringify(body),
        success: function() {
            $("#lblStatus").html("Successful.");
        },
        error: function(err) {
            console.log(err);
        }
    });
}