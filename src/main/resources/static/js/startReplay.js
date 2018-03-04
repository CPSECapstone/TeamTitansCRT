$(function() {
    $("#btnReplayStart").on("click", function() {
        // Only start capture if rds and capture selected
        if ($('#rdsSelector').val() != '' && $('#captureSelector').val() != '') {
            var replay = {
                id: $("#txtID").val(),
                rds: $("#rdsSelector").val(),
                s3: $("#s3Selector").val(),
                filterStatements: $("#txtFilterStatements").val().split(',').map(x => x.trim()),
                filterUsers: $("#txtFilterUsers").val().split(',').map(x => x.trim())
            };

            // options: Time Sensitive or Fast Mode
            replay = {
                replay,
                replayType: $("#replayTypeSelector").val()
            }

            startReplay(replay);
        }
    });

});

/**
 * @param  {Replay} The replay object to pass to back end
 */
function startReplay(replay) {
    $.ajax({
        url: "/replay/start",
        type: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        data: JSON.stringify(replay),
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
