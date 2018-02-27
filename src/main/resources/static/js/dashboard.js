var domain = "http://localhost:8080";

$(document).ready(function() {
    $("#btnStatus").on("click", function() {
        updateStatus();
    });
    updateStatus();
});

/* Updates the table to represent what is actually happening for capture. Only running and queued captures are visible in the table. */
function updateStatus() {
    $.ajax({
        url: "/capture/status",
        type: "GET",
        success: function(data) {
            $('#statusTable > tbody').html("");
            for (var i = 0; i < data.length; i++) {
                var capture = data[i];
                var id = capture["id"];
                var status = capture["status"];
                var startTime = capture["startTime"];
                var endTime = capture["endTime"];
                var button = "";
                var icon = "";
                console.log("ID: " + id +
                    "\nStatus: " + status);
                
                if (status == "Running") {
                    icon = "<img src=\"../img/running.png\" alt=\"running\">";
                    button =
                        "<a href=\"#\" id=\"stopButton" + id +
                        "\" class=\"btn btn-default btn-stop\">Stop Capture</a>";
                    addToTable(icon, id, status, startTime, endTime, button);
                }                
                else if (status == "Queued") {
                    icon = "<img src=\"../img/queued.png\" alt=\"queued\">";
                    addToTable(icon, id, status, startTime, endTime, button);
                }
            }
        },
        error: function(err) {
            console.log(err);
            console.log("Error updating the status.")
        }
    });
}

/* Adds new captures to the table. Takes the capture's id and gets its status, start time, and end time. Also, adds an icon to show the status of the capture visually. If the capture is running, there will be a stop capture button as well. */
function addToTable(icon, id, status, startTime, endTime, button) {
    var body = {
        id: id,
        startTime: startTime,
        endTime: endTime,
        metrics: ["CPUUtilization", "FreeStorageSpace", "WriteThroughput"]
    };
    
    $.ajax({
        url: "/cloudwatch/average",
        type: "POST",
        headers: { "Content-Type": "application/json" },
        data: JSON.stringify(body),
        success: function(data) {
            
            // manually append html string
            $('#statusTable > tbody').append(
                "<tr data-toggle=\"collapse\" data-target=\"#accordion" + id + "\" class=\"clickable\">" +
                "<td width=\"(100/12)%\">" + icon +
                "</td><td width=\"(100/4)%\">" + id +
                "</td><td width=\"(100/6)%\">" + status +
                "</td><td width=\"(100/6)%\">" + formatTime(startTime, "MM/dd/yyyy HH:mm:ss") +
                "</td><td width=\"(100/6)%\">" + formatTime(endTime, "MM/dd/yyyy HH:mm:ss") +
                "</td><td width=\"(100/6)%\">" + button +
                "</td></tr>" +
                "<tr>" +
                    "<td colspan=\"3\">" +
                        "<div id=\"accordion" + id + "\" class=\"collapse\">" +
                            "<ul class=\"stats-list\">" +
                                "<li>CPU Utilization (percent): " + data[0] + "</li>" +
                                "<li>Free Storage Space Available (bytes): " + data[1] + "</li>" +
                                "<li>Write Throughput (bytes/sec): " + data[2] + "</li>" +
                            "</ul>" +
                        "</div>" +
                    "</td>" +
                "</tr>");
        },
        error: function(err) {
            console.log(err);
            console.log("Error adding the the table on the dashboard.");
        }
    });
}

/* If you push the stop capture button, it ends immediately. */
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
        url: url,
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
            console.log("Error stopping capture on dashboard");
        }
    });
}

/* Formarts the time from milliseconds to month day year hour minutes seconds. */
function formatTime(time, format) {
    var t = new Date(time);
    var tf = function (i) { return (i < 10 ? '0' : '') + i };
    return format.replace(/yyyy|MM|dd|HH|mm|ss/g, function (a) {
        switch (a) {
            case 'yyyy': //year
                return tf(t.getFullYear());
                break;
            case 'MM': //month
                return tf(t.getMonth() + 1);
                break;
            case 'mm': //minutes
                return tf(t.getMinutes());
                break;
            case 'dd': //day
                return tf(t.getDate());
                break;
            case 'HH': //hour
                return tf(t.getHours());
                break;
            case 'ss': //seconds
                return tf(t.getSeconds());
                break;
        }
    })
}

$(function() {
    $('#statusTable').on('click', '[id^=stopButton]' , function() {
        stopCapture(this.id.replace("stopButton", ""));
    });
});