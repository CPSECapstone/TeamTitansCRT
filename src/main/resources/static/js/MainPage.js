// Local debugging helper
var domain = window.location.href;
if (domain.includes("8080")) {
    domain = "http://localhost:8080";
} else {
    domain = "https://cp-fellowship.herokuapp.com";
}

class Comment {
    constructor(object) {
            this.id = object["id"];
            this.parentId = object["parentId"];
            this.timeStamp = object["timeStamp"];
            this.name = object["author"]["name"];
            this.message = object["message"];
    }
    
    getTemplate() {
        var template = '' +
            '<div id="' + this.id + '" class="panel-footer">' +
                this.innerTemplate() + this.message +
            '</div>';
        return template;
    }

    innerTemplate() {
        var template = '' +
        '<h3 class="panel-title"><em><u> ' + this.name +
            '</u></em></h3>';
        return template;
    }
}

class Post {
    constructor(object) {
        this.id = object["id"];

        this.name = object["author"]["name"];
        this.message = object["message"];
        this.likes = object["likes"].length;

        // This is for testing purposes
        var tempLikes = object["likes"];
        var likedBy = "";
        for(var i = 0; i < tempLikes.length; i++) {
            likedBy += tempLikes[i]["name"] + " ";
        }
        console.log("likedBy:  " + likedBy);

        this.image = object["image"];

        this.timestamp = object["timeStamp"];
        this.simpleTime = this.timestamp.replace("T", " ");
        this.simpleTime = new Date(this.simpleTime);
        this.simpleTime = $.format.prettyDate(this.simpleTime);

        this.tags = object["tags"];
        this.tags = this.tags.split(",");

        this.comments = object["comments"];
        if (!this.comments) {
            this.comments = 0;
        }
    }

    getTemplate() {
        var template = '' +
            '<div id="' + this.id + '" class="col-xs-12">' +
            this.innerTemplate() +
            '</div>';
        return template;
    }

    innerTemplate() {
        var template = '' +
            '<div class="panel post raised">' +
            '<div class="panel-heading">' +
            '<h3 class="panel-title"><em> ' + this.name +
            ' </em> <small class="pull-right"> ' + this.simpleTime +
            ' </small></h3>' +
            '</div>' +
            '<div class="panel-body">' +
            '<div class="well">' +
            this.message +
            '</div>' +
            '<div>' +
            '<img src="data:image/png;base64,' + this.image + '"/>' +
            '</div>' +
            '<ul class="tags list-inline pull-right">';
        for (var i = 0; i < this.tags.length; i++) {
            template += '<li><a id="tag" class="">' + this.tags[i] +
                '</a></li>';
        }
        template += '' +
            '</ul>' +
            '</div>' +
            '<div class="panel-footer">' +
            '<ul class="list-inline">' +
            '<li><a id="likeButton" class="btn btn-sm btn-default">' +
            this.likes +
            '    <span class="glyphicon glyphicon-heart"></span>  </a></li>' +
            '<li><a id="commentButton" class="btn btn-sm btn-default">' +
            '    <textarea id="commentTextarea" class="form-control"></textarea>' +
            '    <button href="javascript:void(0);" id="commentSubmit" class="btn btn-default">Submit Comment</button>' +
            '</ul>' +
            '</div>' +
            '</div>';
        return template;
    }
}

function showPosts(postList) {
    removeAllPosts();
    $.each(postList, function (index, object) {
        var post = new Post(object);
        $("#listArea").append(post.getTemplate());
        showAllComments(post.id);
    });
}

function removeAllPosts() {
    $("#listArea").children().remove();
}

function showAllPosts() {
    $.ajax({
        url: domain + "/posts",
        type: "GET",
        data: null,
        success: function(data) {
            showPosts(data);
        },
        error: function(err) {
            console.log(err);
        }
    });
}

function showComments(commentList) {
    // removeAllComments();
    $.each(commentList, function (index, object) {
        var comment = new Comment(object);
        console.log(comment);
        $("#listArea").children("#" + comment.parentId).children().append(comment.getTemplate());
        console.log("Got past showComments()");
    });
}

function removeAllComments() {
    $("#listArea").children().remove();
}

function showAllComments(id) {
    $.ajax({
        url: domain + "/comments/" + id,
        type: "GET",
        data: null,
        success: function(data) {
            showComments(data);
        },
        error: function(err) {
            console.log(err);
        }
    });
    console.log("Got past showAllComments()");
}

function showTaggedPosts(tags) {
    $.ajax({
        url: domain + "/posts/" + tags,
        type: "GET",
        data: null,
        success: function(data) {
            showPosts(data);
        },
        error: function(err) {
            console.log(err);
            // TODO alert user that the tag is not found
            showAllPosts();
        }
    });
}

function sendNewComment(input, id) {
    $.ajax({
        url: domain + "/comments/" + id,
        type: "POST",
        headers: {
            "Content-Type": "application/json",
            "X-Auth-Token": window.sessionStorage.getItem("token")
        },
        dataType: "json",
        data: JSON.stringify(input),
        success: function() {
            console.log("comment successful");
            showAllPosts();
        },
        error: function(err) {
            console.log("wth comments not working")
            console.log(err);
        }
    });
    console.log("postid = " + id);
    console.log("Got past sendNewComment()");
}


function sendNewPost(input) {
    $.ajax({
        url: domain + "/posts",
        type: "POST",
        headers: {
            "Content-Type": "application/json",
            "X-Auth-Token": window.sessionStorage.getItem("token")
        },
        dataType: "json",
        data: JSON.stringify(input),
        success: function() {
            console.log("post successful");
            showAllPosts();
        },
        error: function(err) {
            console.log(err);
        }
    });
}

function likePost(id) {
    $.ajax({
        url: domain + "/posts/" + id,
        type: "PUT",
        headers: {
            "Content-Type": "application/json",
            "X-Auth-Token": window.sessionStorage.getItem("token")
        },
        success: function() {
            console.log("like successful");
            showAllPosts();
        },
        error: function(err) {
            console.log(err);
        }
    });
}

function clearPostBox() {
    $("#postTextarea").val("");
    $("#postTags").val("");
    $("#image-file").val("");
    $('#imageUpload').attr("src", "data:image/png;base64,null");
}

function clearCommentBox() {
    $("#postCommentarea").val("");
}


function checkFileType(file) {
    var ext = file.name.split('.').pop().toLowerCase();
    if($.inArray(ext, ['gif','png','jpg','jpeg']) == -1) {
        alert('Invalid extension! Only images can be uploaded');
        return false;
    }
    return true;
}

function checkFileSize(file) {
    var mb = 2;
    var maxSize = 1024 * 1000 * mb;
    if(file.size > maxSize) {
        alert('Please select an image less than ' + mb + ' MB');
        return false;
    }
    return true;
}

$(document).ready(function() {

    // uncomment for production
    showAllPosts();

    var imageData;
    function imageIsLoaded(e) {
        //preview image
        $('#imageUpload').attr("src", e.target.result);
        imageData = e.target.result;
        //clean up the base64 string for db storage
        imageData = imageData.substring(imageData.indexOf(',')+1, imageData.length);
    };

    $("input[type=file]").change(function(){
        //file found, convert file to base64 string
        if (this.files && this.files[0]) {
            //check if file is an image and has a size less than 2MB
            var isImage = checkFileType(this.files[0]);
            var hasValidSize = checkFileSize(this.files[0]);

            if(isImage && hasValidSize) {
                var reader = new FileReader();
                reader.onload = imageIsLoaded;
                reader.readAsDataURL(this.files[0]);
            }
            else {
                $('#image-file').val("");
                imageData = null;
                $('#imageUpload').attr("src", "data:image/png;base64,null");
            }

            var reader = new FileReader();
            reader.onload = imageIsLoaded;
            reader.readAsDataURL(this.files[0]);
        }
        else {
            //file not found, clear image preview and image data
            $('#imageUpload').attr("src", "data:image/png;base64,null");
            imageData = null;
        }
    });



    // logo redirects home
    $("#custom-bootstrap-menu > div > div.navbar-header > a").on("click", function() {
        window.location.href = domain;        
    });

    $("#postSubmit").on("click", function() {
        var text = $("#postTextarea").val();
        var tagString = $("#postTags").val();
        var tags = tagString.replace(/ /g, '');

        var input = {
            message: text,
            tags: tags,
            image: imageData
        };
        sendNewPost(input);
        clearPostBox();
        imageData = null;
    });

    $("#listArea").on("click", "#commentSubmit", function() {
        var text = $(this).parent().children("#commentTextarea").val();
        console.log("comment button clicked");
        var div = $(this).parent().parent().parent().parent().parent().parent();
        var id = div[0].id;

        console.log("commentSubmit button clcked val: " + text + " id:" + id);
        var input = {
            message: text,
            parentId: id
        };
        sendNewComment(input, id);
        imageData = null;
    });

    $("#listArea").on("click", "#likeButton", function() {
        // TODO this needs to be fixed according to new layout
        var div = $(this).parent().parent().parent().parent().parent();
        var id = div[0].id;
        likePost(id);
    });

    $("#listArea").on("click", "#tag", function() {
        var val = $(this)[0].innerText;
        showTaggedPosts(val);
    });

    $("#searchBar").on("keydown", function(e) {
        if (e.which == 13) {
            var tags = $(this).val();
            showTaggedPosts(tags);
            $(this).val("");
        }
    });

    $("#signOut_Button").on("click", function() {
        delete window.sessionStorage.token;
        window.location.replace(domain + "/sign-out");
    });
});
