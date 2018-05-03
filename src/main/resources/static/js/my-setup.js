$(function() {
    var instProfHelpSelector = "InstProfModal";
    
    $("div.content-placeholder").replaceWith(`
    <div class="container">
        <div class="row">
            
        </div>
        <div class="row">
            <div class="col-lg-6 start-capture-form border-on-right" style="text-align: center">
                <div class="welcome">
                    <div class="center-welcome">MyCRT</div>

                    <p><a href="/dashboard" class="btn btn-default gray" >Use My Instance Profile</a></p>

                    <p><a href="javascript:void(0)" class="" data-toggle="modal" data-target="#${instProfHelpSelector}">Set Up An Instance Profile</a></p>                    
                </div>
                <div id="${instProfHelpSelector}" class="modal fade" role="dialog">
                    ${createInstanceProfHelpModal(instProfHelpSelector)}
                </div>  
            </div>
            <div class="col-lg-6">
                <div class="pull-right">
                    <img src="../img/laptopguy.jpg" id="bg" alt="welcome">
                    <div class="centered">Find the most optimal database by comparing db captures and replays.</div>
                </div>
            </div>            
        </div>
    </div>
    `);
});


/**
 * Function that creates a modal with instructions on how to create an instance profile
 * @param
 * @return {string}
 */
function createInstanceProfHelpModal(selector) {
    return `            
                <div class="modal-dialog">
                    <div class="modal-content">
                        <div class="modal-header">
                            <button type="button" class="close" data-dismiss="modal">&times;</button>
                            <h4 class="modal-title">Instance Profile Instructions</h4>
                        </div>
                        <div class="modal-body">
                            <p><b>Getting to IAM</b></p>
                            <ol>
                                <li>Go to https://aws.amazon.com</li>
                                <li>In top right corner, click My Account then click AWS Management Console from the drop down</li>
                                <li>Under Services click IAM</li>
                            </ol>
                            <p><b>Creating a Policy</b></p>
                            <ol>
                                <li>Click Policies in the sidebar</li>
                                <li>Click Create Policy</li>
                                <li>Click the JSON tab and replace “Statement: []” with the text from the Policy JSON document 
                                    <a href="https://github.com/CPSECapstone/TeamTitansCRT/wiki/Policy-Statement">HERE</a>
                                </li>
                                <li>Click Review Policy</li>
                                <li>Name the policy mycrt</li>
                                <li>Click Create Policy</li>
                            </ol>
                            <p><b>Creating a Role</b></p>
                            <ol>
                                <li>Click on Roles in the sidebar</li>
                                <li>Click Create role</li>
                                <li>Under AWS service click EC2</li>
                                <li>Click EC2 under use case</li>
                                <li>Click Next</li>
                                <li>Search for the mycrt policy created in the steps above</li>
                                <li>Click Next</li>
                                <li>Name the role mycrt</li>
                                <li>Click Create role</li>
                            </ol>
                            <p><b>Applying Role to EC2 Instance</b></p>
                            <ol>
                                <li>Under Services click EC2</li>
                                <li>Click Instances in the sidebar</li>
                                <li>Right-click the instance the program will run on and under Instance Settings click Attach/Replace IAM Role</li>
                                    <i>You can attach an IAM role when creating the EC2 instance in Step 3</i>
                                <li>Attach the mycrt IAM role</li>
                                <li>Apply changes</li>
                            </ol>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-default gray" data-dismiss="modal">Close</button>
                        </div>
                    </div>
            </div>`;
}