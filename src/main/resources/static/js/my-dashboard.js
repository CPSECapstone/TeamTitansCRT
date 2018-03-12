$(function() {
    $("div.content-placeholder").replaceWith(`
    <div class="container">
        <div class="row">
            <div class="col-lg-6 col-lg-offset-3">
                <h4 class="text-center">Capture and Replay Dashboard</h4>
            </div>
        </div>
        <div class="row">
            <div class="col-lg-12">
                <div class="manageCapturesLoadingIcon" tabindex="-1" role="dialog"><div class="spinner"></div></div>
                <div class="dashboard-content"></div>
            </div>
        </div>
    </div>
    `);
    updateStatus();
    /*
    var data = [
        {
            id: "Test1",
            startTime: 1520871274784,
            endTime: null,
            fileSizeLimit: 420,
            transactionLimit: 840,
            status: "Running"
        },
        {
            id: "Test2",
            startTime: 1520871274784,
            endTime: 1520881274784,
            fileSizeLimit: 420,
            transactionLimit: 840,
            status: "Finished"
        }
    ]
    $("div.dashboard-content").replaceWith(captureDashboard(data));
    fillTable(data);
    */
});

function emptyDashboard() {
    return `
    <p class="text-center">You have no captures or replays! Let's get started!</p>
    <div class="text-center">
        <a href="capture" class="btn btn-default">Start new capture</a>
    </div>`;
}

/* Updates the table to represent what is actually happening for capture. Only running and queued captures are visible in the table. */
function updateStatus() {
    $.ajax({
        url: "/capture/status",
        type: "GET",
        beforeSend: function() {
            $(".manageCapturesLoadingIcon").show();
        },
        complete: function() {
            $(".manageCapturesLoadingIcon").hide();
        },
        success: function(data) {
            if (data.length > 0) {
                $(".manageCapturesLoadingIcon").hide();
                $("div.dashboard-content").replaceWith(captureDashboard(data));    
                fillTable(data);
            }
            else {
                $(".manageCapturesLoadingIcon").hide();
                $("div.dashboard-content").replaceWith(emptyDashboard());    
            }
        },
        error: function(err) {
            console.log(err);
            console.log("Error updating the status.")
        }
    });
}

function captureDashboard(data) {
    return `
    <table class="table" width="100%">
        <thead class="thead-dark">
            <tr class="">
                <th scope="col"> </th> 
                <th scope="col">Name</th>
                <th scope="col">Status</th>
                <th scope="col">Start Time</th>
                <th scope="col">End Time</th>
                <th scope="col"> </th>
            </tr>
        </thead>
        <tbody class="capture-table">
        </tbody>
    </table>`;
}

function fillTable(data) {
    $("tbody.capture-table").empty();
    data.map(createTableRow);
}

function createTableRow(capture) {
    $("tbody.capture-table").append(createRow(capture));
    var id = capture["id"];
    $(`a#stopButton${id}`).on("click", function() {
        stopCapture(id);
        updateStatus();
    });
    /*
    <tr>
        <td colspan="3">
            <div id="accordion${id} class="collapse">
                <ul class="stats-list">
                    <li>CPU Utilization (percent): ${data[0]}</li>
                    <li>Free Storage Space Available (bytes): ${data[1]} </li>
                    <li>Write Throughput (bytes/sec): ${data[2]} </li>
                </ul>
            </div>
        </td>
    </tr>`;
    */
}

function createRow(capture) {
    var id = capture["id"];
    var status = capture["status"];
    var startTime = capture["startTime"];
    var endTime = capture["endTime"];
    console.log(`ID: ${id}\tStatus: ${status}`);

    return `
    <tr data-toggle="collapse" data-target="#accordion${id}" class="clickable">
        <td width="(100/12)%">${createIcon(status)}</td>
        <td width="(100/4)%">${id}</td>
        <td width="(100/6)%">${status}</td>
        <td width="(100/6)%">${formatTime(startTime, "MM/dd/yyyy HH:mm:ss")}</td>
        <td width="(100/6)%">${formatTime(endTime, "MM/dd/yyyy HH:mm:ss")}</td>
        <td width="(100/6)%">${createButton(id, status)}</td>
    </tr>`;
}

function createIcon(status) {
    if (status == "Running") {
        return `<img src="../img/running.png" alt="running">`;
    }                
    else if (status == "Queued") {
        return `<img src="../img/queued.png" alt="queued">`;
    }
    else if (status == "Finished") {
        return `<img src="../img/finished.png" alt="finished">`;
    }
    else {
        return `<img src="../img/failed.png" alt="failed">`;
    }
}

function createButton(id, status) {
    if (status == "Running") {
        return `<a href="javascript:void(0)" id="stopButton${id}" class="defaultLinkColor">Stop Capture</a>`;
    }
    else if (status == "Finished") {
        return `<a href="analyze" class="defaultLinkColor">Analyze</a>`;
    }
}

/* If you push the stop capture button, it ends immediately. */
function stopCapture(id) {
    console.log(`Stopping capture: ${id}`);
    var url = "/capture/stop";
    var body = {
        id: id
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
    if (time === null) {
        return 'Not Specified';
    }

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
