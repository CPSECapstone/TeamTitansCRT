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
        </ul>
    </nav>`);
}

