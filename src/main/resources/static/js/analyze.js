var defaultMetric = "CPUUtilization"; // Default metric to use when displaying graph
var metricData = {}; // Metric data used in graph

// Requests capture metrics on button click
$(function() {
    $("#btnGetMetrics").on("click", function() {

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

        if (checkedCaptures.length > 0 || checkedReplays.length > 0) {
            // Draw the graph after all ajax calls complete
            $.when.apply(this, requests).done(function() {
                if (!jQuery.isEmptyObject(metricData)) {
                    createChart(metricData[defaultMetric])
                } else {
                    $('#container').html('<p>No metric data available for selected Capture(s)/Replay(s)</p>');
                }
            });
        } else {
            $('#container').html('')
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
                '<th>S3</th>' +
                '<th>Status</th>' +
                '<th>Start</th>' +
                '<th>End</th>' +
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
                    '<td>' + log['s3'] + '</td>' +
                    '<td>' + log['status'] + '</td>' +
                    '<td>' + startTime + '</td>' +
                    '<td>' + endTime + '</td>' +
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
                '<th>S3</th>' +
                '<th>Status</th>' +
                '<th>Type</th>' +
                '<th>Start</th>' +
                '<th>End</th>' +
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
                    '<td><label><input class="replayCheckbox" type="checkbox" value="' + log['id'] + '"' + disabled + ' ' + checked + '></label></td>' +
                    '<td>' + log['id'] + '</td>' +
                    '<td>' + log['captureId'] + '</td>' +
                    '<td>' + log['rds'] + '</td>' +
                    '<td>' + log['s3'] + '</td>' +
                    '<td>' + log['status'] + '</td>' +
                    '<td>' + log['replayType'] + '</td>' +
                    '<td>' + startTime + '</td>' +
                    '<td>' + endTime + '</td>' +
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

$('body').on('change', '.captureCheckbox', function() {
    var checkedCaptures = $('.captureCheckbox:checkbox:checked').map(function () {return this.value;});

    $("#replayTable tr td:nth-child(3)").each(function() {
        if(jQuery.inArray($(this).text(), checkedCaptures) !== -1 || checkedCaptures.length == 0) {
            $(this).parent('tr').show();
        } else {
            $(this).parent('tr').hide();
        }
    });
});