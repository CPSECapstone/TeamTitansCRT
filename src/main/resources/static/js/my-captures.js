$(function() {
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
    var searchSelector = "searchSelector";

    var startBtnSelector = "btnCaptureStart";

    $("div.content-placeholder").replaceWith(`
    <div class="container">
        <div class="row">
            <div class="col-lg-6 col-lg-offset-3">
                <h4 class="text-center">My Captures</h4>
                <p class="text-center">Start and manage workload captures</p>
            </div>
        </div>
        <div class="row">
            <div class="col-lg-6 start-capture-form border-on-right">
                <p class=""><strong>Start a Capture</strong></p>
                <hr />
                ${insertLoadingSpinner("startCaptureLoadingIcon")}
                <div class="${rdsRegionSelector}"></div>
                <div class="${rdsSelector}"></div>
                <div class="${s3RegionSelector}"></div>
                <div class="${s3Selector}"></div>
                ${createTextInput("Capture ID:", idSelector)}

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
                    ${createNumericInput("Max Capture Size (KB):", fileSizeLimitSelector)}
                    ${createNumericInput("Max Number of Transactions:", transactionLimitSelector)}
                    ${createTextInput("Database Commands to Ignore (comma delimited):", filterStatementsSelector)}
                    ${createTextInput("Database Users to Ignore (comma delimited):", filterUsersSelector)}
                </div>
                <a href="javascript:void(0)" id="" class="${startBtnSelector} btn btn-default">Start Capture</a>
            </div>
            <div class="col-lg-6">
                <p class=""><strong>Manage Captures</strong></p>
                <hr />
                ${createTextInputPlaceholder("Capture Filter:", searchSelector, "Search for capture")}
                ${insertLoadingSpinner("manageCapturesLoadingIcon")}
                <ul id="CaptureList" class="list-group"></ul>
            </div>
        </div>
    </div>
    `);
    updateCaptureList();
    // testCaptureList();
    
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

        // Only start capture if rds and s3 selected
        if ($(`.${rdsSelector}`).val() != '' && $(`.${s3Selector}`).val() != '' && $(`.${idSelector}`).val() != '') {
            
            var capture = {
                id: $(`.${idSelector}`).val(),
                rds: $(`.${rdsSelector}`).val(),
                rdsRegion: $(`.${rdsRegionSelector}`).val(),
                s3: $(`.${s3Selector}`).val(),
                s3Region: $(`.${s3RegionSelector}`).val(),
                startTime: startTime,
                endTime: endTime,
                fileSizeLimit: $(`.${fileSizeLimitSelector}`).val(),
                transactionLimit: $(`.${transactionLimitSelector}`).val(),
                filterStatements: $(`.${filterStatementsSelector}`).val().split(',').map(x => x.trim()),
                filterUsers: $(`.${filterUsersSelector}`).val().split(',').map(x => x.trim())
            };

            startCapture(capture);
        }
    });

    $(`.${searchSelector}`).on("input", function() {
        var value = $(this).val().toLowerCase();

        $("#CaptureList > li").each(function() {
            if (value == '' || $(this).attr('id').slice(5).toLowerCase().includes(value)) {
                $(this).show();
            }
            else {
                $(this).hide();
            }
        });
    });
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

    // link delete button to modal
    $(`#${id}-delete-link`).on("click", function() {
        deleteCapture(capture);
    });
}

function deleteCapture(capture) {
    var id = capture["id"];
    console.log(`deleting ${id}`);

    // sends request to backend
    $.ajax({
        url: "/capture/delete",
        type: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        data: JSON.stringify(capture),
        success: function() {
            $("#exampleModal").html(createDeleteCaptureModal("Successful", `Capture ${id} was deleted.`));
            $('#exampleModal').on('hidden.bs.modal', function () {
                updateCaptureList();
            });
            $("#exampleModal").modal("show");
        },
        error: function(err) {
            $("#exampleModal").html(createDeleteCaptureModal("Failure", "Deletion failed."));
            $("#exampleModal").modal("show");

            console.log("Error deleting capture");
            console.log(err.responseText);
        }
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
    // startTime.setHours(startTime.getHours() - 7); // daylight savings lol
    console.log(startTime);
    //startTime.setHours(startTime.getHours());
    var tzoffset = (new Date()).getTimezoneOffset() * 60000; //offset in milliseconds
    startTime = (new Date(startTime - tzoffset)).toISOString().slice(0, -1);
    //startTime = startTime.toISOString().replace("Z", "");
    console.log(startTime);
    var endTime = capture["endTime"];
    if (endTime != null) {
        endTime = new Date(endTime);
        endTime = (new Date(endTime - tzoffset)).toISOString().slice(0, -1);
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
                        
    footerDelete = `<a id="${id}-delete-link" class="btn btn-primary" data-dismiss="modal">Delete</a>`;
    footerDelete += `<a class="btn btn-secondary" data-dismiss="modal">Close</a>`;

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
    </div>
    <div class="modal fade" id="${id}-modal-delete" tabindex="-1" role="dialog">
        <div class="modal-dialog" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title">Delete Capture: ${id}</h5>
                </div>
                <div class="modal-body">
                    <p>Are sure you want to delete capture? Please note this will delete all associated replays.</p>
                </div>
                <div class="modal-footer">
                    ${footerDelete}
                </div>
            </div>
        </div>
    </div>`;
}

function createCaptureListItem(id, status, selector) {
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
            updateCaptureList();
        },
        error: function(err) {
            $("#lblStatus").html("Startup failure.");
            console.log(err);
        }
    });
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
function startCapture(capture) {
    $.ajax({
        url: "/capture/start",
        type: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        data: JSON.stringify(capture),
        success: function() {
            $("#exampleModal").html(createStartCaptureModal("Successful", "Your capture is in progress. Go to Dashboard to see the current status."));
            $('#exampleModal').on('hidden.bs.modal', function () {
                updateCaptureList();
            });
            $("#exampleModal").modal("show");
        },
        error: function(err) {
            $("#exampleModal").html(createStartCaptureModal("Failure", err.responseText ? err.responseText : "Your capture failed to start. Verify all fields are correct."));
            $("#exampleModal").modal("show");

            console.log("Error starting capture");
            console.log(err);
        }
    });
}

function createStartCaptureModal(result, message) {
    return `
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title">Capture ${result}</h5>
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

function createDeleteCaptureModal(result, message) {
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
 * Populate region and rds/s3 dropdowns
 */
function populateResourceDropdowns(rdsRegion, rdsSelector, s3Region, s3Selector) {
    $.ajax({
        url: "/resource/regions",
        type: "GET",
        beforeSend: function() {
            $(".startCaptureLoadingIcon").show();
        },
        complete: function() {
            $(".startCaptureLoadingIcon").hide();
        },
        success: function(data) {
            var defaultVal = "US_WEST_1"

            $(`div.${rdsRegion}`).replaceWith(createDropdownWithDefault("Select RDS Region", rdsRegion, data, defaultVal));
            $(`div.${s3Region}`).replaceWith(createDropdownWithDefault("Select S3 Region", s3Region, data, defaultVal));

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
                `<select class="${selector} form-control" id="">
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
                `<select class="${selector} form-control" id="">
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
    return createTextInputValue(label, id, "");
}

function createTextInputValue(label, id, value) {
    return `
    <div class="form-group">
        <label class="input-label">${label}</label>
        <input id="" class="${id} form-control" type="text" value="${value}">
    </div>`;
}

function createTextInputPlaceholder(label, id, value) {
    return `
    <div class="form-group">
        <label class="input-label">${label}</label>
        <input id="" class="${id} form-control" type="text" placeholder="${value}">
    </div>`;
}

function createNumericInput(label, id) {
    return createNumericInputValue(label, id, "");
}

function createNumericInputValue(label, id, value) {
    return `
    <div class="form-group">
        <label class="input-label">${label}</label>
        <input id="" class="${id} form-control" type="number" min="1" max="${1e5}" value="${value}">
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

function createDropdownWithDefault(label, id, options, defaultVal) {
    return `
    <div class="form-group">
        <label class="input-label">${label}</label>
        <select class="${id} form-control" id="">
            ${options.map(function(x) { return createOptionWithDefault(x, defaultVal);}).join('')}
        </select>
    </div>`;
}

function createOption(option) {
    return `<option value="${option}">${option}</option>`;
}

function createOptionWithDefault(option, defaultVal) {
    if (option == defaultVal) {
        return `<option value="${option}" selected="selected">${option}</option>`;
    }
    return `<option value="${option}">${option}</option>`;
}

function openAnalysis(id) {
    sessionStorage.setItem("defaultCapture", id);
}