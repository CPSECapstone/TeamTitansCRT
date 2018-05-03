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
                <div class="dashboard-content"></div>
                ${insertLoadingSpinner("tableLoadingIcon")}
            </div>
        </div>
    </div>
    `);
    updateStatus();
    // testDashboardTable();
});

function testDashboardTable() {
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
    fillTable("tbody.capture-table", data);
}

function insertLoadingSpinner(selector) {
    return `
    <div class="${selector}" style="display: none;" tabindex="-1" role="dialog">
        <div class="text-center">Loading...</div>
        <div class="spinner"></div>
    </div>`;
}

function emptyDashboard() {
    return `
    <p class="text-center">You have no captures or replays currently running! Let's get started!</p>
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
            console.log("show");
            $(".tableLoadingIcon").show();
        },
        success: function(data) {
            setTimeout(function()
            {
                console.log("hide");
                $(".tableLoadingIcon").hide();
                if (data.length > 0) {
                    $("div.dashboard-content").replaceWith(captureDashboard(data));    
                    fillTable("tbody.capture-table", data);
                }
                else {
                    $("div.dashboard-content").replaceWith(emptyDashboard());    
                }
            },
            500);
        },
        error: function(err) {
            console.log(err);
            console.log("Error updating the status.")
            $(".tableLoadingIcon").hide();
        }
    });
}

function captureDashboard(data) {
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

function fillTable(selector, data) {
    $(selector).empty();
    data.map(createTableRow);
}


function createTableRow(capture) {
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
    
    createRow(id, rds, rdsRegion, status, startTimeMilli, startTime, endTimeMilli, endTime);
    var id = capture["id"];
    $(`a#stopButton${id}`).on("click", function() {
        stopCapture(id);
        updateStatus();
    });
}

function createRow(id, rds, rdsRegion, status, startTimeMilli, startTime, endTimeMilli, endTime) {
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
    
    $.ajax({
         url: "/cloudwatch/average",
        type: "POST",
        headers: { "Content-Type": "application/json" },
        data: JSON.stringify(body),
        success: function(data) {
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
            addRowToHtml(row, id);
            },
        error: function(err) {
            addRowToHtml(row, id);
            console.log(err);
        }
    });
}

function addRowToHtml(row, id) {
    $("tbody.capture-table").append(row);
        
    $(`#stopButton${id}`).on("click", function() {
        stopCapture(id);
        updateStatus();
    });
    
    $(`#${id}-analyze`).on("click", function() {
        openAnalysis(id);
    });
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

function openAnalysis(id) {
    sessionStorage.setItem("defaultCapture", id);
}
