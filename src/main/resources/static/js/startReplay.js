var domain = "http://localhost:8080";

$(document).ready(function() {
    setDateFields();
    $('#example-getting-started').multiselect();

    $("#btnReplayStart").on("click", function() {
        // Only start capture if rds and s3 selected
        if ($('#rdsSelector').val() != '' && $('#captureSelector').val() != '') {
            var body = {
                id: $("#txtID").val(),
                rds: $("#rdsSelector").val(),
                status: ""
            };

            // options: Time Sensitive or Fast Mode
            body = {
                body,
                "Fast Mode"
            }

            startReplay("/replay/start", body);
        }
    });

});

function startReplay(url, body) {
    $.ajax({
        url: domain + url,
        type: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        data: JSON.stringify(body),
        success: function() {
            $("#lblStatus").html("<p>Started Successful.</p>" +
                                 "<a href=\"manageReplays\" id=\"btnManageReplays\" class=\"btn btn-default\">Manage Replays</a>" +
                                 "<a href=\"dashboard\" id=\"btnDashboard\" class=\"btn btn-default\">Go to Dashboard</a>");
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

// Populate capture select dropdown
// need endpoint that supplies names of all running captures
// maybe parse the repsonse from updateStatus??
$(function() {
    $.ajax({
        // TODO: idk what's supposed be here
        url: "",
        type: "GET",
        success: function(data) {
            var selector = '<option value="">Select a Capture</option>';
            for (var i = 0; i < data.length; i++) {
                selector += "<option value='" + data[i] +"'>" + data[i] + "</option>"; // Add selector option
            }
            $('#captureSelector').html(selector);
        },
        error: function(err) {
            console.log(err);
        }
    });
});
