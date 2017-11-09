/*!
 * Start Bootstrap - Grayscale Bootstrap Theme (http://startbootstrap.com)
 * Code licensed under the Apache License v2.0.
 * For details, see http://www.apache.org/licenses/LICENSE-2.0.
 */

// jQuery to collapse the navbar on scroll
function collapseNavbar() {
    if ($(".navbar").offset().top > 50) {
        $(".navbar-fixed-top").addClass("top-nav-collapse");
    } else {
        $(".navbar-fixed-top").removeClass("top-nav-collapse");
    }
}

$(window).scroll(collapseNavbar);
$(document).ready(collapseNavbar);

// jQuery for page scrolling feature - requires jQuery Easing plugin
$(function() {
    $('a.page-scroll').bind('click', function(event) {
        var $anchor = $(this);
        $('html, body').stop().animate({
            scrollTop: $($anchor.attr('href')).offset().top
        }, 1500, 'easeInOutExpo');
        event.preventDefault();
    });
});

// Closes the Responsive Menu on Menu Item Click
$('.navbar-collapse ul li a').click(function() {
    $(this).closest('.collapse').collapse('toggle');
});

// Google Maps Scripts
var map = null;
// When the window has finished loading create our google map below
google.maps.event.addDomListener(window, 'load', init);
google.maps.event.addDomListener(window, 'resize', function() {
    map.setCenter(new google.maps.LatLng(40, 10));
});

function init() {
    // Basic options for a simple Google Map
    // For more options see: https://developers.google.com/maps/documentation/javascript/reference#MapOptions
    var mapOptions = {
        // How zoomed in you want the map to start at (always required)
        zoom: 2,

        // The latitude and longitude to center the map (always required)
        center: new google.maps.LatLng(48, -8),

        // Disables the default Google Maps UI components
        disableDefaultUI: false,
        scrollwheel: false,
        draggable: true,
        mapTypeControl: false,
        streetViewControl: false,

        // How you would like to style the map. 
        // This is where you would paste any style found on Snazzy Maps.
        styles: [
            {
                "featureType": "all",
                "elementType": "geometry.fill",
                "stylers": [
                    {
                        "weight": "1.00"
                    }
                ]
            },
            {
                "featureType": "all",
                "elementType": "labels.text",
                "stylers": [
                    {
                        "visibility": "off"
                    }
                ]
            },
            {
                "featureType": "all",
                "elementType": "labels.icon",
                "stylers": [
                    {
                        "visibility": "off"
                    }
                ]
            },
            {
                "featureType": "landscape.natural",
                "elementType": "geometry.fill",
                "stylers": [
                    {
                        "hue": "#00ff26"
                        //color:"#ffffff"
                    }
                ]
            },
            {
                "featureType": "road.highway",
                "elementType": "geometry.fill",
                "stylers": [
                    {
                        //"color": "#b2ac83"
                        "color": "#ffffff"
                    }
                ]
            },
            {
                "featureType": "road.highway",
                "elementType": "geometry.stroke",
                "stylers": [
                    {
                        "color": "#b2ac83"
                    }
                ]
            },
            {
                "featureType": "road.highway",
                "elementType": "labels.icon",
                "stylers": [
                    {
                        "visibility": "off"
                    }
                ]
            },
            {
                "featureType": "water",
                "elementType": "geometry.fill",
                "stylers": [
                    {
                        "color": "#2895ab"
                    }
                ]
            }
        ]
    };

    // Get the HTML DOM element that will contain your map 
    // We are using a div with id="map" seen below in the <body>
    var mapElement = document.getElementById('map');

    // Create the Google Map using out element and options defined above
    map = new google.maps.Map(mapElement, mapOptions);


    // Custom Map Marker Icon - Customize the map-marker.png file to customize your icon

    var myLatLng = new google.maps.LatLng(37.773972, -122.431297);

    var locations = [
            ['Cal Poly SLO', 35.304926, -120.662537],
            ['MUAS', 48.155003, 11.556118],
            ['University of Canberra', -35.237916, 149.084119],
            ['Swinburne University of Technology', -37.822142, 145.038879],
            ['FH Joanneum Fachhochschul-Studiengangen, Graz', 47.069293, 15.409789],
            ['Seinӓjoen University of Applied Sciences, Seinӓjoki', 62.789595, 22.821829],
            ['LEcole dArchitecture de Paris-Val-de-Seine', 48.827429, 2.385183],
            ['Anhalt University of Applied Sciences & the Dessau Institute of Architecture (DIA)', 51.823863, 11.707696],
            ['HdM Stuttgart', 48.741974, 9.100759],
            ['University of Milan', 45.460083, 9.194616],
            ['MIP Polytecnico di Milano - School of Management', 45.503245, 9.155025],
            ['John Cabot University - Rome', 41.892617, 12.467706],
            ['Universidade Lusofona de Humanidades e Tecnologias, Lisbon', 38.757999, -9.153298],
            ['TECNUN, University of Navarra, San Sebastian', 43.304670, -2.010113],
            ['Chalmers University of Technology, Goetborg', 57.688368, 11.977769],
            ['Royal Institute of Technology (KTH), Stockholm', 59.349746, 18.070643],
            ['HSR Hochschule fur Technik, Rapperswil', 47.223368, 8.817363],
            ['Lucerne University of Appl Sci, School of Engineering and Architecture', 47.014907, 8.304564],
            ['Kadir Has Üniversitesi, Cibali, Istanbul', 41.024957, 28.958921],
            ['Leeds Beckett University', 53.803611, -1.547343],
            ['University of Birmingham', 52.450804, -1.930406]
        ];


    var image = 'img/marker.png'

    locations.forEach(function(location) {
        var marker = new google.maps.Marker({
            title: location[0],
            position: new google.maps.LatLng(location[1], location[2]),
            map: map,
            icon: image
        });
    });
}
