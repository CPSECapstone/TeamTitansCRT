function convert(minutes) {
    return minutes * 60000; // TODO fix hardcoded conversion
}

function getRand(min, max) {
    return Math.floor(Math.random() * (max - min)) + min;
}

function generateData(arr) {
    var now = new Date();
    for (var d = new Date(); d <= now.getTime() + convert(90); d.setTime(d.getTime() +
            convert(10))) {
        arr.push([d, getRand(40, 80)]);
    }
}

var cpuData = [];
var ramData = [];
var diskData = [];
generateData(cpuData);
generateData(ramData);
generateData(diskData);

Highcharts.chart('container', {
    chart: {
        type: 'spline'
    },
    title: {
        text: 'Performance Metrics'
    },
    subtitle: {
        text: '10 minute interval'
    },
    xAxis: {
        type: 'datetime',
        dateTimeLabelFormats: {
            hour: '%I %p',
            minute: '%I:%M %p'
        },
        title: {
            text: 'Date'
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
        pointFormat: '{point.x:%e. %b}: {point.y:.2f} m'
    },

    plotOptions: {
        spline: {
            marker: {
                enabled: false
            }
        }
    },

    series: [{
        name: 'CPU',
        data: cpuData
    }, {
        name: 'RAM',
        data: ramData
    }, {
        name: 'Disk I/O',
        data: diskData
    }]
});

// builds an html string, then injects it
var dataString = "";
for (var i = 0; i < cpuData.length; i++) {
    var time = cpuData[i][0].toLocaleTimeString();
    var cpu = cpuData[i][1];
    var ram = ramData[i][1];
    var disk = diskData[i][1];

    dataString = dataString.concat(
        "<tr><td>" + time + "</td><td>" + cpu + "</td><td>" + ram +
        "</td><td>" + disk + "</td></tr>");
}

$('#captureTable > tbody').append(dataString);
