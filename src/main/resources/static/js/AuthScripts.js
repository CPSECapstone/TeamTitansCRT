// Local debugging helper
var domain = window.location.href;
if (domain.includes("8080")) {
    domain = "http://localhost:8080";
} else {
    domain = "https://cp-fellowship.herokuapp.com";
}

function sendSignIn(username, password) {
    var creds = {
        "username": username,
        "password": password
    };
    $.ajax({
        url: domain + "/login",
        type: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        dataType: "text",
        data: JSON.stringify(creds),
        success: function(data) {
            window.sessionStorage.token = data;
            window.location.href = domain + "/main";
        },
        error: function(err) {
            console.log(err);
            // alert('error signing up, try again');
            var x = document.getElementById("errorMessage");
            if (x.style.display === 'none') {
                x.style.display = 'block';
            }
        }
    });
}

function sendSignUp(name, username, password, role) {
    var creds = {
        "name": name,
        "email": username,
        "password": password,
        "role": role
    };
    $.ajax({
        url: domain + "/user",
        type: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        dataType: "text",
        data: JSON.stringify(creds),
        success: function () {
            window.location.href = domain + "/sign-in";
            console.log("POOOOOP");
        },
        error: function(err) {
            console.log(err);
            // alert('error signing up, try again');
            var x = document.getElementById("errorMessage");
            if (x.style.display === 'none') {
                x.style.display = 'block';
            }
        }
    });
}

$(document).ready(function() {
    // logo redirects home
    $("#custom-bootstrap-menu > div > div.navbar-header > a").on("click", function() {
        window.location.href = domain;        
    });

    $("#signIn_Button").on("click", function () {
        var username = $("#signIn_EmailInput").val();
        var password = $("#signIn_PasswordInput").val();
        sendSignIn(username, password);
    });

    $("#signOut_Button").on("click", function () {
        sendSignOut();
    });

    $("#signUp_Button").on("click", function () {
        var name = $("#signUp_NameInput").val();
        var username = $("#signUp_EmailInput").val();
        var password = $("#signUp_PasswordInput").val();
        var role = $("#signUp_RoleInput").val();
        sendSignUp(name, username, password, role);
    });
});