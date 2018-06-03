var defaultMetric = "CPUUtilization"; // Default metric to use when displaying graph
var metricData = {}; // Metric data used in graph
var metricsSelector = "MetricsModal";

$(function() {
    $("div.content-placeholder").replaceWith(`
    <div class="container">
        <div class="row">
            <div class="col-lg-6 col-lg-offset-3">
                <h4 class="text-center">Compare Results</h4>
                <p class="text-center">Select a Capture and Replays to Compare</p>
            
                <div> 
                    <a href="javascript:void(0)" id="btnGetMetrics" class="btn btn-default center-block" data-toggle="modal" data-target="#${metricsSelector}">Get Metrics</a>      
                </div>
            </div>
        </div>

        <div class="row">
            <div class="col-lg-6 start-capture-form border-on-right">
                <p class=""><strong>Captures</strong></p>
                <hr />
               <div id="captureTable" style="height:500px;overflow:auto;">
                    <table class="table table-striped table-hover">
                        <col width="30">
                        <tr>
                            <th></th>
                            <th>ID</th>
                            <th>RDS</th>
                            <th>Status</th>
                        </tr>
                    </table>
                </div>                
            </div>
            <div class="col-lg-6">
                <p class=""><strong>Replays</strong></p>
                <hr />
                <div id="replayTable" style="height:500px;overflow:auto;">
                    <table class="table table-striped table-hover">
                        <col width="30">
                        <tr>
                            <th></th>
                            <th>ID</th>
                            <th>CaptureID</th>
                            <th>RDS</th>
                            <th>Status</th>
                            <th>Type</th>
                        </tr>
                    </table>
                </div>
            </div>
            
            
            <div id="${metricsSelector}" class="modal fade" role="dialog">
                ${createMetricsModal(metricsSelector)}
            </div>    
        </div>              
    </div>
    `);
});

function testCaptureList() {
    var data = [
        {
            id: "Running",
            startTime: 1520871274784,
            endTime: null,
            fileSizeLimit: 420,
            transactionLimit: 840,
            status: "Running"
        },
        {
            id: "Queued",
            startTime: 1520871274784,
            endTime: null,
            fileSizeLimit: 420,
            transactionLimit: 840,
            status: "Queued"
        },
        {
            id: "Finished",
            startTime: 1520871274784,
            endTime: null,
            fileSizeLimit: 420,
            transactionLimit: 840,
            status: "Finished"
        },
        {
            id: "Failed",
            startTime: 1520871274784,
            endTime: 1520881274784,
            fileSizeLimit: 420,
            transactionLimit: 840,
            status: "Failed"
        }
    ]
    addAllToCaptureList(data);
}

//TODO: if no data stop loading
function insertLoadingSpinner(selector) {
    return `
    <div class="${selector}" tabindex="-1" role="dialog">
        <div class="text-center">Loading...</div>
        <div class="spinner"></div>
    </div>`;
}

/**
 * Top level function for creating list of capture cards
 */
function updateCaptureList() {
    $.ajax({
        url: "/resource/captures",
        type: "GET",
        beforeSend: function() {
            $(".manageCapturesLoadingIcon").show();
        },
        success: function(data) {
            console.log(data);
            if (data.length > 0) {
                setTimeout(function()
                {
                    $(".manageCapturesLoadingIcon").hide();
                    addAllToCaptureList(data);
                },
                500);
            }
            else {
                $(".manageCapturesLoadingIcon").hide();
                $("#CaptureList").append(`<p>You have no capture history</p>`);
            }
        },
        error: function(err) {
            console.log(err);
            $(".manageCapturesLoadingIcon").hide();
        }
    });
}

function addAllToCaptureList(data) {
    // clears contents of the CaptureList
    $('#CaptureList').empty();
    data.map(addToCaptureList).join('')
}

/**
 * Takes a capture, adds it to the list of capture cards, and creates the save binding
 * @param {Capture}
 */
function addToCaptureList(capture) {
    $("#CaptureList").append(createEditCaptureModal(capture));

    var id = capture["id"];
    $(`#${id}-save`).on("click", function() {
        updateCapture(id);
    });

    $(`#${id}-analyze`).on("click", function() {
        openAnalysis(id);
    });

    // disables fields on finished or failed captures
    var status = capture["status"]
    if (status == 'Finished' || status == 'Failed') {
        $(`#${id}-modal input`).attr("readonly", true);
    }
    else {
        $(`#${id}-modal input`).attr("readonly", false);
    }
}

/**
 * Function that uses a template to create a card
 * @param  {Capture}
 * @return {string}
 */
function createEditCaptureModal(capture) {
    var id = capture["id"];
    var status = capture["status"];
    
    // to be fixed with user set timezone
    var startTime = new Date(capture["startTime"]);
    // startTime.setHours(startTime.getHours() - 7); // daylight savings lol
    startTime.setHours(startTime.getHours());
    startTime = startTime.toISOString().replace("Z", "");
    var endTime = capture["endTime"];
    if (endTime != null) {
        endTime = new Date(endTime);
        // endTime.setHours(endTime.getHours() - 7); // daylight savings lol
        endTime.setHours(endTime.getHours());
        endTime = endTime.toISOString().replace("Z", "");
    }
    else {
        endTime = "";
    }

    var fileSizeLimit = capture["fileSizeLimit"];
    var transactionLimit = capture["transactionLimit"];

    var footer = '';
    if (status == "Queued" || status == "Running") {
        footer += `<a id="${id}-save" class="btn btn-primary" data-dismiss="modal">Save</a>`;
    }
    else if (status == "Finished") {
        footer += `<a href="analyze" id="${id}-analyze" class="btn btn-default">Analyze</a>`
    }
    footer += `<a class="btn btn-secondary" data-dismiss="modal">Close</a>`;
                        

    return `
    ${createCaptureListItem(id, status, `${id}-modal`)}
    <div class="modal fade" id="${id}-modal" tabindex="-1" role="dialog">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">${id}  - ${status}</h5>
                </div>
                <div class="modal-body">
                    <label class="input-label">Start Time:
                        <input id="" class="txtStartTime form-control" type="datetime-local" value="${startTime}">
                    </label>
                    <label class="input-label">End Time:
                        <input id="" class="txtEndTime form-control" type="datetime-local" value="${endTime}">
                    </label>
                    ${createNumericInputValue("Max Capture Size (KB):", "txtMaxSize", fileSizeLimit)}
                    ${createNumericInputValue("Max Number of Transactions:", "txtMaxTrans", transactionLimit)}
                </div>
                <div class="modal-footer">
                    ${footer}
                </div>
            </div>
        </div>
    </div>`;
}


/**
 * Function that creates a modal with chart from capture and replay comparison
 * @param   
 * @return {string}
 */
function createMetricsModal(selector) {
    return `            
        <div class="modal-dialog" style="width:1250px;">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal">&times;</button>
                    <h4 class="modal-title">Metrics Comparison</h4>
                </div>
                <div class="modal-body">
                    ${insertLoadingSpinner("manageCapturesLoadingIcon")}
                    <div id="metricSelectorDiv"></div>
                    <div id="container"></div> 
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default gray" data-dismiss="modal">Close</button>
                </div>
            </div>
        </div>`;
}

// Requests capture metrics on button click
$(function() {
    $("#btnGetMetrics").on("click", function() {
        $(this).prop('disabled', true);

        metricData = {}; // Clears metric data

        var requests = []; // Stores all ajax requests

        var checkedCaptures = $('.captureCheckbox:checkbox:checked');
        var checkedReplays = $('.replayCheckbox:checkbox:checked');


        // Makes ajax request for each specified capture
        for (var i = 0; i < checkedCaptures.length; i++) {
            var body = {
                id: checkedCaptures[i].value,
                type: "Capture"
            };
            requests.push(requestMetrics(body));
        }

        // Makes ajax request for each specified replay
        for (var i = 0; i < checkedReplays.length; i++) {
            var body = {
                id: checkedReplays[i].value,
                type: "Replay"
            };
            requests.push(requestMetrics(body));
        }
        
        $(".manageCapturesLoadingIcon").show();
        
        if (checkedCaptures.length > 0 || checkedReplays.length > 0) {

            $(`#${metricsSelector}`).modal("show");

            // Draw the graph after all ajax calls complete
            $.when.apply(this, requests).done(function() {
                
                $(".manageCapturesLoadingIcon").hide();
                
                if (!jQuery.isEmptyObject(metricData)) {
                    createChart(metricData[defaultMetric])
                } else {
                    $('#container').html('<p>No metric data available for selected Capture(s)/Replay(s)</p>');
                }
                $(this).prop('disabled', false);
            });
        } else {
            $('#container').html('')
            $(this).prop('disabled', false);
        }
        return false; // Stops page from jumping to top
    });
});

// Function to get metric data using ajax call
function requestMetrics(body) {
    return $.ajax({
        url: "/analysis",
        type: "POST",
        headers: { "Content-Type": "application/json" },
        data: JSON.stringify(body),
        dataType: 'json',
        success: function(data) {
            var selector = "<div><select id='metricSelector' class='btn btn-secondary'>"; // html string to be injected

            // Parses through every metric returned
            for (var index = 0; index < data.length; index++) {
                var metricObj = data[index];
                var metric = metricObj["Metric"];
                var dataPoints = metricObj["DataPoints"].sort(compareTimes);

                selector += "<option value='" + metric +"'>" + metric + "</option>"; // Add selector option

                // Only handle metric if datapoints returned
                if (dataPoints.length > 0) {

                    var points = [];
                    var startTime = dataPoints[0]["Timestamp"];
                    var unit = dataPoints[0]["Unit"];

                    // Process all datapoints
                    for (var i = 0; i < dataPoints.length; i++) {
                        var point = dataPoints[i];
                        points.push([point["Timestamp"] - startTime, point["Average"]]);
                    }

                    // Create new metric entry, or add to existing one
                    if (metric in metricData) {
                        metricData[metric]['data'].push({name: body['id'], data: points});
                    } else {
                        metricData[metric] = {metric: metric, units: unit, data: [{name: body['id'], data: points}]};
                    }
                }
            }

            selector += "</select></div>";
            $("#metricSelectorDiv").html(selector); // Injects html string
            $('#metricSelector option[value="' + defaultMetric + '"]').prop("selected", "selected"); // Set selector to default
        },
        error: function(err) {
            console.log(err);
        }
    });
}

// Function to sort datapoints based on timestamp
function compareTimes(a,b) {
  return a["Timestamp"] - b["Timestamp"];
}

// Function to create highchart using the given data
function createChart(data) {
    if(typeof data === 'undefined') {
        $('#container').html('<p>No metric data available for selected Capture(s)/Replay(s)</p>');
        return;
    }

    var metric = data['metric'];
    var unit = data['units'];
    var dataPoints = data['data'];

    Highcharts.chart('container', {
        chart: {
            type: 'spline',
            zoomType: 'x'
        },
        title: {
            text: metric
        },
        xAxis: {
            type: 'datetime',
            dateTimeLabelFormats: {
                year: '%Hh:%Mm:%Ss',
                month: '%Hh:%Mm:%Ss',
                week: '%Hh:%Mm:%Ss',
                day: '%Hh:%Mm:%Ss',
                hour: '%Hh:%Mm:%Ss',
                minute: '%Hh:%Mm:%Ss',
                second: '%Mm:%Ss',
                millisecond: '%Mm:%Ss'
            },
            title: {
                text: 'Time'
            }
        },
        yAxis: {
            title: {
                text: unit + ' Usage'
            },
            min: 0
        },
        tooltip: {
            headerFormat: "{point.x:%Hh:%Mm:%Ss}",
            pointFormat: '<span style="color:{series.color}">{series.name}</span>: <b>{point.y:.2f} ' + (unit == 'Percent' ? '%' : unit) + '</b><br/>',
            split: true
        },
        plotOptions: {
            spline: {
                marker: {
                    enabled: false
                }
            }
        },
        series: dataPoints
    });
}

// Changes graph when new metric selected
$(function() {
    $('body').on('change', '#metricSelector' , function() {
        if(!jQuery.isEmptyObject(metricData)) {
            createChart(metricData[this.value]); // Updates chart
        }
        defaultMetric = this.value; // Updates default to new metric
    });
});

// Populate capture table with available captures
$(function() {

    var selected = sessionStorage.getItem("defaultCapture");
    sessionStorage.removeItem("defaultCapture");

    $.ajax({
        url: "/resource/captures",
        type: "GET",
        success: function(data) {
            var table = '<table class="table table-striped table-hover"><col width="40">'
            table += '<tr>' +
                '<th></th>' +
                '<th>ID</th>' +
                '<th>RDS</th>' +
                '<th>Status</th>' +
                '</tr>';

            for (var i = 0; i < data.length; i++) {
                var log = data[i];
                var startTime = log['startTime'] == null ? 'N/A'  : new Date(log['startTime']);
                var endTime = log['endTime'] == null ? 'N/A'  : new Date(log['endTime']);

                var disabled = log['status'] == 'Failed' || log['status'] == 'Queued' ? 'disabled' : '';
                var checked = log['id'] == selected ? 'checked' : ''

                if (disabled) {
                    continue;
                }

                table += '<tr>' +
                    '<td><label><input class="captureCheckbox" type="checkbox" value="' + log['id'] + '"' + disabled + ' ' + checked + '></label></td>' +
                    '<td>' + log['id'] + '</td>' +
                    '<td>' + log['rds'] + '</td>' +
                    '<td>' + log['status'] + '</td>' +
                    '</tr>';
            }
            $('#captureTable').html(table);

            if (selected != null) {
                $("#btnGetMetrics").trigger( "click" );
            }
        },
        error: function(err) {
            console.log(err);
        }
    });
});

// Populate replay table with available replays
$(function() {

    var selected = sessionStorage.getItem("defaultReplay");
    sessionStorage.removeItem("defaultReplay");

    $.ajax({
        url: "/resource/replays",
        type: "GET",
        success: function(data) {
            var table = '<table class="table table-striped table-hover"><col width="40">'
            table += '<tr>' +
                '<th></th>' +
                '<th>ID</th>' +
                '<th>CaptureID</th>' +
                '<th>RDS</th>' +
                '<th>Status</th>' +
                '<th>Type</th>' +
                '</tr>';

            for (var i = 0; i < data.length; i++) {
                var log = data[i];
                var startTime = log['startTime'] == null ? 'N/A'  : new Date(log['startTime']);
                var endTime = log['endTime'] == null ? 'N/A'  : new Date(log['endTime']);

                var disabled = log['status'] == 'Failed' || log['status'] == 'Queued' ? 'disabled' : '';
                var checked = log['id'] == selected ? 'checked' : ''

                if (disabled) {
                    continue;
                }
                
                var idReplay = log['id'];
                if (idReplay.length > 12) {
                    var strR = idReplay.substr(0, 12);
                    idReplay = strR.concat("...");
                }
                
                var idCapture = log['captureId']
                if (idCapture.length > 12) {
                    var strC = idCapture.substr(0, 12);
                    idCapture = strC.concat("...");
                }
                
                table += '<tr>' +
                    '<td><label><input class="replayCheckbox" type="checkbox" value="' + log['id'] + '"' + disabled + ' ' + checked + '></label></td>' +
                    '<td class="CellWithComment">' + idReplay + '<span class="CellComment">' + log['id'] + '</span></td>' +
                    '<td class="CellWithComment">' + idCapture + '<span class="CellComment">' + log['captureId'] + '</span></td>' +
                    '<td>' + log['rds'] + '</td>' +
                    '<td>' + log['status'] + '</td>' +
                    '<td>' + log['replayType'] + '</td>' +
                    '</tr>';
            }
            $('#replayTable').html(table);

            if (selected != null) {
                $("#btnGetMetrics").trigger( "click" );
            }
        },
        error: function(err) {
            console.log(err);
        }
    });
});

function filterTables() {
    var checkedCaptures = $('.captureCheckbox:checkbox:checked').map(function () {return this.value;});
    var checkedReplays = $('.replayCheckbox:checkbox:checked').map(function () {return $(this).closest('tr').children().eq(2).find("span").text();});

    var checked = checkedCaptures

    for (var i = 0; i < checkedReplays.length; i++) {
        if(jQuery.inArray(checkedReplays[i], checked) === -1) {
            checked.push(checkedReplays[i])
        }
    }

    $("#replayTable tr td:nth-child(3)").each(function() {
        if(jQuery.inArray($(this).find("span").text(), checked) !== -1 || checked.length == 0) {
            $(this).parent('tr').show();
        } else {
            $(this).parent('tr').hide();
            $(this).parent('tr').find('.replayCheckbox').prop('checked', false);
        }
    });

    $("#captureTable tr td:nth-child(2)").each(function() {
        if(jQuery.inArray($(this).text(), checked) !== -1 || checked.length == 0) {
            $(this).parent('tr').show();
        } else {
            $(this).parent('tr').hide();
            $(this).parent('tr').find('.captureCheckbox').prop('checked', false);
        }
    });
}

$('body').on('change', '.captureCheckbox', function() {
    filterTables()
});

$('body').on('change', '.replayCheckbox', function() {
    filterTables()
});