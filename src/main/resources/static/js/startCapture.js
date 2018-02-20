var domain = "http://localhost:8080";

$(document).ready(function() {
    setDateFields();
    $('#example-getting-started').multiselect();

    $("#btnCaptureStart").on("click", function() {
        var startTime = null;
        if ($("#txtStartTime").val()) {
            startTime = new Date(String($("#txtStartTime").val()));
        }

        var endTime = null;
        if ($("#txtEndTime").val()) {
            endTime = new Date(String($("#txtEndTime").val()));
        }

        // Only start capture if rds and s3 selected
        if ($('#rdsSelector').val() != '' && $('#s3Selector').val() != '') {
            var body = {
                id: $("#txtID").val(),
                rds: $("#rdsSelector").val(),
                s3: $("#s3Selector").val(),
                startTime: startTime,
                endTime: endTime,
                fileSizeLimit: $("#txtMaxSize").val(),
                transactionLimit: $("#txtMaxTrans").val(),
                status: ""
            };

            startCapture("/capture/start", body);
        }
    });

});

function setDateFields() {
    var now = new Date();
    // TODO i think this is bc Pacific Time Zone
    now.setHours(now.getHours() - 8);
    $("#txtStartTime").val(String(now.toISOString().replace("Z", "")));
    // TODO: fix bug about 24 + 2
    now.setHours(now.getHours() + 2);
    $("#txtEndTime").val(String(now.toISOString().replace("Z", "")));
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
            $("#lblStatus").html("<p>Started Successful.</p>" +
                                "<a href=\"dashboard\" id=\"btnDashboard\" class=\"btn btn-default\">Go to Dashboard</a>");
                                 
            // window.location.href = "dashboard";
        },
        error: function(err) {
            $("#lblStatus").html("Startup failure.");
            console.log(err);
        }
    });
}

// Populate rds dropdown
$(function() {
    $.ajax({
        url: "/resource/rds",
        type: "GET",
        success: function(data) {
            var selector = '<option value="">Select RDS Endpoint</option>';
            for (var i = 0; i < data.length; i++) {
                selector += "<option value='" + data[i] +"'>" + data[i] + "</option>"; // Add selector option
            }
            $('#rdsSelector').html(selector);
        },
        error: function(err) {
            console.log(err);
        }
    });
});

// Populate s3 dropdown
$(function() {
    $.ajax({
        url: "/resource/s3",
        type: "GET",
        success: function(data) {
            var selector = '<option value="">Select S3 Endpoint</option>';
            for (var i = 0; i < data.length; i++) {
                selector += "<option value='" + data[i] +"'>" + data[i] + "</option>"; // Add selector option
            }
            $('#s3Selector').html(selector);
        },
        error: function(err) {
            console.log(err);
        }
    });
});