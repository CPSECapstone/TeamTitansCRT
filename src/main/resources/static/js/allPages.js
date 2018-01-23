$(document).ready(function() {
    $("#btnSettings").on("click", function() {
        window.location.href = "settings";
    });
    $("#btnAnalyze").on("click", function() {
        window.location.href = "analyze";
    });
    $("#btnCapture").on("click", function() {
        window.location.href = "capture";
    });
    $("#btnDashboard").on("click", function() {
        window.location.href = "dashboard";
    });
    $("#btnLogout").on("click", function() {
        window.location.href = "login";
    });
});
