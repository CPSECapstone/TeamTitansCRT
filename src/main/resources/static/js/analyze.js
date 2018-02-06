var defaultMetric = "CPUUtilization"
var metricData = {};

$(document).ready(function() {
    $("#btnGetMetrics").on("click", function() {

        metricData = {};

        var requests = []

        for (var i = 1; i <= $('#captureSelector').val(); i++) {
            var body = {
                id: $("#txtID-" + i).val(),
                s3: $("#txtS3-" + i).val()
            };
            requests.push(requestMetrics(body))
        }
        $.when.apply(this, requests).done(function() {createChart(metricData[defaultMetric])});
        return false
    });
});

function requestMetrics(body) {
    return $.ajax({
        url: "/analysis",
        type: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        data: JSON.stringify(body),
        dataType: 'json',
        success: function(data) {

            var selector = "<div><select id='metricSelector' class='btn btn-secondary'>";
            for (var index = 0; index < data.length; index++) {
                var metricObj = data[index];
                var metric = metricObj["Metric"];
                var dataPoints = metricObj["DataPoints"].sort(compareTimes);
                var unit = ""

                if (dataPoints.length > 0) {
                    var points = [];
                    var startTime = dataPoints[0]["Timestamp"]
                    for (var i = 0; i < dataPoints.length; i++) {
                        var point = dataPoints[i];
                        points.push([point["Timestamp"] - startTime, point["Average"]]);
                    }

                    unit = dataPoints[0]["Unit"]
                    selector += "<option value='" + metric +"'>" + metric + "</option>";

                    if (metric in metricData) {
                        metricData[metric]['data'].push({name: body['id'], data: points})
                    } else {
                        metricData[metric] = {metric: metric, units: unit, data: [{name: body['id'], data: points}]};
                    }
                }
            }

            selector += "</select></div>";
            $("#metricSelectorDiv").html(selector);
            $('#metricSelector option[value="' + defaultMetric + '"]').prop("selected", "selected");
        },
        error: function(err) {
            console.log(err);
        }
    });
}

function compareTimes(a,b) {
  return a["Timestamp"] - b["Timestamp"]
}

function createChart(data) {
    var metric = data['metric']
    var unit = data['units']
    var dataPoints = data['data']

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

function createTable(data) {
    // builds an html string, then injects it
    var dataString = "";
    for (var i = 0; i < data.length; i++) {
        var time = data[i][0].toLocaleTimeString();
        var metric = data[i][1];

        dataString = dataString.concat(
            "<tr><td>" + time + "</td><td>" + metric + "</td></tr>");
    }

    $('#captureTable > tbody').append(dataString);
}

$('body').on('change', '#metricSelector' , function() {
    createChart(metricData[this.value]);
    defaultMetric = this.value
})

$('#captureSelector').on('change', function() {
    var captureInputs = ""
    for (var i = 1; i <= this.value; i++) {
        captureInputs += '<div class="input">'
        captureInputs += '<label class="input-label">Capture' + i + ' ID:<input id="txtID-' + i + '" class="form-control" type="text" value="MyCapture' + i + '"></label>'
        captureInputs += '</div>'
        captureInputs += '<div class="input">'
        captureInputs += '<label class="input-label">S3 Endpoint:<input id="txtS3-' + i + '" class="form-control" type="text" value="teamtitans-test-mycrt"></label>'
        captureInputs += '</div>'
    }
    $('#captureInputs').html(captureInputs)
});