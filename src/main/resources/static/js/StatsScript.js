var domain = window.location.href;
    if (domain.includes("8080")) {
        domain = "http://localhost:8080";
    } else {
        domain = "https://cp-fellowship.herokuapp.com";
    }
	window.onload = function () {

		// initial values of dataPoints
		var dps = [
		{label: "Cal Poly Users", y: 0}	,
		{label: "MUAS Users", y: 0},
		{label: "Total Students", y: 0},
		{label: "Total Posts", y: 0},
		];
        var totalUsers = "Total Users: 0";

		var chart = new CanvasJS.Chart("chartContainer",{
			theme: "theme2",
			title:{
				text: "The Fellowship Statistics"
			},
			axisY: {
			},
			legend:{
				verticalAlign: "top",
				horizontalAlign: "centre",
				fontSize: 18

			},
			data : [{
				type: "column",
				showInLegend: true,
				legendMarkerType: "none",
				legendText: totalUsers,
				indexLabel: "{y}",
				dataPoints: dps
			}]
		});

		// renders initial chart
		chart.render();

		var updateInterval = 2000;  // milliseconds

		var updateUsers = function (info) {
			// updating legend text.
			totalUsers = "Total Users: " + info;
			chart.options.data[0].legendText = totalUsers;
		};

		var updatePoly = function (info) {
            dps[0].y = info;
        };

        var updateStudents = function (info) {
            dps[2].y = info;
        };

        var updatePosts = function (info) {
            dps[3].y = info;
        };

		var updateTotalUsers = function(){
		    $.ajax({
                url: domain + "/stats/totalUsers",
                type: "GET",
                dataType: "json",
                success: function(data) {
                    updateUsers(data);
                },
                error: function(err) {
                    console.log(err);
                }
            });
		};

		var updatePolyUsers = function(){
            $.ajax({
                url: domain + "/stats/totalPolyUsers",
                type: "GET",
                dataType: "json",
                success: function(data) {
                    updatePoly(data);
                },
                error: function(err) {
                    console.log(err);
                }
            });
        };

        var updateStudentUsers = function(){
            $.ajax({
                url: domain + "/stats/totalStudents",
                type: "GET",
                dataType: "json",
                success: function(data) {
                    updateStudents(data);
                },
                error: function(err) {
                    console.log(err);
                }
            });
        };

        var updateTotalPosts = function(){
            $.ajax({
                url: domain + "/stats/totalPosts",
                type: "GET",
                dataType: "json",
                success: function(data) {
                    updatePosts(data);
                },
                error: function(err) {
                    console.log(err);
                }
            });
        };

		// update chart after specified interval
		setInterval(function(){
		    updatePolyUsers();
		    updateTotalPosts();
		    updateTotalUsers();
		    updateStudentUsers();
		    chart.render();
		}, updateInterval);


		//Tags Per Post Table
		getTags();
    }

function TagLoop(array_tags){

    for(i = 0; i < array_tags.length; i++){
        getPostPerTag(array_tags[i]);
    }
}

function AddTagTableRow(tagName, numPosts){

    var tableRef = document.getElementById('myTable').getElementsByTagName('tbody')[0];

    // Insert a row in the end of the table
    var newRow   = tableRef.insertRow(tableRef.rows.length);

    // Insert a cell in the row at index 0
    var newCell  = newRow.insertCell(0);

    // Append a text node to the cell
    var newText  = document.createTextNode(tagName + ': ' + numPosts)
    newCell.appendChild(newText);
}

function getTags(){
    $.ajax({
        url: domain + "/stats/tagNames",
        type: "GET",
        dataType: "json",
        success: function(data) {
            TagLoop(data);
        },
        error: function(err) {
        console.log(err);
        }
    });
};

function getPostPerTag(tagName){
    $.ajax({
        url: domain + "/stats/" + tagName,
        type: "GET",
        dataType: "json",
        success: function(data) {
            AddTagTableRow(tagName, data);
        },
        error: function(err) {
        console.log(err);
        }
    });
};