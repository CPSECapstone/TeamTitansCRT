$(function() {
    injectNavTop();
    injectSidebar();
});

// function that inject the nav html into any div with the placeholder class
function injectNavTop() {
    $("div.nav-placeholder").replaceWith(`
    <nav class="navbar navbar-custom z-depth-1" role="navigation">
        <div id="nav-container">
            <div class="navbar-header">
                <a class="navbar-brand menu-brand page-scroll" href="javascript:void(0)">Menu</a>
                <a class="navbar-brand crt-brand page-scroll" href="dashboard">MyCRT</a>
            </div>
        </div>
    </nav>`);
}

// function that inject the sidebar html into any div with the placeholder class
function injectSidebar() {
    $("div.sidebar-placeholder").replaceWith(`
    <nav class="navbar navbar-inverse navbar-fixed-top" id="sidebar-wrapper" role="navigation">
        <ul class="nav sidebar-nav">
            <li class="sidebar-brand">
                <a href="dashboard">
                    MyCRT
                </a>
            </li>
            <li>
                <a href="dashboard">Dashboard</a>
            </li>
            <li>
                <a href="capture">My Captures</a>
            </li>
            <li>
                <a href="replay">My Replays</a>
            </li>
            <li>
                <a href="analyze">Analyze Results</a>
            </li>
            <li>
                <a href="settings">Settings</a>
            </li>
            <li>
                <a href="#" onclick="window.location.href='https://docs.google.com/forms/d/e/1FAIpQLSd8HFc4JjisWfcFUrVAeJda42GRt9ihAEBaHkY3I2m7RLbjcg/viewform?usp=sf_link'">Give Feedback</a>
            </li>
            <li>
                <a href="#" onclick="window.location.href='https://docs.google.com/forms/d/e/1FAIpQLScw_vf00MAgk1lWAYC7AUOD_jtb8D_bj-goap96NdvqzRNPiw/viewform?usp=sf_link'">Report a Bug</a>
            </li>
        </ul>
        <ul id="feedback_sidebar_button" class="nav nav-sidebar">
            <li><a href="login">Log out</a></li>
        </ul>
    </nav>`);
}


