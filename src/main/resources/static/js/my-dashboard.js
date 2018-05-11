$(function() {
    $("div.content-placeholder").replaceWith(mainTemplate());
    $("#toggle-btn").on("click", function() {
        toggleDashboard();
    });
    // testDashboardTable();
    updateStatus();
    $("div.replay-row").hide();
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
            $("div.capture-dashboard").replaceWith(captureDashboardTemplate());    
            $("tbody.capture-table").empty();

            // sort running captures first
            data.sort(sortRunningFirst);

            var rows = "";
            for(let i = 0; i < data.length; i++) {
                var capture = data[i];
                var id = capture["id"];

                rows += createTableRow(capture);
                rows += `<tr class="collapse" id="${selectorCloudwatch(id)}"></tr>`;

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

            $("tbody.capture-table").append(rows);

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
            $("div.capture-dashboard").replaceWith(captureDashboardEmptyTemplate());
        }
    },
    500);
}

function createReplayDashboard(data) {
    setTimeout(function()
    {
        if (data.length > 0) {
            $("div.replay-dashboard").replaceWith(replayDashboardTemplate());    
            $("tbody.replay-table").empty();

            // sort running replays first
            data.sort(sortRunningFirst);

            var rows = "";
            for(let i = 0; i < data.length; i++) {
                rows += createTableRow(data[i]);
            }

            $("tbody.replay-table").append(rows);

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
            $("div.replay-dashboard").replaceWith(replayDashboardEmptyTemplate());
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
        icon = `<img src="../img/running.png" alt="running">`;
    }                
    else if (status == "Finished") {
        btn = `<a href="analyze" id="${selectorAnalyze(id)}" class="defaultLinkColor">Analyze</a>`;
        icon = `<img src="../img/finished.png" alt="finished">`;
    }
    else if (status == "Queued") {
        icon = `<img src="../img/queued.png" alt="queued">`;
    }
    else {
        icon = `<img src="../img/failed.png" alt="failed">`;
    }

    var row = `
    <tr data-toggle="collapse" data-target="#${selectorCloudwatch(id)}" class="clickable">
        <td width="(100/12)%">${icon}</td>
        <td width="(100/4)%">${id}</td>
        <td width="(100/6)%">${status}</td>
        <td width="(100/6)%">${startTime}</td>
        <td width="(100/6)%">${endTime}</td>
        <td width="(100/6)%">${btn}</td>
    </tr>`;

    return row;
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

function captureDashboardEmptyTemplate() {
    return `
    <p class="text-center">You have no captures currently running! Let's get started!</p>
    <div class="text-center">
        <a href="capture" class="btn btn-default">Start new capture</a>
    </div>`;
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

/* ----------------------- API Calls ----------------------------------------- */
function getMetrics(capture) {
    // adds required metrics field
    capture["metrics"] = ["CPUUtilization", "FreeStorageSpace", "WriteThroughput"];
    console.log(capture);

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

/* ----------------------- Testing ------------------------------------------- */
function testDashboardTable() {
    var testCaps = [
    {
        dbFileSize: 0,
        endTime: 1525371692181,
        fileSizeLimit: 0,
        filterStatements: [],
        filterUsers: [],
        id: "test_capture",
        rds: "testdb",
        rdsRegion: "US_WEST_1",
        s3: "teamtitans-test-mycrt",
        s3Region: "US_WEST_1",
        startTime: 1525371672121,
        status: "Running",
        transactionCount: 50,
        transactionLimit: 0
    },
    {
        dbFileSize: 0,
        endTime: 1525371692181,
        fileSizeLimit: 0,
        filterStatements: [],
        filterUsers: [],
        id: "test_capture_2",
        rds: "testdb",
        rdsRegion: "US_WEST_1",
        s3: "teamtitans-test-mycrt",
        s3Region: "US_WEST_1",
        startTime: 1525371672121,
        status: "Queued",
        transactionCount: 50,
        transactionLimit: 0
    },
    {
        dbFileSize: 0,
        endTime: 1525371692181,
        fileSizeLimit: 0,
        filterStatements: [],
        filterUsers: [],
        id: "test_capture_3",
        rds: "testdb",
        rdsRegion: "US_WEST_1",
        s3: "teamtitans-test-mycrt",
        s3Region: "US_WEST_1",
        startTime: 1525371672121,
        status: "Running",
        transactionCount: 50,
        transactionLimit: 0
    },
    {
        dbFileSize: 0,
        endTime: 1525371692181,
        fileSizeLimit: 0,
        filterStatements: [],
        filterUsers: [],
        id: "test_capture_4",
        rds: "testdb",
        rdsRegion: "US_WEST_1",
        s3: "teamtitans-test-mycrt",
        s3Region: "US_WEST_1",
        startTime: 1525371672121,
        status: "Queued",
        transactionCount: 50,
        transactionLimit: 0
    }];
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
    }];
    createCaptureDashboard(testCaps);
    createReplayDashboard(testReps);
}
