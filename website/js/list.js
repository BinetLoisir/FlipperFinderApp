//Intialize PinMyBalls Back4App.
Parse.initialize("wx8ZJI9628FDGq39REy6rMlZjKdP5ERUMXjZpqjE", "HVHpDx5BgQG54UDLdZhLv7cFoontQUA8eIE8YC2D");
Parse.serverURL = 'https://parseapi.back4app.com';

var x = document.getElementById("demo");
var tip = document.getElementById("tip");
let liste = document.getElementById("myList");
let button = document.getElementById("searchButton");

const defaultLocation = new Parse.GeoPoint({
    latitude: 48.883461,
    longitude: 2.340561
});
var MyLocation;
getLocation();


function getLocation() {
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(showPosition);
    } else {
        x.innerHTML = "Geolocation is not supported by this browser.";
        MyLocation = defaultLocation;
    }
}

function showPosition(position) {
    console.log("Positon: " + "(" + position.coords.latitude +
        "," + position.coords.longitude + ")");
    MyLocation = new Parse.GeoPoint({
        latitude: position.coords.latitude,
        longitude: position.coords.longitude
    });
    //UI update
    tip.innerText="";
    button.disabled = false;
}

var list = [];

function searchAround() {
    while (liste.firstChild) {
        liste.removeChild(liste.lastChild);
    }
    list = getFlippers(MyLocation, 40, 50);
    //list = getEnseignes(defaultLocation, 100000, 20);
}

//-- -- -- -- -- - Parse interactions -- -- -- -- -- -

var Flipper = Parse.Object.extend("FLIPPER");
var Enseigne = Parse.Object.extend("ENSEIGNE");
var Modele = Parse.Object.extend("MODELE_FLIPPER");

//Get the nearby flippers in a given radius
function getFlippers(center, radius, maxflippers) {

    //Definition of the geobox query  -- NOT USED
    var geoboxquery = new Parse.Query(Enseigne);
    geoboxquery.withinGeoBox(
        "ENS_GEO",
        new Parse.GeoPoint({
            latitude: center.latitude - 0.01,
            longitude: center.longitude - 0.01
        }),
        new Parse.GeoPoint({
            latitude: center.latitude + 0.01,
            longitude: center.longitude + 0.01
        })
    );

    //Definition of WithinKm query
    var withinkmquery = new Parse.Query(Enseigne);
    withinkmquery.withinKilometers("ENS_GEO", center, radius, true);

    //Definition of the FlipperQuery
    var query = new Parse.Query(Flipper);
    query.equalTo("FLIP_ACTIF", true);
    query.matchesQuery("FLIP_ENSEIGNE_P", withinkmquery);
    query.include("FLIP_ENSEIGNE_P");
    query.include("FLIP_MODELE_P");
    query.limit(maxflippers);

    /* 
    withinkmquery.find({
         success: function (results) {
             if (results.length > 0) {
                 console.log("Successfully retrieved " + results.length + " enseignes.");
 
                 var ensArray = [];
                 for (var i = 0; i < results.length; i++) {
                     var ens = {
                         ens_ObjId: results[i].id,
                         ens_id: results[i].get(enseigneFields.id),
                         ens_name: results[i].get(enseigneFields.name),
                         ens_address: results[i].get(enseigneFields.address),
                         ens_cp: results[i].get(enseigneFields.postcode),
                         ens_city: results[i].get(enseigneFields.city),
                         ens_country: results[i].get(enseigneFields.country),
                         ens_latlng: results[i].get(enseigneFields.latlng),
                         ens_lat: results[i].get(enseigneFields.lat),
                         ens_lng: results[i].get(enseigneFields.lng),
                         ens_nbflips: results[i].get(enseigneFields.nbflips)
                     }
                     ensArray.push(ens);
                     let li = document.createElement("li");
                     var lat1 = center.latitude;
                     var lng1 = center.longitude;
                     var lat2 = ens.ens_lat;
                     var lng2 = ens.ens_lng;
 
                     var dist = Math.round(10 * distance(lat1, lng1, lat2, lng2)) / 10;
                     li.innerText = ens.ens_name + " " + ens.ens_city + " --dist: " + dist + "km";
                     liste.appendChild(li);
                 }
             } else {
                 snack("Pas de flippers aux environs");
             }
 
 
         },
         error: function (error) {
             alert("Error: " + error.code + " " + error.message);
             console.log("Error: " + error.code + " " + error.message);
         }
     })
     */

    query.find({
        success: function (results) {
            console.log("Successfully retrieved " + results.length + " flippers.");
            //console.log(results);
            //console.log(results[0]);

            if (results.length > 0) {
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
                        flip_objectId: results[i].id,
                        flip_distance: 0,
                        flip_updatedAt: results[i].updatedAt
                    
                    }
                    var lat1 = center.latitude;
                    var lng1 = center.longitude;
                    var lat2 = flip.lat;
                    var lng2 = flip.lng;
                    var dist = Math.round(10 * distance(lat1, lng1, lat2, lng2)) / 10;
                    flip.flip_distance = dist;

                    flipperArray.push(flip);

                }
                //var snackText = "Résultats de la recherche : " + results.length + " flippers.";
                //snack(snackText);

                flipperArray.sort((a, b) => a.flip_distance - b.flip_distance);

                populateList(flipperArray);

                return flipperArray;

            } else {
                let err = document.createElement("li");
                err.className = "list-group-item";
                err.innerText = "Pas de flippers aux environs";
                liste.appendChild(err);

                //snack("Pas de flippers aux environs");
            }
        },
        error: function (error) {
            alert("Error: " + error.code + " " + error.message);
            console.log("Error: " + error.code + " " + error.message);
        }

    });
}

//Get the nearby Enseignes in a given radius
function getEnseignes(center, radius, maxEnseigne) {
    var query = new Parse.Query(Enseigne);
    query.withinKilometers("ENS_GEO", center, radius, true);
    query.limit(maxEnseigne);
    query.find({
        success: function (results) {
            console.log("Successfully retrieved " + results.length + " enseignes.");

            if (results.length > 0) {
                var ensArray = [];

                for (var i = 0; i < results.length; i++) {
                    var ens = {
                        ens_ObjId: results[i].id,
                        ens_id: results[i].get(enseigneFields.id),
                        ens_name: results[i].get(enseigneFields.name),
                        ens_address: results[i].get(enseigneFields.address),
                        ens_cp: results[i].get(enseigneFields.postcode),
                        ens_city: results[i].get(enseigneFields.city),
                        ens_country: results[i].get(enseigneFields.country),
                        ens_latlng: results[i].get(enseigneFields.latlng),
                        ens_lat: results[i].get(enseigneFields.lat),
                        ens_lng: results[i].get(enseigneFields.lng),
                        ens_nbflips: results[i].get(enseigneFields.nbflips)
                    }
                    ensArray.push(ens);
                    let li = document.createElement("li");
                    var dist = distance(22, 39, 21, 38);
                    var lat1 = center.latitude;
                    var lng1 = center.longitude;
                    var lat2 = ens.ens_lat;
                    var lng2 = ens.ens_lng;

                    var dist = Math.round(distance(lat1, lng1, lat2, lng2));






                    //var distance = distance(MyLocation.latitude, MyLocation.longitude, ens.ens_lat, ens.ens_lng);

                    li.innerText = ens.ens_name + " " + ens.ens_city + " --dist: " + dist + "km";
                    liste.appendChild(li);
                }

                var snackText = "Résultats de la recherche : " + results.length + " enseignes.";
                snack(snackText);

                return ensArray;
            } else {
                snack("Pas de flippers aux environs");
            }
        },
        error: function (error) {
            alert("Error: " + error.code + " " + error.message);
            console.log("Error: " + error.code + " " + error.message);
        }

    });


}

//-- -- -- -- -- - UI -- -- -- -- -- --

function populateList(list) {

    list.forEach(flip => {
        let li = document.createElement("li");
        li.className = "list-group-item";
        liste.appendChild(li);


        let card = document.createElement("div");
        card.className = "card";
        li.appendChild(card);

        let cardheader = document.createElement('div');
        cardheader.className = "card-header d-flex justify-content-between align-items-center bg-primary text-white";
        cardheader.innerText = flip.modele_nom + ", " + flip.modele_marque + " (" + flip.modele_annee + ")";
        card.appendChild(cardheader);


        let cardheaderright = document.createElement('span');
        cardheaderright.className = "badge bg-dark";
        let d = flip.flip_distance;
        let text = d > 10 ? Math.round(d) + "km" : d + "km";
        cardheaderright.innerText = text;
        cardheader.appendChild(cardheaderright);


        let cardbody = document.createElement('div');
        cardbody.className = "card-body";
        card.appendChild(cardbody);

        let title = document.createElement('h6');
        title.className = "card-title";
        title.innerText = flip.ens_nom;
        cardbody.appendChild(title);

        let subtitle = document.createElement('h7');
        subtitle.className = "card-subtitle mb-2 text-muted";
        subtitle.innerText = flip.ens_adresse + " " + flip.ens_cp + " " + flip.ens_ville;
        cardbody.appendChild(subtitle);

        let cardfooter = document.createElement('h9');
        cardfooter.className = "card-footer text-muted";
        cardfooter.innerHTML = vuSince(flip.flip_updatedAt);
        card.appendChild(cardfooter);


    });





}

//-- -- -- -- -- - Const-- -- -- -- -- -

const modeleFields = {
    id: "MOFL_ID",
    name: "MOFL_NOM",
    year: "MOFL_ANNEE_LANCEMENT",
    brand: "MOFL_MARQUE"
};

const flipperFields = {
    id: "FLIP_ID",
    modele: "FLIP_MODELE",
    modele_p: "FLIP_MODELE_P",
    enseigne: "FLIP_ENSEIGNE",
    enseigne_p: "FLIP_ENSEIGNE_P",
    datemaj: "FLIP_DATMAJ",
    actif: "FLIP_ACTIF"
};

const enseigneFields = {
    id: "ENS_ID",
    name: "ENS_NOM",
    datemaj: "ENS_DATMAJ",
    address: "ENS_ADRESSE",
    postcode: "ENS_CODE_POSTAL",
    city: "ENS_VILLE",
    country: "ENS_PAYS",
    latlng: "ENS_GEO",
    lat: "ENS_LATITUDE",
    lng: "ENS_LONGITUDE",
    nbflips: "ENS_NBFLIPS"
};

const commentFields = {
    id: "COMM_ID",
    date: "COMM_DATE",
    text: "COMM_TEXTE",
    type: "COMM_TYPE",
    pseudo: "COMM_PSEUDO",
    actif: "COMM_ACTIF",
    flipperid: "COMM_FLIPPER_ID",
    flipperid_p: "COMM_FLIPPER_ID_P"
};


//-- -- -- -- -- - Helper functions-- -- -- -- -- -

//Format date as YYYY/MM/DD
function formatDate(date) {
    var d = new Date(date),
        month = '' + (d.getMonth() + 1),
        day = '' + d.getDate(),
        year = d.getFullYear();

    if (month.length < 2) month = '0' + month;
    if (day.length < 2) day = '0' + day;

    return [year, month, day].join('/');
}

function diffDateDays(date){
    var today = new Date();
    var Difference_In_Time = today.getTime() - date.getTime();
    var Difference_In_Days = Math.floor(Difference_In_Time / (1000 * 3600 * 24));
    return Difference_In_Days;
}

function vuSince(date){
    var days = diffDateDays(date);
    if (days > 360){
        return '<i class="fas fa-exclamation-triangle"></i> Vu il y a '+ days + ' jours';
    }
    switch(days){
        case 0 : return "Vu aujourdhui";
        break;
        case 1 : return "Vu hier";
        break;
        default : return "Vu il y a "+days+" jours";
    }
}

//This function takes in latitude and longitude of two location and returns the distance between them (in km)
function distance(lat1, lon1, lat2, lon2) {
    var R = 6371; // km
    var dLat = toRad(lat2 - lat1);
    var dLon = toRad(lon2 - lon1);
    var lat1 = toRad(lat1);
    var lat2 = toRad(lat2);

    var a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
    var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    var d = R * c;
    //console.log(d);
    return d;
}

// Converts numeric degrees to radians
function toRad(Value) {
    return Value * Math.PI / 180;
}
