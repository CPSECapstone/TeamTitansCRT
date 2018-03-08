$(function() {
    var rdsSelector = "rdsSelector";
    var s3Selector = "s3Selector";

    $("div.content-placeholder").replaceWith(`
    <div class="container">
        <div class="row">
            <div class="col-lg-6 col-lg-offset-3">
                <h4 class="text-center">My Captures</h4>
                <p class="text-center">Add page description</p>
            </div>
        </div>
        <div class="row">
            <div class="col-lg-offset-2 col-lg-4 border-on-right">
                <p class=""><strong>Manage Captures</strong></p>
                ${createCaptureTable()}
            </div>
            <div class="col-lg-4 start-capture-form">
                <p class=""><strong>Start a Capture</strong></p>
                <div class="${rdsSelector}"></div>
                <div class="${s3Selector}"></div>
                ${createTextInput("Capture ID:", "txtID")}

                <a data-toggle="collapse" href="#advanced">Advanced <span class="caret"></span></a>

                <div id="advanced" class="collapse">
                    <label class="input-label">Start Time:
                        <input id="txtStartTime" class="form-control" type="datetime-local" value="">
                    </label>
                    <label class="input-label">End Time:
                        <input id="txtEndTime" class="form-control" type="datetime-local" value="">
                    </label>
                    ${createTextInput("Max Capture Size (mB):", "txtMaxSize")}
                    ${createTextInput("Max Number of Transactions:", "txtMaxTrans")}
                    ${createTextInput("Database Commands to Ignore (comma delimited):", "txtFilterStatements")}
                    ${createTextInput("Database Users to Ignore (comma delimited):", "txtFilterUsers")}
                </div>
            </div>
        </div>
    </div>
    `);
    populateRDSDropdown(rdsSelector);
    populateS3Dropdown(s3Selector);
});

/**
 * Populate rds dropdown
 * @param {string} selector
 */
function populateRDSDropdown(selector) {
    $.ajax({
        url: "/resource/rds",
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

function createCaptureTable() {
    return `
    <ul class="list-group">
      <li class="list-group-item">Cras justo odio <a href="javascript:void(0)" class="pull-right">Edit</a></li>
      <li class="list-group-item">Dapibus ac facilisis in <a href="javascript:void(0)" class="pull-right">Edit</a></li>
      <li class="list-group-item">Morbi leo risus <a href="javascript:void(0)" class="pull-right">Edit</a></li>
      <li class="list-group-item">Porta ac consectetur ac <a href="javascript:void(0)" class="pull-right">Edit</a></li>
      <li class="list-group-item">Vestibulum at eros <a href="javascript:void(0)" class="pull-right">Edit</a></li>
    </ul>`;
}