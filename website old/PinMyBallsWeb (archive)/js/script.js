document.addEventListener('DOMContentLoaded', function () {
    if (document.querySelectorAll('#map').length > 0) {
        if (document.querySelector('html').lang)
            lang = document.querySelector('html').lang;
        else
            lang = 'en';

        var js_file = document.createElement('script');
        js_file.type = 'text/javascript';
        js_file.src = 'https://maps.googleapis.com/maps/api/js?callback=initMap&key=AIzaSyBWzLO7XJTK0qp3hWkX599YdiUWGc_yFYc&libraries=places&language=' + lang;
        document.getElementsByTagName('head')[0].appendChild(js_file);
    }
});

var map;
var markers = [];
var bounds, ne, sw;

var allFlippers = [];

Parse.initialize("wx8ZJI9628FDGq39REy6rMlZjKdP5ERUMXjZpqjE", "HVHpDx5BgQG54UDLdZhLv7cFoontQUA8eIE8YC2D");
Parse.serverURL = 'https://parseapi.back4app.com';
// Simple syntax to create a new subclass of Parse.Object.
var Flipper = Parse.Object.extend("FLIPPER");
var Enseigne = Parse.Object.extend("ENSEIGNE");
var myLocation = new Parse.GeoPoint({
    latitude: 48.883461,
    longitude: 2.340561
});

var input = /** @type {!HTMLInputElement} */ (
    document.getElementById('pac-input'));

function initMap() {
    var location = new google.maps.LatLng(48.883461, 2.340561);
    map = new google.maps.Map(document.getElementById('map'), {
        center: location,
        zoom: 13,
        disableDefaultUI: true,
        zoomControl: true,
        scaleControl: true,
    });

    //Adds the location button and search buttons on the map  
    var myMarker = new google.maps.Marker({
        map: map,
        animation: google.maps.Animation.DROP,
        position: location
    });
    addYourLocationButton(map, myMarker);
    addSearchButton(map);

    //Add Autocomplete Bar
    // Create the search box and link it to the UI element.
    map.controls[google.maps.ControlPosition.TOP_CENTER].push(input);

    var autocomplete = new google.maps.places.Autocomplete(input);
    //autocomplete.setTypes(['address']);
    var marker = new google.maps.Marker({
        map: map,
        anchorPoint: new google.maps.Point(0, -29)
    });
    autocomplete.addListener('place_changed', function () {
        setMapOnAll(null);
        var place = autocomplete.getPlace();
        console.log("Place selected");
        if (!place.geometry) {
            // User entered the name of a Place that was not suggested and
            // pressed the Enter key, or the Place Details request failed.
            window.alert("No details available for input: '" + place.name + "'");
            return;
        }
        // If the place has a geometry, then present it on a map.
        if (place.geometry.viewport) {
            map.fitBounds(place.geometry.viewport);
        } else {
            map.setCenter(place.geometry.location);
            map.setZoom(17); // Why 17? Because it looks good.
        }

        marker.setIcon( /** @type {google.maps.Icon} */ ({
            url: place.icon,
            size: new google.maps.Size(71, 71),
            origin: new google.maps.Point(0, 0),
            anchor: new google.maps.Point(17, 34),
            scaledSize: new google.maps.Size(35, 35)
        }));
        marker.setPosition(place.geometry.location);
        marker.setVisible(true);

    });

    //Clear the autocomplete when click
    google.maps.event.addDomListener(input, 'click', function () {
        this.value = '';
    });


    //Load markers after map is loaded
    map.addListener('idle', function () {

        //Definition of the  Radius innerquery
        var radiusquery = new Parse.Query(Enseigne);
        var radiusKm = 100;
        radiusquery.withinKilometers("ENS_GEO", myLocation, radiusKm);

        //Defintion of Geobox innerquery
        var SW = new Parse.GeoPoint({
            latitude: map.getBounds().getSouthWest().lat(),
            longitude: map.getBounds().getSouthWest().lng()
        });
        var NE = new Parse.GeoPoint({
            latitude: map.getBounds().getNorthEast().lat(),
            longitude: map.getBounds().getNorthEast().lng()
        });
        var geoboxquery = new Parse.Query(Enseigne);
        geoboxquery.withinGeoBox("ENS_GEO", SW, NE);

        //Definition of the FlipperQuery
        var query = new Parse.Query(Flipper);
        query.equalTo("FLIP_ACTIF", true);
        query.matchesQuery("FLIP_ENSEIGNE_P", geoboxquery);
        query.include("FLIP_ENSEIGNE_P");
        query.include("FLIP_MODELE_P");
        query.limit(500);

        query.find({
            success: function (results) {
                console.log("Successfully retrieved " + results.length + " flippers.");

                if (results.length > 0) {
                    var modele_nom, modele_annee, modele_marque, ens_nom, ens_adresse, ens_cp, ens_ville, lat, lng, flip_datemaj, flip_id, flip_objectId;
                    var flipperArray = [];
                    for (var i = 0; i < results.length; i++) {
                        var flip = {
                            modele_nom: results[i].get("FLIP_MODELE_P").get("MOFL_NOM"),
                            modele_annee: results[i].get("FLIP_MODELE_P").get("MOFL_ANNEE_LANCEMENT"),
                            modele_marque: results[i].get("FLIP_MODELE_P").get("MOFL_MARQUE"),
                            ens_nom: results[i].get("FLIP_ENSEIGNE_P").get("ENS_NOM"),
                            ens_adresse: results[i].get("FLIP_ENSEIGNE_P").get("ENS_ADRESSE"),
                            ens_cp: results[i].get("FLIP_ENSEIGNE_P").get("ENS_CODE_POSTAL"),
                            ens_ville: results[i].get("FLIP_ENSEIGNE_P").get("ENS_VILLE"),
                            lat: results[i].get("FLIP_ENSEIGNE_P").get("ENS_LATITUDE"),
                            lng: results[i].get("FLIP_ENSEIGNE_P").get("ENS_LONGITUDE"),
                            flip_datemaj: results[i].get("FLIP_DATMAJ"),
                            flip_id: results[i].get("FLIP_ID"),
                            flip_objectId: results[i].id
                        };
                        flipperArray.push(flip);
                    }

                    var snackText = results.length + " flippers trouvés.";
                    //snack(snackText);
                    plotMarkers(flipperArray);
                } else {
                    alert("Pas de flippers aux environs");
                }
            },
            error: function (error) {
                alert("Error: " + error.code + " " + error.message);
                console.log("Error: " + error.code + " " + error.message);
            }
        });
    });







    //OLD QUERY
    /*query.find({
        success: function (results) {
            console.log("Successfully retrieved " + results.length + " flippers.");
            //console.log(results);
            if (results.length > 0) {
                var modele_nom, modele_annee, modele_marque, ens_nom, ens_adresse, ens_cp, ens_ville, lat, lng, flip_datemaj, flip_id, flip_objectId;

                var flipperArray = [];

                for (var i = 0; i < results.length; i++) {
                    var flip = {
                        modele_nom: results[i].get("FLIP_MODELE_P").get("MOFL_NOM"),
                        modele_annee: results[i].get("FLIP_MODELE_P").get("MOFL_ANNEE_LANCEMENT"),
                        modele_marque: results[i].get("FLIP_MODELE_P").get("MOFL_MARQUE"),
                        ens_nom: results[i].get("FLIP_ENSEIGNE_P").get("ENS_NOM"),
                        ens_adresse: results[i].get("FLIP_ENSEIGNE_P").get("ENS_ADRESSE"),
                        ens_cp: results[i].get("FLIP_ENSEIGNE_P").get("ENS_CODE_POSTAL"),
                        ens_ville: results[i].get("FLIP_ENSEIGNE_P").get("ENS_VILLE"),
                        lat: results[i].get("FLIP_ENSEIGNE_P").get("ENS_LATITUDE"),
                        lng: results[i].get("FLIP_ENSEIGNE_P").get("ENS_LONGITUDE"),
                        flip_datemaj: results[i].get("FLIP_DATMAJ"),
                        flip_id: results[i].get("FLIP_ID"),
                        flip_objectId: results[i].id

                    }
                    flipperArray.push(flip);
                }
                plotMarkers(flipperArray);
            } else {
                alert("Pas de flippers aux environs");
            }
        },
        error: function (error) {
            alert("Error: " + error.code + " " + error.message);
            console.log("Error: " + error.code + " " + error.message);
        }

    });*/

    map.addListener('dragend', function () {

        /*
                var newCenter = new Parse.GeoPoint({
                    latitude: map.getBounds().getCenter().lat(),
                    longitude: map.getBounds().getCenter().lng()
                });

                var query = new Parse.Query(Flipper);
                var innerquery = new Parse.Query(Enseigne);
                innerquery.withinKilometers("ENS_GEO", newCenter, radiusKm);
                query.equalTo("FLIP_ACTIF", true);
                query.limit(200);
                query.matchesQuery("FLIP_ENSEIGNE_P", innerquery);
                query.include("FLIP_ENSEIGNE_P");
                query.include("FLIP_MODELE_P");

                query.find({
                    success: function (results) {
                        console.log("Successfully retrieved " + results.length + " flippers.");
                        //console.log(results);
                        if (results.length > 0) {
                            var ens_nom, ens_adresse, ens_cp, ens_ville, ens_nom, modele_nom;

                            var flipperArray = [];

                            for (var i = 0; i < results.length; i++) {
                                var flip = {
                                    modele_nom: results[i].get("FLIP_MODELE_P").get("MOFL_NOM"),
                                    modele_annee: results[i].get("FLIP_MODELE_P").get("MOFL_ANNEE_LANCEMENT"),
                                    modele_marque: results[i].get("FLIP_MODELE_P").get("MOFL_MARQUE"),
                                    ens_nom: results[i].get("FLIP_ENSEIGNE_P").get("ENS_NOM"),
                                    ens_adresse: results[i].get("FLIP_ENSEIGNE_P").get("ENS_ADRESSE"),
                                    ens_cp: results[i].get("FLIP_ENSEIGNE_P").get("ENS_CODE_POSTAL"),
                                    ens_ville: results[i].get("FLIP_ENSEIGNE_P").get("ENS_VILLE"),
                                    lat: results[i].get("FLIP_ENSEIGNE_P").get("ENS_LATITUDE"),
                                    lng: results[i].get("FLIP_ENSEIGNE_P").get("ENS_LONGITUDE"),
                                    flip_datemaj: results[i].get("FLIP_DATMAJ"),
                                    objectID: results[i].id,
                                    flip_id: results[i].get("FLIP_ID")

                                }
                                flipperArray.push(flip);
                            }
                            plotMarkers(flipperArray);
                        } else {
                            alert("Pas de flippers aux environs");
                        }
                    },
                    error: function (error) {
                        alert("Error: " + error.code + " " + error.message);
                        console.log("Error: " + error.code + " " + error.message);
                    }

                });
                */

    });

}

//var markers;
var bounds;

function plotMarkers(m) {
    //markers = [];
    bounds = new google.maps.LatLngBounds();
    //console.log("Plotting : " + m.length + " markers.")
    m.forEach(function (marker) {
        var position = new google.maps.LatLng(marker.lat, marker.lng);
        var infowindow = new google.maps.InfoWindow({
            content: contentInfoW(
                marker.flip_objectId,
                marker.flip_id,
                marker.ens_nom,
                marker.ens_adresse,
                marker.ens_cp,
                marker.ens_ville,
                marker.modele_nom,
                marker.modele_annee,
                marker.modele_marque,
                makeDate(marker.flip_datemaj).toLocaleDateString())
        });

        var mapMarker = new google.maps.Marker({
            position: position,
            map: map,
            title: marker.modele_nom,
            //animation: google.maps.Animation.DROP,
            icon: iconSelect(makeDate(marker.flip_datemaj))
        });

        markers.push(mapMarker);

        mapMarker.addListener('click', function () {
            infowindow.open(map, mapMarker);
            console.log(marker.flip_objectId + " : " + marker.modele_nom + ' @ ' + marker.ens_nom);
        });

        //bounds.extend(position);
    });

    //map.fitBounds(bounds);
};

function contentInfoW(flip_objectId, flip_id, enseigne, addresse, cp, ville, modele, annee, marque, datemaj) {
    flip_objectId = "'" + flip_objectId + "'";
    var args = flip_id;
    //var mailto = "'" + emailMaker(flip_id, modele, enseigne, ville) + "'";
    var mailto = "\'location.href='mailto:flipper.finder2@gmail.com';\'"
    return contentString = '<div id="iw-container">' +
        '<div class = iw-title>' + enseigne + '</div>' +
        '<div class = iw-address>' + addresse + ', ' + cp + ' ' + ville + '</div>' +
        '<br>' +
        '<div class="iw-content">' +
        '<a href="#">' + modele + '</a>&nbsp<a href="#" data-toggle="tooltip" title="Actualiser!"><i class="fa fa-fw fa-refresh"></i></a>&nbsp<a href="#" data-toggle="tooltip" title="Supprimer"><i class="fa fa-fw fa-trash"></i></a>' +
        '<br>' + marque + ' (' + annee + ')' +
        '<div class="iw-datemaj">' + '<p>' + datemaj + '</p> </div>' +
        '</div>' +
        '<div>' +
        '<button id="buttonUpdate" class="btn btn-info btn-sm" onclick="updateFlip(' + flip_objectId + ')">Confirmer ' +
        '</button>' +
        '<button id="buttonDelete" class="btn btn-warning btn-sm" onclick="sendMail(' + args + '); return false;">Supprimer ' +
        '</button>' +
        '</div>' +
        '</div>';
};

//function to make Date object from FLIP_DATEMAJ. 
function makeDate(dateString) {
    var parts = dateString.split("/");
    var date = new Date(parts[0], parts[1] - 1, parts[2]);
    return date;
};
//function to add days to a given date. 
function addDays(startDate, numberOfDays) {
    var returnDate = new Date(
        startDate.getFullYear(),
        startDate.getMonth(),
        startDate.getDate() + numberOfDays,
        startDate.getHours(),
        startDate.getMinutes(),
        startDate.getSeconds());
    return returnDate;
}
//function to remove days to a given date.
function removeDays(startDate, numberOfDays) {
    var returnDate = new Date(
        startDate.getFullYear(),
        startDate.getMonth(),
        startDate.getDate() - numberOfDays,
        startDate.getHours(),
        startDate.getMinutes(),
        startDate.getSeconds());
    return returnDate;
}
//function to calculate the number of days between 2 Dates.
function diffDays(date1, date2) {
    var oneDay = 24 * 60 * 60 * 1000;
    return Math.round(Math.abs((date1.getTime() - date2.getTime()) / (oneDay)));
}
//function to return map icon based on FLIP_DATEMAJ.
function iconSelect(datemaj) {
    var today = new Date();
    var delta = diffDays(today, datemaj);
    var img = "img/ic_flipmarker_new_mini.png";

    if (delta > 7) {
        img = "img/ic_flipmarker_blue_mini.png";
    }
    if (delta > 60) {
        img = "img/ic_flipmarker_lightblue_mini.png";
    }
    if (delta > 360) {
        img = "img/ic_flipmarker_grey_mini.png"
    }
    return img;
}


function addSearchButton(map) {
    var controlDiv = document.createElement('div');

    var firstChild = document.createElement('button');
    firstChild.style.backgroundColor = '#fff';
    firstChild.style.border = 'none';
    firstChild.style.outline = 'none';
    firstChild.style.width = '28px';
    firstChild.style.height = '28px';
    firstChild.style.borderRadius = '2px';
    firstChild.style.boxShadow = '0 1px 4px rgba(0,0,0,0.3)';
    firstChild.style.cursor = 'pointer';
    firstChild.style.marginRight = '10px';
    firstChild.style.padding = '0';
    firstChild.title = 'Search';
    controlDiv.appendChild(firstChild);

    var secondChild = document.createElement('div');
    secondChild.innerHTML = "<i class='material-icons'> search </i>";
    secondChild.style.margin = '2px';

    firstChild.appendChild(secondChild);

    google.maps.event.addListener(map, 'center_changed', function () {
        secondChild.style['background-position'] = '0 0';
        secondChild.style.color = 'Grey';
    });

    firstChild.addEventListener('click', function () {
        secondChild.style.color = 'LightGrey';

        var newSW = new Parse.GeoPoint({
            latitude: map.getBounds().getSouthWest().lat(),
            longitude: map.getBounds().getSouthWest().lng()
        });

        var newNE = new Parse.GeoPoint({
            latitude: map.getBounds().getNorthEast().lat(),
            longitude: map.getBounds().getNorthEast().lng()
        });

        var query = new Parse.Query(Flipper);
        var innerquery = new Parse.Query(Enseigne);
        innerquery.withinGeoBox("ENS_GEO", newSW, newNE);
        query.equalTo("FLIP_ACTIF", true);
        query.limit(500);
        query.matchesQuery("FLIP_ENSEIGNE_P", innerquery);
        query.include("FLIP_ENSEIGNE_P");
        query.include("FLIP_MODELE_P");

        query.find({
            success: function (results) {
                console.log("Successfully retrieved " + results.length + " flippers.");
                //console.log(results);
                if (results.length > 0) {
                    var modele_nom, modele_annee, modele_marque, ens_nom, ens_adresse, ens_cp, ens_ville, lat, lng, flip_datemaj, flip_id, flip_objectId;

                    var flipperArray = [];

                    for (var i = 0; i < results.length; i++) {
                        var flip = {
                            modele_nom: results[i].get("FLIP_MODELE_P").get("MOFL_NOM"),
                            modele_annee: results[i].get("FLIP_MODELE_P").get("MOFL_ANNEE_LANCEMENT"),
                            modele_marque: results[i].get("FLIP_MODELE_P").get("MOFL_MARQUE"),
                            ens_nom: results[i].get("FLIP_ENSEIGNE_P").get("ENS_NOM"),
                            ens_adresse: results[i].get("FLIP_ENSEIGNE_P").get("ENS_ADRESSE"),
                            ens_cp: results[i].get("FLIP_ENSEIGNE_P").get("ENS_CODE_POSTAL"),
                            ens_ville: results[i].get("FLIP_ENSEIGNE_P").get("ENS_VILLE"),
                            lat: results[i].get("FLIP_ENSEIGNE_P").get("ENS_LATITUDE"),
                            lng: results[i].get("FLIP_ENSEIGNE_P").get("ENS_LONGITUDE"),
                            flip_datemaj: results[i].get("FLIP_DATMAJ"),
                            flip_id: results[i].get("FLIP_ID"),
                            flip_objectId: results[i].id

                        }
                        flipperArray.push(flip);
                        var snackText = "Résultats de la recherche : " + results.length + " flippers.";
                        //snack(snackText);

                    }
                    plotMarkers(flipperArray);
                } else {
                    snack("Pas de flippers aux environs");
                }
            },
            error: function (error) {
                alert("Error: " + error.code + " " + error.message);
                console.log("Error: " + error.code + " " + error.message);
            }

        });


    });

    controlDiv.index = 2;
    map.controls[google.maps.ControlPosition.LEFT_TOP].push(controlDiv);
}

//  -------------START of The location button-----------------------------------------

function addYourLocationButton(map, marker) {
    var controlDiv = document.createElement('div');

    var firstChild = document.createElement('button');
    firstChild.style.backgroundColor = '#fff';
    firstChild.style.border = 'none';
    firstChild.style.outline = 'none';
    firstChild.style.width = '28px';
    firstChild.style.height = '28px';
    firstChild.style.borderRadius = '2px';
    firstChild.style.boxShadow = '0 1px 4px rgba(0,0,0,0.3)';
    firstChild.style.cursor = 'pointer';
    firstChild.style.marginRight = '10px';
    firstChild.style.padding = '0';
    firstChild.title = 'Your Location';
    controlDiv.appendChild(firstChild);

    var secondChild = document.createElement('div');
    secondChild.style.margin = '5px';
    secondChild.style.width = '18px';
    secondChild.style.height = '18px';
    secondChild.style.backgroundImage = 'url(https://maps.gstatic.com/tactile/mylocation/mylocation-sprite-2x.png)';
    secondChild.style.backgroundSize = '180px 18px';
    secondChild.style.backgroundPosition = '0 0';
    secondChild.style.backgroundRepeat = 'no-repeat';
    firstChild.appendChild(secondChild);

    google.maps.event.addListener(map, 'center_changed', function () {
        secondChild.style['background-position'] = '0 0';
    });

    firstChild.addEventListener('click', function () {
        var imgX = '0',
            animationInterval = setInterval(function () {
                imgX = imgX === '-18' ? '0' : '-18';
                secondChild.style['background-position'] = imgX + 'px 0';
            }, 500);

        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(function (position) {
                var latlng = new google.maps.LatLng(position.coords.latitude, position.coords.longitude);
                marker.setPosition(latlng);
                map.setCenter(latlng);
                clearInterval(animationInterval);
                secondChild.style['background-position'] = '-144px 0';
            });
        } else {
            clearInterval(animationInterval);
            secondChild.style['background-position'] = '0 0';
        }
    });

    controlDiv.index = 1;
    map.controls[google.maps.ControlPosition.RIGHT_TOP].push(controlDiv);
}
//  -------------END of The location button-----------------------------------------


//-----SNACKBAR-------
function snack(htmlContent) {
    // Get the snackbar DIV
    var x = document.getElementById("snackbar");
    // Add HMTL
    x.innerHTML = htmlContent;
    // Add the "show" class to DIV
    x.className = "show";

    // After 3 seconds, remove the show class from DIV
    setTimeout(function () {
        x.className = x.className.replace("show", "");
    }, 3000);
}



function updateFlip(flipID) {
    var today = new Date();
    var todayFormated = formatDate(today);
    var url = 'https://parseapi.back4app.com/classes/FLIPPER/' + flipID;
    console.log(url);
    var data = {
        FLIP_DATMAJ: todayFormated
    };
    postData(url, data)
        .then(data => console.log(data)) // JSON from `response.json()` call
        .catch(error => console.error(error))
}

function postData(url, data) {
    return fetch(url, {
            body: JSON.stringify(data),
            headers: {
                "X-Parse-Application-Id": "wx8ZJI9628FDGq39REy6rMlZjKdP5ERUMXjZpqjE",
                "X-Parse-Rest-Api-Key": "FUuJ7wft2vxbzFGHEZno6sx5AuuJEYl7yEYQlwCc",
                "content-type": 'application/json'
            },
            method: 'PUT',
        })
        .then(response => response.json())
}

function formatDate(date) {
    var d = new Date(date),
        month = '' + (d.getMonth() + 1),
        day = '' + d.getDate(),
        year = d.getFullYear();

    if (month.length < 2) month = '0' + month;
    if (day.length < 2) day = '0' + day;

    return [year, month, day].join('/');
}

function emailMaker(flip_id, modele_nom, ens_nom, ens_ville) {
    var email = "flipper.finder2@gmail.com";
    var subject = "Retrait du flipper no " + flip_id;
    var body = "ID : " + flip_id +
        "\nModèle : " + modele_nom +
        "\nDu : " + ens_nom +
        "\nA : " + ens_ville +
        "\nCe flipper n'existe plus!";
    return "mailto:" + email + "?subject=" + subject + "&body=" + body;
}


function sendMail(flip_id) {
    var link = "mailto:flipper.finder2@gmail.com" +
        "?subject=" + escape("Retrait du flipper no " + flip_id) +
        "&body=" + escape("ID : " + flip_id + "\nCe flipper n'existe plus");
    window.location.href = link;
}

function recursiveQuery(query, batchNumber, allObjects) {
    return simpleQuery(query, batchNumber).then(function (objects) {
        // concat the intermediate objects into the final array
        allObjects = allObjects.concat(objects);
        // if the objects length is 1000, it means that we are not at the end of the list
        if (objects.length === 1000) {
            batchNumber = batchNumber + 1;
            return recursiveQuery(query, batchNumber, allObjects);
        } else {
            return allObjects;
        }
    });
}

function simpleQuery(query, batchNumber) {
    query.limit(1000);
    query.skip(batchNumber * 1000);
    return query.find().then(
        function (objects) {
            return objects;
        },
        function (error) {
            return error;
        }
    );
}


// Sets the map on all markers in the array.
function setMapOnAll(map) {
    for (var i = 0; i < markers.length; i++) {
        markers[i].setMap(map);
    }
}
