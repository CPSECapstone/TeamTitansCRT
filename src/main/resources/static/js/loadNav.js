$.get("components/nav.html", function(data){
    $(".nav-placeholder").replaceWith(data);
});

$.get("components/sidebar.html", function(data){
    $(".sidebar-placeholder").replaceWith(data);
});