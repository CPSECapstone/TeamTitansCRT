$(function() {
    $("div.content-placeholder").replaceWith(`
    <div class="container">
        <a id="toggle-btn" class="btn btn-default pull-right">Show Replays</a>
        <div class="row capture-row">
            <div class="col-lg-6 col-lg-offset-3">
                <h4 class="text-center">Capture Dashboard</h4>
            </div>
            <div class="col-lg-12">
                <div class="capture-dashboard"></div>
                ${insertLoadingSpinner("captureLoadingIcon")}
            </div>
        </div>
        <div class="row replay-row">
            <div class="col-lg-6 col-lg-offset-3">
                <h4 class="text-center">Replay Dashboard</h4>
            </div>
            <div class="col-lg-12">
                <div class="replay-dashboard"></div>
                ${insertLoadingSpinner("replayLoadingIcon")}
            </div>
        </div>
    </div>
    `);
    $("#toggle-btn").on("click", function() {
        toggleDashboard();
    });
    // updateStatus();
    testDashboardTable();
});

/* Updates the table to represent what is actually happening for capture. Only running and queued captures are visible in the table. */
function updateStatus() {
    $(".captureLoadingIcon").show();
    $.ajax({
        url: "/capture/status",
        type: "GET"
    })
    .done(createCaptureDashboard)
    .fail(function(err) {
        console.log(err);
        console.log("Error updating the status.")
        $(".captureLoadingIcon").hide();
    });
    
    /*
    $(".replayLoadingIcon").show();
    $.ajax({
        url: "/replay/status",
        type: "GET"
    })
    .done(function(data) {
        console.log("replay data:");
        console.log(data);
        setTimeout(function()
        {
            $(".replayLoadingIcon").hide();
            if (data.length > 0) {
                $("div.replay-dashboard").replaceWith(replayDashboardTemplate());    
                createTableRows("tbody.replay-table", data);
            }
            else {
                var body = "You have no replays currently running! Let's get started!";
                $("div.replay-dashboard").replaceWith(emptyDashboard(body, "Start new replay", "replay"));
            }
        },
        500);
    })
    .fail(function(err) {
        console.log(err);
        console.log("Error updating the status.")
        $(".replayLoadingIcon").hide();
    });
    */
    $("div.replay-row").hide();
}

function createCaptureDashboard(data) {
    setTimeout(function()
    {
        if (data.length > 0) {
            $("div.capture-dashboard").replaceWith(captureDashboardTemplate());    
            $("tbody.capture-table").empty();

            var rows = "";
            for(let i = 0; i < data.length; i++) {
                rows += createTableRow(selector, data[i]);
            }

            $(".captureLoadingIcon").hide();
            $("tbody.capture-table").append(rows);

            $.each(data, function(index, value) {
                var id = value["id"];
                $(`#stopButton${id}`).on("click", function() {
                    stopCapture(id);
                    updateStatus();
                });
                $(`#${id}-analyze`).on("click", function() {
                    openAnalysis(id);
                });
            })
            /*
             */
        }
        else {
            var body = "You have no captures currently running! Let's get started!";
            $(".captureLoadingIcon").hide();
            $("div.capture-dashboard").replaceWith(emptyDashboard(body, "Start new capture", "capture"));
        }
    },
    500);
}


function emptyDashboard(body, btn, ref) {
    return `
    <p class="text-center">${body}</p>
    <div class="text-center">
        <a href="${ref}" class="btn btn-default">${btn}</a>
    </div>`;
}

// TODO delete
function createTableRows(selector, data) {
    var rows = "";
    for(let i = 0; i < data.length; i++) {
        rows += createTableRow(selector, data[i]);
    }
    return rows;
}

function createTableRow(selector, capture) {
    var id = capture["id"];
    var status = capture["status"];
    var rds = capture["rds"];
    var rdsRegion = capture["rdsRegion"];
    var startTimeMilli = capture["startTime"];
    var startTime = "Not Specified";
    var tempStartTime = new Date(startTimeMilli);
    
    if (startTimeMilli) {
        startTime = tempStartTime.customFormat("#MM#/#DD#/#YYYY# #hh#:#mm#:#ss# #AMPM#")
    }
    
    var endTimeMilli = capture["endTime"];
    var endTime = "Not Specified";
    var tempEndTime = new Date(endTimeMilli);
    
    if (endTimeMilli) {
        endTime = tempEndTime.customFormat("#MM#/#DD#/#YYYY# #hh#:#mm#:#ss# #AMPM#")
    }
    
    // var row = createRow(selector, id, rds, rdsRegion, status, startTimeMilli, startTime, endTimeMilli, endTime);
    var row = `
            <tr data-toggle="collapse" data-target="#accordion${id}" class="clickable">
                <td width="(100/12)%">${createIcon(status)}</td>
                <td width="(100/4)%">${id}</td>
                <td width="(100/6)%">${status}</td>
                <td width="(100/6)%">${startTime}</td>
                <td width="(100/6)%">${endTime}</td>
                <td width="(100/6)%">${createButton(id, status)}</td>
            </tr>`;
        
    return row;
}

/*
function createRow(selector, id, rds, rdsRegion, status, startTimeMilli, startTime, endTimeMilli, endTime) {
     var body = {
        rds: rds,
        rdsRegion: rdsRegion,
        startTime: startTimeMilli,
        endTime: endTimeMilli,
        metrics: ["CPUUtilization", "FreeStorageSpace", "WriteThroughput"]
    };

    var row = `
            <tr data-toggle="collapse" data-target="#accordion${id}" class="clickable">
                <td width="(100/12)%">${createIcon(status)}</td>
                <td width="(100/4)%">${id}</td>
                <td width="(100/6)%">${status}</td>
                <td width="(100/6)%">${startTime}</td>
                <td width="(100/6)%">${endTime}</td>
                <td width="(100/6)%">${createButton(id, status)}</td>
            </tr>`;
    
    return row;

    $.ajax({
         url: "/cloudwatch/average",
        type: "POST",
        headers: { "Content-Type": "application/json" },
        data: JSON.stringify(body)
    })
    .done(function(data) {
        console.log('adding cloudwatch data');
        row += `
        <tr class="collapse" id="accordion${id}">
            <td colspan="6">
                <div>
                    <ul class="stats-list">
                        <li>CPU Utilization (percent): ${data[0]}</li>
                        <li>Free Storage Space Available (bytes): ${data[1]}</li>
                        <li>Write Throughput (bytes/sec): ${data[2]}</li>
                    </ul>
                </div>
            </td>
        </tr>`;
        // $(selector).append(row);
    
        $(`#stopButton${id}`).on("click", function() {
            stopCapture(id);
            updateStatus();
        });
        
        $(`#${id}-analyze`).on("click", function() {
            openAnalysis(id);
        });
    })
    .fail(function(err) {
        console.log('cloudwatch failed');
        // $(selector).append(row);
        $(`#stopButton${id}`).on("click", function() {
            stopCapture(id);
            updateStatus();
        });
        
        $(`#${id}-analyze`).on("click", function() {
            openAnalysis(id);
        });
    });
}
*/

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
           // table.ajax.reload();
        },
        error: function(err) {
            console.log(err);
            console.log("Error stopping capture on dashboard");
        }
    });
}

function toggleDashboard() {
    replayShown = $("div.replay-row").is(":visible");
    if (replayShown) {
        $("div.replay-row").hide();
        $("div.capture-row").show();
        $("#toggle-btn").html("Show Replays");
    }
    else {
        $("div.replay-row").show();
        $("div.capture-row").hide();
        $("#toggle-btn").html("Show Captures");
    }
}

function captureDashboardTemplate() {
    return `
    <table class="table table-hover" width="100%">
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

function replayDashboardTemplate() {
    return `
    <table class="table table-hover" width="100%">
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
        <tbody class="replay-table">
        </tbody>
    </table>`;
}

function insertLoadingSpinner(selector) {
    return `
    <div class="${selector}" style="display: none;" tabindex="-1" role="dialog">
        <div class="text-center">Loading...</div>
        <div class="spinner"></div>
    </div>`;
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
        return `<a href="analyze" id="${id}-analyze" class="defaultLinkColor">Analyze</a>`;
    }
}

function openAnalysis(id) {
    sessionStorage.setItem("defaultCapture", id);
}

/* Formarts the time from milliseconds to month day year hour minutes seconds. */
Date.prototype.customFormat = function(formatString){
  var YYYY,YY,MMMM,MMM,MM,M,DDDD,DDD,DD,D,hhhh,hhh,hh,h,mm,m,ss,s,ampm,AMPM,dMod,th;
  YY = ((YYYY=this.getFullYear())+"").slice(-2);
  MM = (M=this.getMonth()+1)<10?('0'+M):M;
  MMM = (MMMM=["January","February","March","April","May","June","July","August","September","October","November","December"][M-1]).substring(0,3);
  DD = (D=this.getDate())<10?('0'+D):D;
  DDD = (DDDD=["Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"][this.getDay()]).substring(0,3);
  th=(D>=10&&D<=20)?'th':((dMod=D%10)==1)?'st':(dMod==2)?'nd':(dMod==3)?'rd':'th';
  formatString = formatString.replace("#YYYY#",YYYY).replace("#YY#",YY).replace("#MMMM#",MMMM).replace("#MMM#",MMM).replace("#MM#",MM).replace("#M#",M).replace("#DDDD#",DDDD).replace("#DDD#",DDD).replace("#DD#",DD).replace("#D#",D).replace("#th#",th);
  h=(hhh=this.getHours());
  if (h==0) h=24;
  if (h>12) h-=12;
  hh = h<10?('0'+h):h;
  hhhh = hhh<10?('0'+hhh):hhh;
  AMPM=(ampm=hhh<12?'am':'pm').toUpperCase();
  mm=(m=this.getMinutes())<10?('0'+m):m;
  ss=(s=this.getSeconds())<10?('0'+s):s;
  return formatString.replace("#hhhh#",hhhh).replace("#hhh#",hhh).replace("#hh#",hh).replace("#h#",h).replace("#mm#",mm).replace("#m#",m).replace("#ss#",ss).replace("#s#",s).replace("#ampm#",ampm).replace("#AMPM#",AMPM);
};

function testDashboardTable() {
    var testCaps = [
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
    var testReps = [
        {
            captureId: "test_capture",
            captureLogFileName: "test_capture-Workload.log",
            database: null,
            dbpassword: null,
            dburl: null,
            dbusername: null,
            endTime: "2018-05-03",
            filterStatements: [],
            filterUsers: [],
            id: "MyReplay",
            rds: "testdb",
            rdsRegion: "US_WEST_1",
            replayType: "Fast Mode",
            s3: "teamtitans-test-mycrt",
            s3Region: "US_WEST_1",
            startTime: "2018-05-03",
            status: "Finished",
            transactionLimit: 0
        },
        {
            captureId: "test_capture",
            captureLogFileName: "test_capture-Workload.log",
            database: null,
            dbpassword: null,
            dburl: null,
            dbusername: null,
            endTime: "2018-05-03",
            filterStatements: [],
            filterUsers: [],
            id: "MyReplay_2",
            rds: "testdb",
            rdsRegion: "US_WEST_1",
            replayType: "Fast Mode",
            s3: "teamtitans-test-mycrt",
            s3Region: "US_WEST_1",
            startTime: "2018-05-03",
            status: "Finished",
            transactionLimit: 0
        }
    ]
    createCaptureDashboard(testCaps);
    $("div.replay-dashboard").html(replayDashboardTemplate());
    createTableRows("tbody.replay-table", testReps);

    $("div.replay-row").hide();
}
