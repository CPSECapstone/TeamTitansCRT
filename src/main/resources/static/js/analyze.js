$(document).ready(function() {
    $("#btnGetMetrics").on("click", function() {
        var body = {
            id: $("#txtID").val(),
            s3: $("#txtS3").val()
        };

        $.ajax({
            url: "/analysis",
            type: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            data: JSON.stringify(body),
            dataType: 'json',
            success: function(data) {
                var metricData = []
                for (var index = 0; index < data.length; index++) {
                    var metricObj = data[index];
                    var metric = metricObj["Metric"];
                    var dataPoints = metricObj["DataPoints"];

                    var points = []
                    for (var i = 0; i < dataPoints.length; i++) {
                        var point = dataPoints[i];
                        points.push([point["Timestamp"], point["Average"]]);
                    }
                    metricData.push({name: metric, data: points.sort(compareTimes)});
                }
                createChart(metricData);
            },
            error: function(err) {
                console.log(err);
            }
        });
    });
});

function compareTimes(a,b) {
  if (a[0] < b[0])
    return -1;
  if (a[0] > b[0])
    return 1;
  return 0;
}

function createChart(data) {
    Highcharts.chart('container', {
        chart: {
            type: 'spline',
            zoomType: 'x'
        },
        title: {
            text: 'Performance Metrics'
        },
        xAxis: {
            type: 'datetime',
            dateTimeLabelFormats: {
                hour: '%I %p',
                minute: '%I:%M %p'
            },
            title: {
                text: 'Time'
            }
        },
        yAxis: {
            title: {
                text: 'Percent Usage'
            },
            min: 0
        },
        tooltip: {
            headerFormat: '<b>{series.name}</b><br>',
            pointFormat: '{point.x:%I:%M %p} - {point.y:.2f}%'
        },

        plotOptions: {
            spline: {
                marker: {
                    enabled: false
                }
            }
        },

        series: data
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

Highcharts.setOptions({
    global: {
        useUTC: false
    }
});