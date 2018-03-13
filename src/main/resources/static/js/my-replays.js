$(function() {
    var idSelector = "idSelector";
    var captureSelector = "captureSelector";
    var rdsSelector = "rdsSelector";
    var s3Selector = "s3Selector";
    var startTimeSelector = "startTimeSelector";
    var endTimeSelector = "endTimeSelector";
    var fileSizeLimitSelector = "fileSizeLimitSelector";
    var transactionLimitSelector = "transactionLimitSelector";
    var filterStatementsSelector = "filterStatementsSelector";
    var filterUsersSelector = "filterUsersSelector";

    var usernameSelector = "usernameSelector";
    var passwordSelector = "passwordSelector";

    var startBtnSelector = "btnReplayStart";

    $("div.content-placeholder").replaceWith(`
    <div class="container">
        <div class="row">
            <div class="col-lg-6 col-lg-offset-3">
                <h4 class="text-center">My Replays</h4>
                <p class="text-center">Add page description</p>
            </div>
        </div>
        <div class="row">
            <div class="col-lg-6 start-capture-form border-on-right">
                <p class=""><strong>Start a Replay</strong></p>
                <hr />
                ${insertLoadingSpinner("startReplayLoadingIcon")}
                <div class="${captureSelector}"></div>
                <div class="${rdsSelector}"></div>
                <div class="${s3Selector}"></div>
                ${createTextInput("Replay RDS Username:", usernameSelector)}
                ${createTextInput("Replay RDS Password:", passwordSelector)}

                <div class="block">
                    <a data-toggle="collapse" href="#advanced">Advanced <span class="caret"></span></a>
                </div>                

                <div id="advanced" class="collapse">
                    <label class="input-label">Start Time:
                        <input id="" class="${startTimeSelector} form-control" type="datetime-local" value="">
                    </label>
                    <label class="input-label">End Time:
                        <input id="" class="${endTimeSelector} form-control" type="datetime-local" value="">
                    </label>
                    ${createTextInput("Max Capture Size (mB):", fileSizeLimitSelector)}
                    ${createTextInput("Max Number of Transactions:", transactionLimitSelector)}
                    ${createTextInput("Database Commands to Ignore (comma delimited):", filterStatementsSelector)}
                    ${createTextInput("Database Users to Ignore (comma delimited):", filterUsersSelector)}
                </div>
                <a href="javascript:void(0)" id="" class="${startBtnSelector} btn btn-default">Start Replay</a>
            </div>
            <div class="col-lg-6">
                <p class=""><strong>Manage Replays</strong></p>
                <hr />
                ${insertLoadingSpinner("manageReplaysLoadingIcon")}
                <ul id="ReplayList" class="list-group"></ul>
            </div>
        </div>
    </div>
    `);
    updateReplayList();
    // testReplayList();

    populateCapturesDropdown(captureSelector);
    populateRDSDropdown(rdsSelector);
    populateS3Dropdown(s3Selector);
    $(`.${startBtnSelector}`).on("click", function() {
        var startTime = null;
        if ($(`.${startTimeSelector}`).val()) {
            startTime = new Date(String($(`.${startTimeSelector}`).val()));
        }

        var endTime = null;
        if ($(`.${endTimeSelector}`).val()) {
            endTime = new Date(String($(`.${endTimeSelector}`).val()));
        }

        // Only start capture if rds and s3 selected
        if ($(`.${rdsSelector}`).val() != '' && $(`.${s3Selector}`).val() != '') {
            var capture = {
                id: $(`.${idSelector}`).val(),
                rds: $(`.${rdsSelector}`).val(),
                s3: $(`.${s3Selector}`).val(),
                startTime: startTime,
                endTime: endTime,
                fileSizeLimit: $(`.${fileSizeLimitSelector}`).val(),
                transactionLimit: $(`.${transactionLimitSelector}`).val(),
                filterStatements: $(`.${filterStatementsSelector}`).val().split(',').map(x => x.trim()),
                filterUsers: $(`.${filterUsersSelector}`).val().split(',').map(x => x.trim())
            };

            startReplay(capture);
        }
    });
});

function testReplayList() {
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
    addAllToReplayList(data);
}

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
function updateReplayList() {
    $.ajax({
        url: "/capture/status",
        type: "GET",
        beforeSend: function() {
            $(".manageReplaysLoadingIcon").show();
        },
        success: function(data) {
            console.log(data);
            if (data.length > 0) {
                setTimeout(function()
                {
                    $(".manageReplaysLoadingIcon").hide();
                    addAllToReplayList(data);
                },
                500);
            }
            else {
                $(".manageReplaysLoadingIcon").hide();
                $("#ReplayList").append(`<p>You have no capture history</p>`);
            }
        },
        error: function(err) {
            console.log(err);
            $(".manageReplaysLoadingIcon").hide();
        }
    });
}

function addAllToReplayList(data) {
    // clears contents of the ReplayList
    $('#ReplayList').empty();
    data.map(addToReplayList).join('')
}

/**
 * Takes a capture, adds it to the list of capture cards, and creates the save binding
 * @param {Capture}
 */
function addToReplayList(capture) {
    $("#ReplayList").append(createEditCaptureModal(capture));

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
    startTime.setHours(startTime.getHours() - 7); // daylight savings lol
    startTime = startTime.toISOString().replace("Z", "");
    var endTime = capture["endTime"];
    if (endTime != null) {
        endTime = new Date(endTime);
        endTime.setHours(endTime.getHours() - 7); // daylight savings lol
        endTime = endTime.toISOString().replace("Z", "");
    }
    else {
        endTime = "";
    }

    var fileSizeLimit = capture["fileSizeLimit"];
    var transactionLimit = capture["transactionLimit"];
    return `
    ${createReplayListItem(id, status, `${id}-modal`)}
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
                    ${createTextInputValue("Max Capture Size (mB):", "txtMaxSize", fileSizeLimit)}
                    ${createTextInputValue("Max Number of Transactions:", "txtMaxTrans", transactionLimit)}
                </div>
                <div class="modal-footer">
                    ${status == "Finished" ? 
                        `<a class="btn btn-secondary" data-dismiss="modal">Close</a>` : 
                        `<a id="${id}-save" class="btn btn-primary" data-dismiss="modal">Save</a>`}
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
    var startTime = null;
    if ($(`#${id}-modal .txtStartTime`).val()) {
        startTime = new Date(String($(`#${id}-modal .txtStartTime`).val()));
    }

    var endTime = null;
    if ($(`#${id}-modal .txtEndTime`).val()) {
        endTime = new Date(String($(`#${id}-modal .txtEndTime`).val()));
    }
    
    var body = {
        id: id,
        startTime: startTime,
        endTime: endTime,
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
            updateReplayList();
        },
        error: function(err) {
            $("#lblStatus").html("Startup failure.");
            console.log(err);
        }
    });
}

function createReplayListItem(id, status, selector) {
    return `
    <li id="item-${id}" class="list-group-item">
        ${createIcon(status)}${id}
        <a data-toggle="modal" data-target="#${selector}" href="javascript:void(0)" class="pull-right">
        ${status == "Finished" ? "View" : "Edit"}
        </a>
    </li>`;
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

/**           
 * Starts a capture using the given Capture object
 * @param  {Capture} The Capture object to be started
 */
function startReplay(capture) {
    $.ajax({
        url: "/capture/start",
        type: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        data: JSON.stringify(capture),
        success: function() {
            $("#exampleModal").html(createStartReplayModal("Successful"));
            $('#exampleModal').on('hidden.bs.modal', function () {
                updateReplayList();
            });
            $("#exampleModal").modal("show");
        },
        error: function(err) {
            $("#exampleModal").html(createStartReplayModal("Failure"));
            $("#exampleModal").modal("show");

            console.log("Error starting capture");
            console.log(err);
        }
    });
}

function createStartReplayModal(result) {
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
                <a href="dashboard" class="btn btn-default">Dashboard</a>
                <a class="btn btn-secondary" data-dismiss="modal">Close</a>
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
            $(".startReplayLoadingIcon").show();
        },
        complete: function() {
            $(".startReplayLoadingIcon").hide();
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
function populateCapturesDropdown(selector) {
    $.ajax({
        url: "/resource/captures/finished",
        type: "GET",
        beforeSend: function() {
            $(".startReplayLoadingIcon").show();
        },
        complete: function() {
            $(".startReplayLoadingIcon").hide();
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
        <input id="" class="${id} form-control" type="text" value="">
    </div>`;
}

function createTextInputValue(label, id, value) {
    return `
    <div class="form-group">
        <label class="input-label">${label}</label>
        <input id="" class="${id} form-control" type="text" value="${value}">
    </div>`;
}

function createDropdown(label, id, options) {
    return `
    <div class="form-group">
        <label class="input-label">${label}</label>
        <select class="${id} form-control" id="">
            ${options.map(createOption).join('')}
        </select>
    </div>`;
}

function createOption(option) {
    return `<option value="${option}">${option}</option>`;
}
