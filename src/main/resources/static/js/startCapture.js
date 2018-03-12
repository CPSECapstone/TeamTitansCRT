$(function() {
    populateFields();

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
            var capture = {
                id: $("#txtID").val(),
                rds: $("#rdsSelector").val(),
                s3: $("#s3Selector").val(),
                startTime: startTime,
                endTime: endTime,
                fileSizeLimit: $("#txtMaxSize").val(),
                transactionLimit: $("#txtMaxTrans").val(),
                filterStatements: $("#txtFilterStatements").val().split(',').map(x => x.trim()),
                filterUsers: $("#txtFilterUsers").val().split(',').map(x => x.trim()),
                status: ""
            };

            startCapture(capture);
        }
    });

});

/**           
 * Starts a capture using the given Capture object
 * @param  {Capture} The Capture object to be started
 */
function startCapture(capture) {
    $.ajax({
        url: "/capture/start",
        type: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        data: JSON.stringify(capture),
        success: function() {
            $("#lblStatus").html("<p>Started Successful.</p>" +
                                 "<a href=\"manageCaptures\" id=\"btnManageCaptures\" class=\"btn btn-default\">Manage Captures</a>" +
                                 "<a href=\"dashboard\" id=\"btnDashboard\" class=\"btn btn-default\">Go to Dashboard</a>");
        },
        error: function(err) {
            $("#lblStatus").html("Startup failure.");

            console.log("Error starting capture");
            console.log(err);
        }
    });
}

function populateFields() {
    var timeDifference = -8;
    setDateFields(timeDifference);
    populateRDSDropdown();
    populateS3Dropdown();    
}

/**
 * Automatically sets date fields to current time and +2hr
 * @param {int} timeDifference the time difference as an int (ex: -8)
 */
function setDateFields(timeDifference) {
    var now = new Date();
    // TODO i think this is bc Pacific Time Zone
    now.setHours(now.getHours() + timeDifference);
    $("#txtStartTime").val(String(now.toISOString().replace("Z", "")));
    // TODO: fix bug about 24 + 2
    now.setHours(now.getHours() + 2);
    $("#txtEndTime").val(String(now.toISOString().replace("Z", "")));
}

/**
 * Populate rds dropdown
 */
function populateRDSDropdown() {
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
            console.log("Error populating rds dropdown")
            console.log(err);
        }
    });
}

/**
 * Populate s3 dropdown
 */
function populateS3Dropdown() {
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
            console.log("Error populating s3 dropdown")
            console.log(err);
        }
    });
}