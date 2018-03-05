$.get("nav.html", function(data){
    $(".nav-placeholder").replaceWith(data);
});
$.get("sidebar.html", function(data){
    $(".sidebar-placeholder").replaceWith(data);
});