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
    var body = {
        id: id,
        startTime: startTime,
        endTime: endTime,
        metric: "CPUUtilization"
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
                                "<li>CPU Utilization: " + data + "</li>" +
                            "</ul>" +
                        "</div>" +
                    "</td>" +
                "</tr>");
        }
    });
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

function formatTime(time, format) {
            var t = new Date(time);
            var tf = function (i) { return (i < 10 ? '0' : '') + i };
            return format.replace(/yyyy|MM|dd|HH|mm|ss/g, function (a) {
                switch (a) {
                    case 'yyyy':
                        return tf(t.getFullYear());
                        break;
                    case 'MM':
                        return tf(t.getMonth() + 1);
                        break;
                    case 'mm':
                        return tf(t.getMinutes());
                        break;
                    case 'dd':
                        return tf(t.getDate());
                        break;
                    case 'HH':
                        return tf(t.getHours());
                        break;
                    case 'ss':
                        return tf(t.getSeconds());
                        break;
                }
            })
        }
