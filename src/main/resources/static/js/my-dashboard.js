$(function() {
    $(".content-placeholder").replaceWith(mainTemplate());
    $("#toggle-btn").on("click", function() {
        toggleDashboard();
    });
    testDashboardTable();
    // updateStatus();
    $(".replay-row").hide();
});

/* ----------------------- Main Functions ------------------------------------ */
function updateStatus() {
    $(".captureLoadingIcon").show();
    getCaptures()
    .done(createCaptureDashboard)
    .fail(function(err) {
        console.log("Error updating captures.")
        // console.log(err);
    })
    .always(function() {
        $(".captureLoadingIcon").hide();
    });
    
    $(".replayLoadingIcon").show();
    getReplays()
    .done(createReplayDashboard)
    .fail(function(err) {
        console.log("Error updating replays.")
        // console.log(err);
    })
    .always(function() {
        $(".replayLoadingIcon").hide();
    });
}

function createCaptureDashboard(data) {
    setTimeout(function()
    {
        if (data.length > 0) {
            $(".capture-dashboard").replaceWith(captureDashboardTemplate());    
            $(".capture-table-body-running").empty();
            $(".capture-table-body-queued").empty();

            // sort running captures first
            data.sort(sortRunningFirst);

            var runningRows = [];
            var queuedRows = [];
            for(let i = 0; i < data.length; i++) {
                var capture = data[i];
                var id = capture["id"];

                if (capture["status"] == "Running") {
                    runningRows.push(createTableRow(capture));
                    runningRows.push(`<tr class="collapse" id="${selectorCloudwatch(id)}"></tr>`);
                }
                else if (capture["status"] == "Queued") {
                    queuedRows.push(createTableRow(capture));
                    queuedRows.push(`<tr class="collapse" id="${selectorCloudwatch(id)}"></tr>`);
                }

                // async adds cloudwatch data
                getMetrics(capture)
                .done(function(metrics) {
                    console.log('adding cloudwatch data');
                    $(`#${selectorCloudwatch(id)}`).html(metricsTemplate(metrics));
                })
                .fail(function(err) {
                    console.log('cloudwatch failed');
                });
            }

            $(".capture-table-body-running").append(runningRows.join(''));
            $(".capture-table-body-queued").append(queuedRows.join(''));

            // add event handlers to each row
            $.each(data, function(index, capture) {
                var id = capture["id"];
                $(`#${selectorStop(id)}`).on("click", function() {
                    stopCapture(id)
                    .done(function() {
                        console.log(`Stopped capture ${id}`);
                    })
                    .fail(function(err) {
                        console.log(err);
                        console.log(`Error stopping capture ${id}`);
                    })
                    .always(function() {
                        updateStatus();
                    });
                });
                $(`#${selectorAnalyze(id)}`).on("click", function() {
                    openAnalysis(id);
                });
            });
        }
        else {
            $(".capture-dashboard").replaceWith(captureDashboardEmptyTemplate());
        }
    },
    500);
}

function createReplayDashboard(data) {
    setTimeout(function()
    {
        if (data.length > 0) {
            $(".replay-dashboard").replaceWith(replayDashboardTemplate());    
            $(".replay-table-body").empty();

            // sort running replays first
            data.sort(sortRunningFirst);

            var rows = "";
            for(let i = 0; i < data.length; i++) {
                rows += createTableRow(data[i]);
            }

            $(".replay-table-body").append(rows);

            $.each(data, function(index, replay) {
                var id = replay["id"];
                $(`#${selectorStop(id)}`).on("click", function() {
                    stopReplay(id)
                    .done(function() {
                        console.log(`Stopped replay ${id}`);
                    })
                    .fail(function(err) {
                        console.log(err);
                        console.log(`Error stopping replay ${id}`);
                    })
                    .always(function() {
                        updateStatus();
                    });
                });
                $(`#${selectorAnalyze(id)}`).on("click", function() {
                    openAnalysis(id);
                });
            });
        }
        else {
            $(".replay-dashboard").replaceWith(replayDashboardEmptyTemplate());
        }
    },
    500);
}

function createTableRow(capture) {
    var id = capture["id"];
    var status = capture["status"];
    var rds = capture["rds"];
    var rdsRegion = capture["rdsRegion"];
    var startTimeMilli = capture["startTime"];
    var startTime = "Not Specified";
    
    if (startTimeMilli) {
        var tempStartTime = new Date(startTimeMilli);
        startTime = tempStartTime.customFormat("#MM#/#DD#/#YYYY# #hh#:#mm#:#ss# #AMPM#")
    }
    
    var endTimeMilli = capture["endTime"];
    var endTime = "Not Specified";
    
    if (endTimeMilli) {
        var tempEndTime = new Date(endTimeMilli);
        endTime = tempEndTime.customFormat("#MM#/#DD#/#YYYY# #hh#:#mm#:#ss# #AMPM#")
    }
    
    var btn = "",
        icon = "";

    if (status == "Running") {
        btn = `<a href="javascript:void(0)" id="${selectorStop(id)}" class="defaultLinkColor">Stop Capture</a>`;
        icon = getRunningImageTemplate();
    }                
    else if (status == "Finished") {
        btn = `<a href="analyze" id="${selectorAnalyze(id)}" class="defaultLinkColor">Analyze</a>`;
        icon = getFinishedImageTemplate();
    }
    else if (status == "Queued") {
        icon = getQueuedImageTemplate();
    }
    else {
        icon = getFailedImageTemplate();
    }

    var row = `
    <tr data-toggle="collapse" data-target="#${selectorCloudwatch(id)}" class="clickable">
        <td>${icon}</td>
        <td>${id}</td>
        <td>${status}</td>
        <td>${startTime}</td>
        <td>${endTime}</td>
        <td>${btn}</td>
    </tr>`;

    return row;
}

function toggleDashboard() {
    replayShown = $(".replay-row").is(":visible");
    if (replayShown) {
        $(".replay-row").hide();
        $(".capture-row").show();
        $("#toggle-btn").html("Show Replays");
    }
    else {
        $(".replay-row").show();
        $(".capture-row").hide();
        $("#toggle-btn").html("Show Captures");
    }
}

/* ----------------------- Selectors ----------------------------------------- */
function selectorCloudwatch(id) {
    return `cloudwatch-${id}`;
}

function selectorStop(id) {
    return `stop-${id}`;
}

function selectorAnalyze(id) {
    return `analyze-${id}`;
}

/* ----------------------- Template Functions -------------------------------- */
function mainTemplate() {
    return `
    <div class="container">
        <a id="toggle-btn" class="btn btn-default pull-right">Show Replays</a>
        <div class="row capture-row">
            <div class="col-lg-6 col-lg-offset-3">
                <h4 class="text-center">Capture Dashboard</h4>
                <hr>
            </div>
            <div class="col-lg-12">
                <div class="capture-dashboard"></div>
                <div class="captureLoadingIcon" style="display: none;" tabindex="-1" role="dialog">
                    <div class="text-center">Loading...</div>
                    <div class="spinner"></div>
                </div>
            </div>
        </div>
        <div class="row replay-row">
            <div class="col-lg-6 col-lg-offset-3">
                <h4 class="text-center">Replay Dashboard</h4>
                <hr>
            </div>
            <div class="col-lg-12">
                <div class="replay-dashboard"></div>
                <div class="replayLoadingIcon" style="display: none;" tabindex="-1" role="dialog">
                    <div class="text-center">Loading...</div>
                    <div class="spinner"></div>
                </div>
            </div>
        </div>
    </div>`;
}

function captureDashboardTemplate() {
    return `
    <h4>Running</h4>
    <div class="margin-top panel z-depth-1">
        <table class="capture-table table table-hover table-bordered" >
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
            <tbody class="capture-table-body-running">
            </tbody>
        </table>
    </div>
    <br>
    <h4>Queued</h4>
    <div class="margin-top panel z-depth-1">
        <table class="capture-table table table-hover table-bordered" >
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
            <tbody class="capture-table-body-queued">
            </tbody>
        </table>
    </div>`;
}

function captureDashboardEmptyTemplate() {
    return `
    <p class="text-center">You have no captures currently running! Let's get started!</p>
    <div class="text-center">
        <a href="capture" class="btn btn-default">Start new capture</a>
    </div>`;
}

function replayDashboardTemplate() {
    return `
    <div class="margin-top panel z-depth-1">
        <table class="replay-table table table-hover table-bordered" >
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
            <tbody class="replay-table-body">
            </tbody>
        </table>
    </div>`;
}

function replayDashboardEmptyTemplate() {
    return `
    <p class="text-center">You have no replays currently running! Let's get started!</p>
    <div class="text-center">
        <a href="replay" class="btn btn-default">Start new replay</a>
    </div>`;
}

function metricsTemplate(metrics) {
    return `
    <td colspan="6">
        <div>
            <ul class="stats-list">
                <li>CPU Utilization (percent): ${metrics[0]}</li>
                <li>Free Storage Space Available (bytes): ${metrics[1]}</li>
                <li>Write Throughput (bytes/sec): ${metrics[2]}</li>
            </ul>
        </div>
    </td>`;
}

function getRunningImageTemplate() {
    return `<i class="dashboard-icon fa fa-circle-o-notch fa-spin" style="color:rgb(0,0,200);"></i>`;
    // return `<img src="./img/running.png" alt="running">`;
}

function getFinishedImageTemplate() {
    return `<img src="./img/finished.png" alt="finished">`;
}

function getQueuedImageTemplate() {
    return `<i class="dashboard-icon fa fa-clock-o" style="color:rgb(200,200,0);"></i>`;
    // return `<img src="./img/queued.png" alt="queued">`;
}

function getFailedImageTemplate() {
    return `<img src="./img/failed.png" alt="failed">`;
}

/* ----------------------- API Calls ----------------------------------------- */
function getMetrics(capture) {
    // adds required metrics field
    capture["metrics"] = ["CPUUtilization", "FreeStorageSpace", "WriteThroughput"];

    return $.ajax({
        url: "/cloudwatch/average",
        type: "POST",
        headers: { "Content-Type": "application/json" },
        // data: JSON.stringify(body)
        data: JSON.stringify(capture)
    });
}

function stopCapture(id) {
    var body = {
        id: id
    };
    
    return $.ajax({
        url: "/capture/stop",
        type: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        data: JSON.stringify(body)
    });
}

function stopReplay(id) {
    var body = {
        id: id
    };
    
    return $.ajax({
        url: "/replay/stop",
        type: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        data: JSON.stringify(body)
    });
}

function getCaptures() {
    return $.ajax({
        url: "/capture/status",
        type: "GET"
    });
}

function getReplays() {
    return $.ajax({
        url: "/replay/status",
        type: "GET"
    });
}

/* ----------------------- Utility ------------------------------------------- */
function openAnalysis(id) {
    sessionStorage.setItem("defaultCapture", id);
}

function sortRunningFirst(cap1, cap2) {
    if (cap1["status"] == "Running") return -1;
    if (cap2["status"] == "Running") return 1;
    return 0;
}

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

function getRndInteger(min, max) {
    return Math.floor(Math.random() * (max - min + 1) ) + min;
}

/* ----------------------- Testing ------------------------------------------- */
function testDashboardTable() {
    let testCaps = [];
    for (let i = 0; i < 6; i++) testCaps.push(makeRandomCapture("TestCapture_" + i));

    let testReps = [];
    for (let i = 0; i < 3; i++) testReps.push(makeRandomReplay("TestReplay_" + i));

    createCaptureDashboard(testCaps);
    createReplayDashboard(testReps);
}

function makeRandomCapture(id) {
    // all in millis
    const one_day = 1000 * 60 * 60 * 24;
    const now = new Date().getTime();

    // startTime is in range: now +/- half day
    let startTime = now + getRndInteger(0, one_day) - (one_day/2);
    let endTime = startTime + getRndInteger(0, one_day);

    let status = "";
    if (startTime < now) {
        status = "Running";
    }
    else if (startTime > now) {
        status = "Queued";
    }

    return {
        dbFileSize: getRndInteger(0, 200),
        endTime: endTime,
        fileSizeLimit: getRndInteger(0, 200),
        filterStatements: [],
        filterUsers: [],
        id: id,
        rds: "testdb",
        rdsRegion: "US_WEST_1",
        s3: "teamtitans-test-mycrt",
        s3Region: "US_WEST_1",
        startTime: startTime,
        status: status,
        transactionCount: getRndInteger(0, 200),
        transactionLimit: getRndInteger(0, 200)
    };
}

function makeRandomReplay(id) {
    // all in millis
    const one_day = 1000 * 60 * 60 * 24;
    const now = new Date().getTime();

    // startTime is in range: now +/- half day
    let startTime = now + getRndInteger(0, one_day) - (one_day/2);
    let endTime = startTime + getRndInteger(0, one_day);

    let status = "";
    if (startTime < now) {
        status = "Running";
    }
    else if (startTime > now) {
        status = "Queued";
    }

    return {
        captureId: "ExampleCaptureId",
        captureLogFileName: "test_capture-Workload.log",
        database: null,
        dbpassword: null,
        dburl: null,
        dbusername: null,
        endTime: endTime,
        filterStatements: [],
        filterUsers: [],
        id: id,
        rds: "testdb",
        rdsRegion: "US_WEST_1",
        replayType: getRndInteger(0, 1) ? "Time Sensitive" : "Fast Mode",
        s3: "teamtitans-test-mycrt",
        s3Region: "US_WEST_1",
        startTime: startTime,
        status: status,
        transactionLimit: 0
    };
}
