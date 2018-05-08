$(function() {
    var captureSelector = "captureSelector";
    var typeSelector = "typeSelector";

    var idSelector = "idSelector";
    var rdsSelector = "rdsSelector";
    var s3Selector = "s3Selector";
    var rdsRegionSelector = "rdsRegionSelector";
    var s3RegionSelector = "s3RegionSelector";
    var startTimeSelector = "startTimeSelector";
    var endTimeSelector = "endTimeSelector";
    var fileSizeLimitSelector = "fileSizeLimitSelector";
    var transactionLimitSelector = "transactionLimitSelector";
    var filterStatementsSelector = "filterStatementsSelector";
    var filterUsersSelector = "filterUsersSelector";

    var replayNameSelector = "replayNameSelector";
    var usernameSelector = "usernameSelector";
    var passwordSelector = "passwordSelector";

    var startBtnSelector = "btnReplayStart";

    $("div.content-placeholder").replaceWith(`
    <div class="container">
        <div class="row">
            <div class="col-lg-6 col-lg-offset-3">
                <h4 class="text-center">My Replays</h4>
                <p class="text-center">Start and manage workload replays</p>
            </div>
        </div>
        <div class="row">
            <div class="col-lg-6 start-capture-form border-on-right">
                <p class=""><strong>Start a Replay</strong></p>
                <hr />
                ${insertLoadingSpinner("startReplayLoadingIcon")}
                <div class="${captureSelector}"></div>
                <div class="${rdsRegionSelector}"></div>
                <div class="${rdsSelector}"></div>
                <div class="${s3RegionSelector}"></div>
                <div class="${s3Selector}"></div>

                ${createTextInput("Replay Name:", replayNameSelector)}
                ${createTextInputValue("Replay RDS Username:", usernameSelector, "admin")}
                ${createPasswordInputValue("Replay RDS Password:", passwordSelector, "TeamTitans!")}

                <div class="block">
                    <a data-toggle="collapse" href="#advanced">Advanced <span class="caret"></span></a>
                </div>                

                <div id="advanced" class="collapse">
                    <div class="${typeSelector}"></div>
                    <label class="input-label">Start Time:
                        <input class="${startTimeSelector} form-control" type="datetime-local" value="">
                    </label>
                    <label class="input-label">End Time:
                        <input class="${endTimeSelector} form-control" type="datetime-local" value="">
                    </label>
                    ${createTextInput("Max Replay Size (mB):", fileSizeLimitSelector)}
                    ${createTextInput("Max Number of Transactions:", transactionLimitSelector)}
                    ${createTextInput("Database Commands to Ignore (comma delimited):", filterStatementsSelector)}
                    ${createTextInput("Database Users to Ignore (comma delimited):", filterUsersSelector)}
                </div>
                <a href="javascript:void(0)" class="${startBtnSelector} btn btn-default">Start Replay</a>
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
    populateTypeDropdown(typeSelector);
    populateResourceDropdowns(rdsRegionSelector, rdsSelector, s3RegionSelector, s3Selector);

    $(`.${startBtnSelector}`).on("click", function() {
        var startTime = null;
        if ($(`.${startTimeSelector}`).val()) {
            startTime = new Date(String($(`.${startTimeSelector}`).val()));
        }

        var endTime = null;
        if ($(`.${endTimeSelector}`).val()) {
            endTime = new Date(String($(`.${endTimeSelector}`).val()));
        }

        // Only start capture if rds and s3 selected and a username and password have been entered
        if ($(`.${captureSelector}`).val() != null && $(`.${rdsSelector}`).val() != '' && $(`.${s3Selector}`).val() != '' &&
        $(`.${usernameSelector}`).val() != '' &&
        $(`.${passwordSelector}`).val() != '') {
            var replayInner = {
                databaseInfo: {
                    //dbUrl: "testdb.cgtpml3lsh3i.us-west-1.rds.amazonaws.com:3306",
                    database: $(`.${rdsSelector}`).val(),
                    username: $(`.${usernameSelector}`).val(),
                    password: $(`.${passwordSelector}`).val()
                },
                captureId: $(`.${captureSelector}`).val(),
                id: $(`.${replayNameSelector}`).val(),
                rds: $(`.${rdsSelector}`).val(),
                rdsRegion: $(`.${rdsRegionSelector}`).val(),
                s3: $(`.${s3Selector}`).val(),
                s3Region: $(`.${s3RegionSelector}`).val(),
                startTime: startTime,
                endTime: endTime,
                fileSizeLimit: $(`.${fileSizeLimitSelector}`).val(),
                transactionLimit: $(`.${transactionLimitSelector}`).val(),
                filterStatements: $(`.${filterStatementsSelector}`).val().split(',').map(x => x.trim()),
                filterUsers: $(`.${filterUsersSelector}`).val().split(',').map(x => x.trim()),
                replayType: $(`.${typeSelector}`).val()
            };

            // var replayInner = {
            //     databaseInfo: {
            //         dbUrl: "testdb.cgtpml3lsh3i.us-west-1.rds.amazonaws.com:3306",
            //         database: "testdb",
            //         username: "admin",
            //         password: "TeamTitans!"
            //     },
            //     captureId: "MyCaptureTestKyle33",
            //     id: "TestReplay",
            //     rds: $(`.${rdsSelector}`).val(),
            //     s3: $(`.${s3Selector}`).val(),
            //     startTime: startTime,
            //     endTime: endTime,
            //     fileSizeLimit: $(`.${fileSizeLimitSelector}`).val(),
            //     transactionLimit: $(`.${transactionLimitSelector}`).val(),
            //     filterStatements: $(`.${filterStatementsSelector}`).val().split(',').map(x => x.trim()),
            //     filterUsers: $(`.${filterUsersSelector}`).val().split(',').map(x => x.trim()),
            //     replayType: "Fast Mode"
            // };
            startReplay(replayInner);
        }
        else {
            $("#exampleModal").html(createStartReplayModal("Failure", "Your replay failed to start. Verify all fields are correct."));
            $("#exampleModal").modal("show");
        }
    });
});

function testReplayList() {
    var data = [
        {
            captureId: "test_capture",
            captureLogFileName: "test_capture-Workload.log",
            database: null,
            dbpassword: null,
            dburl: null,
            dbusername: null,
            endTime: "2018-05-03",
            filterStatements: [],
            filterUsers: [],
            id: "MyReplay",
            rds: "testdb",
            rdsRegion: "US_WEST_1",
            replayType: "Fast Mode",
            s3: "teamtitans-test-mycrt",
            s3Region: "US_WEST_1",
            startTime: "2018-05-03",
            status: "Finished",
            transactionLimit: 0
        },
        {
            captureId: "test_capture",
            captureLogFileName: "test_capture-Workload.log",
            database: null,
            dbpassword: null,
            dburl: null,
            dbusername: null,
            endTime: "2018-05-03",
            filterStatements: [],
            filterUsers: [],
            id: "MyReplay_2",
            rds: "testdb",
            rdsRegion: "US_WEST_1",
            replayType: "Fast Mode",
            s3: "teamtitans-test-mycrt",
            s3Region: "US_WEST_1",
            startTime: "2018-05-03",
            status: "Finished",
            transactionLimit: 0
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
        url: "/resource/replays",
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
                $("#ReplayList").append(`<p>You have no replay history</p>`);
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
function addToReplayList(replay) {
    $("#ReplayList").append(createEditReplayModal(replay));

    var id = replay["id"];
    $(`#${id}-save`).on("click", function() {
        updateReplay(id);
    });

    // link delete button to modal
    $(`#${id}-delete-link`).on("click", function() {
        deleteReplay(replay);
    });
}

function deleteReplay(replay) {
    var id = replay["id"];
    console.log(`deleting ${id}`);

    // sends request to backend
    $.ajax({
        url: "/replay/delete",
        type: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        data: JSON.stringify(replay),
        success: function() {
            $("#exampleModal").html(createDeleteReplayModal("Successful", `Replay ${id} was deleted.`));
            $('#exampleModal').on('hidden.bs.modal', function () {
                updateReplayList();
            });
            $("#exampleModal").modal("show");
        },
        error: function(err) {
            $("#exampleModal").html(createDeleteReplayModal("Failure", "Deletion failed."));
            $("#exampleModal").modal("show");

            console.log("Error deleting replay");
            console.log(err.responseText);
        }
    });
}

function createDeleteReplayModal(result, message) {
    return `
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Delete ${result}</h5>
            </div>
            <div class="modal-body">
                <p>${message}</p>
            </div>
            <div class="modal-footer">
                <a class="btn btn-secondary" data-dismiss="modal">Close</a>
            </div>
        </div>
    </div>`;
}

/**
 * Function that uses a template to create a card
 * @param  {Capture}
 * @return {string}
 */
function createEditReplayModal(replay) {
    var id = replay["id"];
    var status = replay["status"];
    
    // to be fixed with user set timezone
    var startTime = new Date(replay["startTime"]);
    startTime.setHours(startTime.getHours() - 7); // daylight savings lol
    startTime = startTime.toISOString().replace("Z", "");
    var endTime = replay["endTime"];
    if (endTime != null) {
        endTime = new Date(endTime);
        endTime.setHours(endTime.getHours() - 7); // daylight savings lol
        endTime = endTime.toISOString().replace("Z", "");
    }
    else {
        endTime = "";
    }

    var fileSizeLimit = replay["fileSizeLimit"];
    var transactionLimit = replay["transactionLimit"];
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
                        <input class="txtStartTime form-control" type="datetime-local" value="${startTime}">
                    </label>
                    <label class="input-label">End Time:
                        <input class="txtEndTime form-control" type="datetime-local" value="${endTime}">
                    </label>
                    ${createTextInputValue("Max Replay Size (mB):", "txtMaxSize", fileSizeLimit)}
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
function updateReplay(id) {
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
        url: "/",
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
    /*
        return `
    <li id="item-${id}" class="list-group-item">
        ${createIcon(status)}
        ${id}
        <div class="pull-right">
            <a data-toggle="modal" data-target="#${selector}-delete" class="pull-right" href="javascript:void(0)"><span class="glyphicon glyphicon-trash"></span></a>
            <a data-toggle="modal" data-target="#${selector}" href="javascript:void(0)" class="pad-right">
                ${status == 'Finished' || status == 'Failed' ? 'View' : 'Edit'}
            </a>
        </div>
    </li>`;

        <a data-toggle="modal" data-target="#${selector}" href="javascript:void(0)" class="pull-right">
        ${status == "Finished" ? "View" : "Edit"}
        </a>
     */

    return `
    <li id="item-${id}" class="list-group-item">
        ${createIcon(status)}
        ${id}
        <div class="pull-right">
            <a data-toggle="modal" data-target="#${selector}-delete" class="pull-right" href="javascript:void(0)"><span class="glyphicon glyphicon-trash"></span></a>
            <a data-toggle="modal" data-target="#${selector}" href="javascript:void(0)" class="pad-right">
                ${status == 'Finished' || status == 'Failed' ? 'View' : 'Edit'}
            </a>
        </div>
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
function startReplay(replay) {
    console.log(replay);
    $.ajax({
        url: "/replay/start",
        type: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        data: JSON.stringify(replay),
        success: function() {
            $("#exampleModal").html(createStartReplayModal("Successful", "Your replay is in progress. Go to Dashboard to see the current status."));
            $('#exampleModal').on('hidden.bs.modal', function () {
                updateReplayList();
            });
            $("#exampleModal").modal("show");
        },
        error: function(err) {
            $("#exampleModal").html(createStartReplayModal("Failure", err.responseText ? err.responseText : "Your replay failed to start. Verify all fields are correct."));
            $("#exampleModal").modal("show");

            console.log("Error starting relpay");
            console.log(err);
        }
    });
}

function createStartReplayModal(result, message) {
    return `
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Replay ${result}</h5>
            </div>
            <div class="modal-body">
                <p>${message}</p>
            </div>
            <div class="modal-footer">
                <a href="dashboard" class="btn btn-default">Dashboard</a>
                <a class="btn btn-secondary" data-dismiss="modal">Close</a>
            </div>
        </div>
    </div>`;
}

function populateTypeDropdown(selector) {
    $(`div.${selector}`).replaceWith(createDropdown("Select Replay Mode", selector, ["Fast Mode", "Time Sensitive"]));
}

/**
 * Populate region and rds/s3 dropdowns
 */
function populateResourceDropdowns(rdsRegion, rdsSelector, s3Region, s3Selector) {
    $.ajax({
        url: "/resource/regions",
        type: "GET",
        beforeSend: function() {
            $(".startReplayLoadingIcon").show();
        },
        complete: function() {
            $(".startReplayLoadingIcon").hide();
        },
        success: function(data) {
            $(`div.${rdsRegion}`).replaceWith(createDropdown("Select RDS Region", rdsRegion, data));
            $(`div.${s3Region}`).replaceWith(createDropdown("Select S3 Region", s3Region, data));

            $(`.${rdsRegion}`).on("change", function() {
                updateRDSDropdown(rdsSelector, rdsRegion);
            });

            $(`.${s3Region}`).on("change", function() {
                updateS3Dropdown(s3Selector, s3Region);
            });

            populateRDSDropdown(rdsSelector, rdsRegion);
            populateS3Dropdown(s3Selector, s3Region);
        },
        error: function(err) {
            console.log("Error populating region dropdowns")
            console.log(err);
        }
    });
}

/**
 * Populate rds dropdown
 * @param {string} selector
 */
function populateRDSDropdown(selector, regionSelector) {
    $.ajax({
        url: "/resource/rds/" + $("." + regionSelector).val(),
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
 * Update rds dropdown
 * @param {string} selector
 * @param {string} regionSelector
 */
function updateRDSDropdown(selector, regionSelector) {
    $.ajax({
        url: "/resource/rds/" + $("." + regionSelector).val(),
        type: "GET",
        success: function(data) {
            $(`.${selector}`).replaceWith(
                `<select class="${selector} form-control">
                    ${data.map(createOption).join('')}
                </select>`);
        },
        error: function(err) {
            console.log("Error populating rds dropdown")
            console.log(err);
        }
    });
}

/**
 * Populate capture dropdown
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
            console.log(data);
            $(`div.${selector}`).replaceWith(createDropdown("Select Capture to Replay", selector, data.map(x => x.id)));
        },
        error: function(err) {
            console.log("Error populating capture dropdown")
            console.log(err);
        }
    });
}

/**
 * Populate s3 dropdown
 * @param {string} selector
 */
function populateS3Dropdown(selector, regionSelector) {
    $.ajax({
        url: "/resource/s3/" + $("." + regionSelector).val(),
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

/**
 * Update s3 dropdown
 * @param {string} selector
 * @param {string} regionSelector
 */
function updateS3Dropdown(selector, regionSelector) {
    $.ajax({
        url: "/resource/s3/" + $("." + regionSelector).val(),
        type: "GET",
        success: function(data) {
            $(`.${selector}`).replaceWith(
                `<select class="${selector} form-control">
                    ${data.map(createOption).join('')}
                </select>`);
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
        <input class="${id} form-control" type="text" value="">
    </div>`;
}

function createTextInputValue(label, id, value) {
    return `
    <div class="form-group">
        <label class="input-label">${label}</label>
        <input class="${id} form-control" type="text" value="${value}">
    </div>`;
}

function createPasswordInputValue(label, id, value) {
    return `
    <div class="form-group">
        <label class="input-label">${label}</label>
        <input class="${id} form-control" type="password" value="${value}">
    </div>`;
}

function createDropdown(label, id, options) {
    return `
    <div class="form-group">
        <label class="input-label">${label}</label>
        <select class="${id} form-control">
            ${options.map(createOption).join('')}
        </select>
    </div>`;
}

function createOption(option) {
    return `<option value="${option}">${option}</option>`;
}
