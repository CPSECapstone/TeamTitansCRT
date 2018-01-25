var domain = "http://localhost:8080";

$(document).ready(function() {
    var now = new Date();
    // TODO i think this is bc Pacific Time Zone
    now.setHours(now.getHours() - 8);
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

});

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
            // window.location.href = "dashboard";
        },
        error: function(err) {
            $("#lblStatus").html("Startup failure.");
            console.log(err);
        }
    });
}
