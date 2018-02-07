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
                var startTime = capture["startTime"];
                var endTime = capture["endTime"];
                var button = "";
                var icon = "";
                console.log("ID: " + id +
                    "\nStatus: " + status);
                
                if (status == "Running") {
                    icon = "<img src=\"../img/running.png\" alt=\"running\">";
                    button =
                        "<a href=\"#\" id=\"" + id +
                        "\" class=\"btn btn-default btn-stop\">Stop Capture</a>";
                }                
                else if (status == "Queued") {
                    icon = "<img src=\"../img/queued.png\" alt=\"queued\">";
                }
                else if (status == "Finished") {
                    icon = "<img src=\"../img/finished.png\" alt=\"finished\">";
                }
                else {
                    icon = "<img src=\"../img/failed.png\" alt=\"failed\">";
                }
                
                addToTable(icon, id, status, startTime, endTime, button);
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

function addToTable(icon, id, status, startTime, endTime, button) {
    // manually append html string
    $('#statusTable > tbody').append(
        "<tr data-toggle=\"collapse\" data-target=\"#accordion\" class=\"clickable\">" +
        "<td width=\"(100/12)%\">" + icon +
        "</td><td width=\"(100/4)%\">" + id +
        "</td><td width=\"(100/6)%\">" + status +
        "</td><td width=\"(100/6)%\">" + startTime.toLocaleString() +
        "</td><td width=\"(100/6)%\">" + endTime.toLocaleString() +
        "</td><td width=\"(100/6)%\">" + button +
        "</td></tr>" +
        "<tr>" +
            "<td colspan=\"3\">" +
                "<div id=\"accordion\" class=\"collapse\">" +
                    "<ul class=\"stats-list\">" +
                        "<li>CPU: </li>" +
                        "<li>RAM: </li>" +
                    "</ul>" +
                "</div>" +
            "</td>" +
        "</tr>");
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
