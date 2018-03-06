$(function() {
    updateCaptureList();
});

/**
 * Top level function for creating list of capture cards
 */
function updateCaptureList() {
    $.ajax({
        url: "/capture/status",
        type: "GET",
        success: function(data) {
            $('#accordion').html("");
            for (var i = 0; i < data.length; i++) {
                var capture = data[i];
                addToCaptureList(capture);
            }
        },
        error: function(err) {
            console.log(err);
        }
    });
}

/**
 * Takes a capture, adds it to the list of capture cards, and creates the save binding
 * @param {Capture}
 */
function addToCaptureList(capture) {
    $("#accordion").append(createCaptureCard(capture));

    var id = capture["id"];
    $("#" + id + " .save").on("click", function() {
        updateCapture(id);
    });
}

/**
 * Function that uses a template to create a card
 * @param  {Capture}
 * @return {string}
 */
function createCaptureCard(capture) {
    var id = capture["id"];
    var status = capture["status"];
    var startTime = new Date(capture["startTime"]);
    startTime.setHours(startTime.getHours() - 8);
    startTime = startTime.toISOString().replace("Z", "");
    var endTime = new Date(capture["endTime"]);
    endTime.setHours(endTime.getHours() - 8);
    endTime = endTime.toISOString().replace("Z", "");
    var fileSizeLimit = capture["fileSizeLimit"];
    var transactionLimit = capture["transactionLimit"];
    return `
        <div class="card">
            <div class="card-header" role="tab">
                <h5 class="mb-0">
                    <a href="#${id}" data-toggle="collapse" data-parent="#accordion">${id} - <small>${status}</small></a>
                </h5>
            </div>
            <div class="collapse" id="${id}" role="tabpanel">
                <div class="card-block">
                    <label for="" class="input-label">Start Time:
                        <input type="datetime-local" class="txtStartTime form-control" value="${startTime}"/>
                    </label>
                    <label for="" class="input-label">End Time:
                        <input type="datetime-local" class="txtEndTime form-control" value="${endTime}"/>
                    </label>
                    <label for="" class="input-label">Max Capture Size (mB):
                        <input type="text" class="txtMaxSize form-control" value="${fileSizeLimit}"/>
                    </label>
                    <label for="" class="input-label">Max Number of Transactions:
                        <input type="text" class="txtMaxTrans form-control" value="${transactionLimit}"/>
                    </label>
                    <a href="javascript:void(0)" class="btn btn-sm btn-default save">Save</a>
                </div>
            </div>
        </div> `
}

/**
 * Takes a capture id and send the update request to the backend
 * @param  {string}
 */
function updateCapture(id) {
    var body = {
        id: id,
        startTime: $("#" + id + " .txtStartTime").val(),
        endTime: $("#" + id + " .txtEndTime").val(),
        fileSizeLimit: $("#" + id + " .txtMaxSize").val(),
        transactionLimit: $("#" + id + " .txtMaxTrans").val()
    };
    
    $.ajax({
        url: "/capture/update",
        type: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        data: JSON.stringify(body),
        success: function() {
            console.log(id + " success")
            updateCaptureList();
        },
        error: function(err) {
            $("#lblStatus").html("Startup failure.");
            console.log(err);
        }
    });
}