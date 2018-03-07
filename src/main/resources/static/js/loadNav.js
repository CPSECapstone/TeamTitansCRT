// function that inject the nav html into any div with the placeholder class
$.get("components/nav.html", function(data){
    $("div.nav-placeholder").replaceWith(data);
});

// function that inject the sidebar html into any div with the placeholder class
$.get("components/sidebar.html", function(data){
    $("div.sidebar-placeholder").replaceWith(data);
});