$(function() {
    var idSelector = "idSelector";
    var rdsSelector = "rdsSelector";
    var s3Selector = "s3Selector";
    var startTimeSelector = "startTimeSelector";
    var endTimeSelector = "endTimeSelector";
    var fileSizeLimitSelector = "fileSizeLimitSelector";
    var transactionLimitSelector = "transactionLimitSelector";
    var filterStatementsSelector = "filterStatementsSelector";
    var filterUsersSelector = "filterUsersSelector";

    var startBtnSelector = "btnCaptureStart";

    $("div.content-placeholder").replaceWith(`
    <div class="container">
        <div class="row">
            <div class="col-lg-6 col-lg-offset-3">
                <h4 class="text-center">My Captures</h4>
                <p class="text-center">Add page description</p>
            </div>
        </div>
        <div class="row">
            <div class="col-lg-6 start-capture-form border-on-right">
                <p class=""><strong>Start a Capture</strong></p>
                <hr />
                <div class="startCaptureLoadingIcon" tabindex="-1" role="dialog"><div class="spinner"></div></div>
                <div class="${rdsSelector}"></div>
                <div class="${s3Selector}"></div>
                ${createTextInput("Capture ID:", idSelector)}

                <div class="block">
                    <a data-toggle="collapse" href="#advanced">Advanced <span class="caret"></span></a>
                </div>                

                <div id="advanced" class="collapse">
                    <label class="input-label">Start Time:
                        <input id="${startTimeSelector}" class="form-control" type="datetime-local" value="">
                    </label>
                    <label class="input-label">End Time:
                        <input id="${endTimeSelector}" class="form-control" type="datetime-local" value="">
                    </label>
                    ${createTextInput("Max Capture Size (mB):", fileSizeLimitSelector)}
                    ${createTextInput("Max Number of Transactions:", transactionLimitSelector)}
                    ${createTextInput("Database Commands to Ignore (comma delimited):", filterStatementsSelector)}
                    ${createTextInput("Database Users to Ignore (comma delimited):", filterUsersSelector)}
                </div>
                <a href="javascript:void(0)" id="${startBtnSelector}" class="btn btn-default">Start Capture</a>
            </div>
            <div class="col-lg-6">
                <p class=""><strong>Manage Captures</strong></p>
                <hr />
                <div class="manageCapturesLoadingIcon" tabindex="-1" role="dialog"><div class="spinner"></div></div>
                <ul id="CaptureList" class="list-group"></ul>
            </div>
        </div>
    </div>
    `);
    //updateCaptureList();
    
    var data = [
        {
            id: "Test1",
            startTime: 10000000,
            endTime: 10000000,
            fileSizeLimit: 420,
            transactionLimit: 840,
        },
        {
            id: "Test2",
            startTime: 10000000,
            endTime: 10000000,
            fileSizeLimit: 420,
            transactionLimit: 840,
        }
    ]
    addAllToCaptureList(data);
    

    populateRDSDropdown(rdsSelector);
    populateS3Dropdown(s3Selector);
    $(`#${startBtnSelector}`).on("click", function() {
        var startTime = null;
        if ($(`#${startTimeSelector}`).val()) {
            startTime = new Date(String($(`#${startTimeSelector}`).val()));
        }

        var endTime = null;
        if ($(`#${endTimeSelector}`).val()) {
            endTime = new Date(String($(`#${endTimeSelector}`).val()));
        }

        // Only start capture if rds and s3 selected
        if ($(`#${rdsSelector}`).val() != '' && $(`#${s3Selector}`).val() != '') {
            var capture = {
                id: $(`#${idSelector}`).val(),
                rds: $(`#${rdsSelector}`).val(),
                s3: $(`#${s3Selector}`).val(),
                startTime: startTime,
                endTime: endTime,
                fileSizeLimit: $(`#${fileSizeLimitSelector}`).val(),
                transactionLimit: $(`#${transactionLimitSelector}`).val(),
                filterStatements: $(`#${filterStatementsSelector}`).val().split(',').map(x => x.trim()),
                filterUsers: $(`#${filterUsersSelector}`).val().split(',').map(x => x.trim())
            };

            startCapture(capture);
        }
    });
});

/**
 * Top level function for creating list of capture cards
 */
function updateCaptureList() {
    $.ajax({
        url: "/capture/status",
        type: "GET",
        beforeSend: function() {
            $(".manageCapturesLoadingIcon").show();
        },
        complete: function() {
            $(".manageCapturesLoadingIcon").hide();
        },
        success: function(data) {
            console.log(data);
            addAllToCaptureList(data);
            $(".manageCapturesLoadingIcon").hide();
        },
        error: function(err) {
            console.log(err);
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
    startTime.setHours(startTime.getHours() - 8);
    startTime = startTime.toISOString().replace("Z", "");
    var endTime = new Date(capture["endTime"]); // TODO: If there is no endtime, display nothing
    endTime.setHours(endTime.getHours() - 8);
    endTime = endTime.toISOString().replace("Z", "");

    var fileSizeLimit = capture["fileSizeLimit"];
    var transactionLimit = capture["transactionLimit"];
    return `
    ${createCaptureListItem(id, `${id}-modal`)}
    <div class="modal fade" id="${id}-modal" tabindex="-1" role="dialog">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">${id}</h5>
                </div>
                <div class="modal-body">
                    <label class="input-label">Start Time:
                        <input id="${startTimeSelector}" class="form-control" type="datetime-local" value="${startTime}">
                    </label>
                    <label class="input-label">End Time:
                        <input id="${endTimeSelector}" class="form-control" type="datetime-local" value="${endTime}">
                    </label>
                    ${createTextInputValue("Max Capture Size (mB):", fileSizeLimitSelector, fileSizeLimit)}
                    ${createTextInputValue("Max Number of Transactions:", transactionLimitSelector, transactionLimit)}
                </div>
                <div class="modal-footer">
                    <button id="${id}-save" type="button" class="btn btn-secondary" data-dismiss="modal">Save</button>
                </div>
            </div>
        </div>
    </div>`;
}

/**
 * Takes a capture id and send the update request to the backend
 * @param  {string}
 */
// TODO fix the match between these selectors and the ones above on lines 172-178
function updateCapture(id) {
    var body = {
        id: id,
        startTime: $(`#${id}-modal .txtStartTime`).val(),
        endTime: $(`#${id}-modal .txtEndTime`).val(),
        fileSizeLimit: $(`#${id}-modal .txtMaxSize`).val(),
        transactionLimit: $(`#${id}-modal .txtMaxTrans`).val()
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

function createCaptureListItem(id, selector) {
    return `<li id="item-${id}" class="list-group-item">${id}<a data-toggle="modal" data-target="#${selector}" href="javascript:void(0)" class="pull-right">Edit</a></li>`;
}

/**           
 * Starts a capture using the given Capture object
 * @param  {Capture} The Capture object to be started
 */
function startCapture(capture) {
    $.ajax({
        url: "/capture/start",
        type: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        data: JSON.stringify(capture),
        success: function() {
            $("#exampleModal").html(createStartCaptureModal("Successful"));
            $("#exampleModal").modal("show");
        },
        error: function(err) {
            $("#exampleModal").html(createStartCaptureModal("Failure"));
            $("#exampleModal").modal("show");

            console.log("Error starting capture");
            console.log(err);
        }
    });
}

function createStartCaptureModal(result) {
    return `
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Capture ${result}</h5>
            </div>
            <div class="modal-body">
                ${result === "Successful" ? 
                    "<p>Your capture is in progress. Go to Dashboard to see the current status.</p>" :
                    "<p>Your capture failed to start. Verify all fields are correct.</p>"}
                
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
            </div>
        </div>
    </div>`;
}

/**
 * Populate rds dropdown
 * @param {string} selector
 */
function populateRDSDropdown(selector) {
    $.ajax({
        url: "/resource/rds",
        type: "GET",
        beforeSend: function() {
            $(".startCaptureLoadingIcon").show();
        },
        complete: function() {
            $(".startCaptureLoadingIcon").hide();
        },
        success: function(data) {
            $(`div.${selector}`).replaceWith(createDropdown("Select RDS Endpoint", selector, data));
        },
        error: function(err) {
            console.log("Error populating rds dropdown")
            console.log(err);
        }
    });
}

/**
 * Populate rds dropdown
 * @param {string} selector
 */
function populateS3Dropdown(selector) {
    $.ajax({
        url: "/resource/s3",
        type: "GET",
        success: function(data) {
            $(`div.${selector}`).replaceWith(createDropdown("Select S3 Endpoint", selector, data));
        },
        error: function(err) {
            console.log("Error populating s3 dropdown")
            console.log(err);
        }
    });
}

function createTextInput(label, id) {
    return `
    <div class="form-group">
        <label class="input-label">${label}</label>
        <input id="${id}" class="form-control" type="text" value="">
    </div>`;
}

function createTextInputValue(label, id, value) {
    return `
    <div class="form-group">
        <label class="input-label">${label}</label>
        <input id="${id}" class="form-control" type="text" value="${value}">
    </div>`;
}

function createDropdown(label, id, options) {
    return `
    <div class="form-group">
        <label class="input-label">${label}</label>
        <select class="form-control" id="${id}">
            ${options.map(createOption).join('')}
        </select>
    </div>`;
}

function createOption(option) {
    return `<option value="${option}">${option}</option>`;
}
