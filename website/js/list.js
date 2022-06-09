//Intialize PinMyBalls Back4App.
Parse.initialize("wx8ZJI9628FDGq39REy6rMlZjKdP5ERUMXjZpqjE", "HVHpDx5BgQG54UDLdZhLv7cFoontQUA8eIE8YC2D");
Parse.serverURL = 'https://parseapi.back4app.com';

var topLine = document.getElementById("tips");
var tipGeo = document.getElementById("tip-geo");
var tipQuery = document.getElementById("tip-query");
let liste = document.getElementById("myList");

//-- -- -- -- -- - Const-- -- -- -- -- -

var Flipper = Parse.Object.extend("FLIPPER")
var Enseigne = Parse.Object.extend("ENSEIGNE")
var Modele = Parse.Object.extend("MODELE_FLIPPER")
var Commentaire = Parse.Object.extend("COMMENTAIRE")
var FlipTrash = Parse.Object.extend("FLIPTRASH")


var modeleFields = {
    id: "MOFL_ID",
    name: "MOFL_NOM",
    year: "MOFL_ANNEE_LANCEMENT",
    brand: "MOFL_MARQUE"
};

var flipperFields = {
    id: "FLIP_ID",
    modele: "FLIP_MODELE",
    modele_p: "FLIP_MODELE_P",
    enseigne: "FLIP_ENSEIGNE",
    enseigne_p: "FLIP_ENSEIGNE_P",
    datemaj: "FLIP_DATMAJ",
    actif: "FLIP_ACTIF"
};

var enseigneFields = {
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

var commentFields = {
    id: "COMM_ID",
    date: "COMM_DATE",
    text: "COMM_TEXTE",
    type: "COMM_TYPE",
    pseudo: "COMM_PSEUDO",
    actif: "COMM_ACTIF",
    flipperid: "COMM_FLIPPER_ID",
    flipperid_p: "COMM_FLIPPER_ID_P"
};

var trashListFields = {
    id: "FLIP_ID",
    processed: "PROCESSED",
    pseudo: "PSEUDO",
};

const defaultLocation = new Parse.GeoPoint({
    latitude: 48.883461,
    longitude: 2.340561
});

//-------------END COMMON CONSTS------------

var myLocation;
//Comment for geolocalisation
//runWithoutGeoloc();
//getLocation();

loadList()

function loadList(parm1, param2) {
    getLocation()
    //runWithoutGeoloc()
}

function getLocation() {
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(success);
    } else {
        tipGeo.innerHTML = "Geolocation is not supported by this browser.";
        myLocation = defaultLocation;
    }
}

function success(position) {
    console.log(`Position obtained: ${position.coords.latitude}, ${position.coords.longitude})`)
    myLocation = new Parse.GeoPoint({
        latitude: position.coords.latitude,
        longitude: position.coords.longitude
    });
    //UI update
    tipGeo.style.display = "none"
    tipQuery.style.display = "block"
    searchAroundandDisplay()
}

function runWithoutGeoloc() {
    myLocation = defaultLocation;
    //UI update
    tipGeo.innerText = "Debugging. Geolocation disabled, using default location"
    searchAroundandDisplay()
}

var list = [];

function searchAroundandDisplay() {
    //clear existing list items
    while (liste.firstChild) {
        liste.removeChild(liste.lastChild);
    }
    list = getFlippers(myLocation, 40, 500);
    //needs to be 500 otherwise some pinballs will be missing
    //list = getEnseignes(defaultLocation, 100000, 20);
}

//-- -- -- -- -- - Parse interactions -- -- -- -- -- -

//Get x nearby flippers in a given center and radius
async function getFlippers(center, radius, maxflippers) {
    /*--------- NOT USED
    //Query definition : geobox query
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
    */

    //Query definition : WithinKm query
    var withinkmquery = new Parse.Query(Enseigne);
    withinkmquery.withinKilometers("ENS_GEO", center, radius, true);

    //Definition of the FlipperQuery
    var query = new Parse.Query(Flipper);
    query.equalTo(flipperFields.actif, true);
    query.matchesQuery(flipperFields.enseigne_p, withinkmquery);
    query.include(flipperFields.enseigne_p);
    query.include(flipperFields.modele_p);
    query.limit(maxflippers);

    //new way
    const flips = await query.find();
    var flipperArray = [];
    let ensMap = new Map();

    if (flips == null) {
        let err = document.createElement("li");
        err.className = "list-group-item";
        err.innerText = "Erreur de recherche";
        liste.appendChild(err);
        return
    }

    if (flips.length == 0) {
        let err = document.createElement("li");
        err.className = "list-group-item";
        err.innerText = "Pas de flippers aux environs";
        liste.appendChild(err);
        return

    }
    //Success
    console.log("Successfully retrieved " + flips.length + " flippers.");

    for (let i = 0; i < flips.length; i++) {
        var flip = flipFactory(flips[i])
        flip.flip_distance = distanceFromInKm(center.latitude, center.longitude, flip.lat, flip.lng)
        //console.log(flip)
        flipperArray.push(flip);

        //key of the map : ens_objectId
        var ensId = flip.ens_objectId;
        //tempFlipArray of flips in this enseigne
        var tempFlipArray = [flip];

        //if key is in map already
        if (ensMap.get(ensId) != undefined) {
            //add flip to the temporary array
            tempFlipArray = tempFlipArray.concat(ensMap.get(ensId));
        }
        //then set or update the key,value
        ensMap.set(ensId, tempFlipArray);
    }

    console.log("Obtained " + ensMap.size + " enseignes")

    //Sorting both array and map by distance
    flipperArray.sort((a, b) => a.flip_distance - b.flip_distance);
    let mapSort = new Map([...ensMap.entries()].sort((a, b) => a[1][0].flip_distance - b[1][0].flip_distance));

    //Get only the first 20
    const arrayTmp = Array.from(mapSort).slice(0, 20)
    mapSortSlice = new Map(arrayTmp)

    //UI update
    console.log("Displaying " + mapSortSlice.size + " enseignes")
    tipQuery.style.display = "none"
    populateListEns(mapSortSlice);

    return mapSortSlice;
}

//Get the nearby Enseignes in a given radius and populate list
//----------NOT USED------------------------------
/*
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






                    //var distance = distance(myLocation.latitude, myLocation.longitude, ens.ens_lat, ens.ens_lng);

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


//-- -- -- -- -- - UI -- -- -- -- -- -- -- -- -- -- --
//populate from list of flips
//--------NOT USED--OLD---------

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
*/

//populates from map of Enseigne
function populateListEns(map) {
    //console.log(map);
    //var i = 0
    map.forEach((flips, key) => {
        //console.log(i++)
        //console.log(flips[0].ens_nom + " : "+ flips[0].modele_nom);
        let li = document.createElement("li");
        li.className = "list-group-item list-enseigne-item list-big-card";
        liste.appendChild(li);

        let card = document.createElement("div");
        card.className = "card";
        li.appendChild(card);

        let cardheader = document.createElement('div');
        cardheader.className = "card-header d-flex justify-content-between align-items-center bg-primary text-white";
        cardheader.innerText = flips[0].ens_nom + ", " + flips[0].ens_ville;
        //cardheader.innerText = flip.modele_nom + ", " + flip.modele_marque + " (" + flip.modele_annee + ")";
        card.appendChild(cardheader);

        let cardheaderright = document.createElement('span');
        cardheaderright.className = "badge bg-dark";
        let d = flips[0].flip_distance;
        let text = d > 10 ? Math.round(d) + "km" : d + "km";
        cardheaderright.innerText = text;
        cardheader.appendChild(cardheaderright);

        let cardbody = document.createElement('div');
        cardbody.className = "card-body py-2";
        card.appendChild(cardbody);

        //let title = document.createElement('h6');
        //title.className = "card-title";
        //title.innerText = flips[0].modele_nom + ", " + flips[0].modele_marque + " (" + flips[0].modele_annee + ")";
        //cardbody.appendChild(title);

        let subtitle = document.createElement('p');
        subtitle.className = "card-subtitle text-muted ";
        subtitle.innerText = flips[0].ens_adresse + " " + flips[0].ens_cp + " " + flips[0].ens_ville;
        cardbody.appendChild(subtitle);

        let ul_flips = document.createElement("ul");
        ul_flips.className = "list-group";
        for (const flip of flips) {
            let li_flip = flipLiElement(flip);
            ul_flips.appendChild(li_flip);
            cardbody.appendChild(ul_flips);
        }

        let cardfooter = document.createElement('div');
        cardfooter.className = "card-footer text-muted";
        cardfooter.innerHTML = vuSince(flips[0].flip_updatedAt);
        card.appendChild(cardfooter);
    });
}

function flipLiElement(flip) {
    let node = document.createElement("li");
    node.className = "list-group-item py-1 px-2 d-flex justify-content-between align-items-center"
    node.id = flip.flip_objectId

    //node.innerText = flip.modele_nom
    node.setAttribute("data-flip-object-id", flip.flip_objectId)
    node.setAttribute("data-flip-id", flip.flip_id)
    node.setAttribute("data-flip-modele-nom", flip.modele_nom)

    let modelSpan = document.createElement('span')
    modelSpan.className = "model-span"
    modelSpan.innerText = flip.modele_nom

    let btn_group = document.createElement("div");
    btn_group.className = "btn-group ms-3";
    btn_group.setAttribute("role", "group");

    let btn_confirm = document.createElement("button");
    btn_confirm.type = "button";
    btn_confirm.className = "btn btn-primary btn-circle";
    btn_confirm.title = "Confirmer";
    btn_confirm.addEventListener('click', confirmerClicked);
    btn_confirm.innerHTML = '<i class="fas fa-clipboard-check"></i>';

    let btn_change = document.createElement("button");
    btn_change.type = "button";
    btn_change.className = "btn btn-secondary btn-circle";
    btn_change.title = "Changement";
    btn_change.setAttribute("data-bs-toggle", "modal")
    btn_change.setAttribute("data-bs-target", "#changeModal")
    btn_change.addEventListener('click', changementClicked)
    btn_change.innerHTML = '<i class="fa fa-refresh"></i>';

    let btn_delete = document.createElement("button");
    btn_delete.type = "button";
    btn_delete.className = "btn btn-danger btn-circle";
    btn_delete.title = "Supprimer";
    btn_delete.addEventListener('click', supprimeClicked)

    btn_delete.innerHTML = '<i class="fa fa-trash"></i>';

    node.appendChild(modelSpan)
    btn_group.appendChild(btn_confirm);
    btn_group.appendChild(btn_change);
    btn_group.appendChild(btn_delete);
    //TODO include buttons in list tab
    node.appendChild(btn_group);

    return node;
}


//-- -- -- -- -- - Helper functions-- -- -- -- -- -

function flipFactory(flipParseObject) {
    var enseigneParseObject = flipParseObject.get(flipperFields.enseigne_p)
    var modeleParseObject = flipParseObject.get(flipperFields.modele_p)

    var flip = {
        modele_nom: modeleParseObject.get(modeleFields.name),
        modele_annee: modeleParseObject.get(modeleFields.year),
        modele_marque: modeleParseObject.get(modeleFields.brand),
        ens_objectId: enseigneParseObject.id,
        ens_nom: enseigneParseObject.get(enseigneFields.name),
        ens_adresse: enseigneParseObject.get(enseigneFields.address),
        ens_cp: enseigneParseObject.get(enseigneFields.postcode),
        ens_ville: enseigneParseObject.get(enseigneFields.city),
        lat: enseigneParseObject.get(enseigneFields.lat),
        lng: enseigneParseObject.get(enseigneFields.lng),
        flip_datemaj: flipParseObject.get(flipperFields.datemaj),
        flip_id: flipParseObject.get(flipperFields.id),
        flip_objectId: flipParseObject.id,
        flip_distance: 0,
        flip_updatedAt: flipParseObject.updatedAt
    }

    return flip
}

//function to make Date object from FLIP_DATMAJ. 
function makeDate(dateString) {
    var parts = dateString.split("/");
    var date = new Date(parts[0], parts[1] - 1, parts[2]);
    return date;
};

//Distance between 2 points, rounded to 1 digit
function distanceFromInKm(lat1, lng1, lat2, lng2) {
    var dist = Math.round(10 * distance(lat1, lng1, lat2, lng2)) / 10;
    return dist
}

function getMODELID(objectid) {
    return modelsMap.get(objectid)[0];
}

function getMODELNAME(objectid) {
    return modelsMap.get(objectid)[1];
}


function saveParseObject(parseObject) {
    parseObject.save()
        .then((parseObject) => {
            // Execute any logic that should take place after the object is saved.
            alert('New object created with objectId: ' + parseObject.id);
        }, (error) => {
            // Execute any logic that should take place if the save fails.
            // error is a Parse.Error with an error code and message.
            alert('Failed to create new object, with error code: ' + error.message);
        });
}

function saveAllParseObjects(listParseObjects) {
    Parse.Object.saveAll(listParseObjects, {
        success: function (listParseObjects) {
            console.log('All objects were saved');
        },
        error: function (error) {
            // An error occurred while saving one of the objects.
            alert('Failed to create new object, with error code: ' + error.message);
        },
    });
};




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

function diffDateDays(date) {
    var today = new Date();
    var Difference_In_Time = today.getTime() - date.getTime();
    var Difference_In_Days = Math.floor(Difference_In_Time / (1000 * 3600 * 24));
    return Difference_In_Days;
}

function vuSince(date) {
    var days = diffDateDays(date);
    if (days > 360) {
        return '<i class="fas fa-exclamation-triangle"></i> Vu il y a ' + days + ' jours';
    }
    switch (days) {
        case 0: return "Vu(s) aujourdhui";
            break;
        case 1: return "Vu(s) hier";
            break;
        default: return "Vu(s) il y a " + days + " jours";
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

//-----SNACKBAR-----------------------
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


//========ACTIONS IN CONTEXT MENU======================================

//Update flip datemaj
function confirmerClicked(event) {
    const button = event.target.closest(".btn")
    const listGroupItem = event.target.closest(".list-group-item")
    const objectId = listGroupItem.dataset.flipObjectId
    const card = event.target.closest(".card")
    const cardFooter = card.querySelector(".card-footer")

    console.log(`Updating flip ${objectId}`)

    updateParseObject(Flipper, objectId, flipperFields.datemaj, formatDate(new Date()))
    button.setAttribute('disabled', true)
    cardFooter.innerHTML = vuSince(new Date())
    /*
    const Flipper = Parse.Object.extend("FLIPPER");
    const query = new Parse.Query(Flipper);
    query.get(objectId)
        .then((flipper) => {
            // The object was retrieved successfully.
            console.log("Object " + objectId + " was retrieved successfully");
            flipper.set("FLIP_DATMAJ", formatDate(new Date()));
            var snackText = "Flipper confirmé. Merci ";
            snack(snackText);
            return flipper.save();

        }, (error) => {
            // The object was not retrieved successfully.
            // error is a Parse.Error with an error code and message.
            console.log("Error retrieving object " + objectId + " : " + error.code + " : " + error.message);
        });
    */
}

function updateParseObject(parseObject, objectId, paramName, newValue) {
    console.log("running updateParseObject from list.js")

    const query = new Parse.Query(parseObject);
    query.get(objectId)
        .then((resultObject) => {
            // The object was retrieved successfully.
            console.log(parseObject.className + " object " + objectId + " was retrieved successfully")
            resultObject.set(paramName, newValue)
            snack(`${Flipper.className} confirmé. Merci `)
            return resultObject.save()
        }, (error) => {
            // The object was not retrieved successfully.
            // error is a Parse.Error with an error code and message.
            console.log("Error retrieving " + parseObject.className + " object " + objectId + " : " + error.code + " : " + error.message)
        });
}

//Puts the flip in the TrashList
function supprimeClicked(event) {
    const button = event.target.closest(".btn")
    const listGroupItem = event.target.closest(".list-group-item")
    const objectId = listGroupItem.dataset.flipObjectId
    const flipId = parseInt(listGroupItem.dataset.flipId)
    const card = event.target.closest(".card")

    console.log(`Notifying disparition of flip ${objectId}`)

    const query = new Parse.Query(FlipTrash);
    query.equalTo(trashListFields.processed, false);
    query.equalTo(trashListFields.id, flipId);
    //query.limit(1);
    query.find().then(function (results) {
        if (results.length > 0) {
            console.log("Flip deja present dans la trash list : no need to add ")
        }
        else {
            var newFlipTrash = new Parse.Object("FLIPTRASH");
            newFlipTrash.set(trashListFields.id, flipId);
            newFlipTrash.set(trashListFields.processed, false);
            newFlipTrash.set(trashListFields.pseudo, "WEB");
            return newFlipTrash.save();
        }
    }).then( fliptrash => {
        console.log('New object created with objectId: ' + fliptrash.id);
        snack("Notification de disparition envoyée. Merci");
        button.setAttribute('disabled', true)

    }, error => {
        console.log("Error: " + error.code + " " + error.message);
    }
    );
}

//Button clicked and open modal
function changementClicked(event) {
    var listItem = event.target.closest(".list-group-item")

    const flip = {
        "flip_objectId": listItem.dataset.flipObjectId,
        "modele_nom": listItem.dataset.flipModeleNom,
        "flip_id": listItem.dataset.flipId
    }

    var modal = document.querySelector(".modal-header")
    modal.setAttribute("data-flip-object-id", flip.flip_objectId)
    modal.setAttribute("data-flip-modele-nom", flip.modele_nom)
    modal.setAttribute("data-flip-id", flip.flip_id)
    console.log(modal.dataset)


    var btn_confirm = document.getElementById("confirm-change-btn")
    btn_confirm.addEventListener("click", changementModeleDirty)

}

function changementModeleDirty(event) {
    console.log("Confirm button : clicked")
    const button = event.target
    //const currentFlipObjectId = document.getElementsByClassName("modal-header")[0].getAttribute("data-flipObjectId")

    const modalHeader = document.querySelector(".modal-header")
    console.log(modalHeader)
    console.log(modalHeader.dataset)
    const currentFlipObjectId = modalHeader.dataset.flipObjectId
    console.log(`currentFlipObjectId : ${currentFlipObjectId}`)
    const currentFlipModeleNom = modalHeader.dataset.flipModeleNom
    console.log(`currentFlipModeleNom : ${currentFlipModeleNom}`)
    const currentFlipId = modalHeader.dataset.flipId
    console.log(`currentFlipId : ${currentFlipId}`)




    var select = document.querySelector("#modeleflipper")
    var selectedModele = select.value
    var selectedModeleNom = select.options[select.selectedIndex].text
    console.log("Nouveau modèle : " + selectedModeleNom)

    const regex = new RegExp('^[^(]*')
    const found = selectedModeleNom.match(regex);
    var shortSelectedModelNom = found == null ? "refresh page" : found[0]
    console.log(shortSelectedModelNom);


    if (selectedModele == "Choisir Modèle") {
        alert("Selectionner un modèle")
        return
    }
    const newFlipModelObjectId = selectedModele

    console.log("flipObjectId/ID: " + currentFlipObjectId + "(" + currentFlipId + "). Current Modele : " + currentFlipModeleNom + " modeleID selected  " + newFlipModelObjectId)

    //besoin de flip objectid, de flip id, de flip modele id

    console.log(`Changing modele of flip ${currentFlipObjectId}: ${currentFlipModeleNom} > ${newFlipModelObjectId} `)
    //RECUPERER LE FLIP PARSEOBJECT

    const query = new Parse.Query(Flipper);
    query.get(currentFlipObjectId)
        .then((flipper) => {
            // The object was retrieved successfully.
            console.log("Object " + flipper.id + " was retrieved successfully");
            flipper.set(flipperFields.datemaj, formatDate(new Date()))
            flipper.set(flipperFields.modele, getMODELID(newFlipModelObjectId))
            flipper.set(flipperFields.modele_p, Modele.createWithoutData(newFlipModelObjectId))

            var snackText = "Modèle modifié. Merci ";
            snack(snackText);
            return flipper.save();

        }, (error) => {
            // The object was not retrieved successfully.
            // error is a Parse.Error with an error code and message.
            console.log("Error retrieving object " + currentFlipObjectId + " : " + error.code + " : " + error.message);
        });

    //TODO fermer le modal et modifier le modele dans l'info window ()




    //CREER COMMENTAIRE REMPLACEMENT
    listToSave = []
    var Commentaire = Parse.Object.extend("COMMENTAIRE");
    var newCommentaire = new Commentaire();

    newCommentaire.set(commentFields.id, new Date().getTime());
    newCommentaire.set(commentFields.text, `Le ${currentFlipModeleNom} a été remplacé`);
    newCommentaire.set(commentFields.actif, true);
    newCommentaire.set(commentFields.date, formatDate(new Date()));
    newCommentaire.set(commentFields.flipperid, parseInt(currentFlipId));
    newCommentaire.set(commentFields.flipperid_p, Flipper.createWithoutData(currentFlipObjectId));
    newCommentaire.set(commentFields.type, "REPLACED");
    newCommentaire.set(commentFields.pseudo, "WEB");
    listToSave.push(newCommentaire);

    saveAllParseObjects(listToSave);

    //Ferme le Modal
    $('#changeModal').modal('hide')

    //update listitem
    var listitem = document.querySelector(`#${currentFlipObjectId}`)
    var texteModel = listitem.querySelector(".model-span")
    console.log(texteModel)
    texteModel.innerText = shortSelectedModelNom
    listitem.setAttribute("data-flip-modele-nom", shortSelectedModelNom)



}


var modelsMap = new Map();

init_modelsMap();

function init_modelsMap() {
    var query = new Parse.Query(Parse.Object.extend("MODELE_FLIPPER"));
    query.limit(350);
    query.ascending("MOFL_NOM");
    query.find({
        success: function (results) {
            console.log("Successfully retrieved " + results.length + " models.");
            if (results.length > 0) {
                for (let i = 0; i < results.length; i++) {
                    modelsMap.set(results[i].id,
                        [results[i].attributes.MOFL_ID,
                        results[i].attributes.MOFL_NOM,
                        results[i].attributes.MOFL_ANNEE_LANCEMENT,
                        results[i].attributes.MOFL_MARQUE
                        ]);
                }
                //once modelsMap is initialized, fill first model dropdown
                populate("modeleflipper");
            };
        },
        error: function (error) {
            console.log("Error: " + error.code + " " + error.message);
        }
    });
}

//-- -- -- -- -- - POPULATE DROPDOWN-- -- -- -- -- -- -
function populate(selectElement) {
    let dropdown = document.getElementById(selectElement);
    //dropdown.length = 0;
    let defaultOption = document.createElement('option');
    defaultOption.text = 'Choisir Modèle';
    dropdown.add(defaultOption);
    dropdown.selectedIndex = 0;

    var modelsMapAsc = new Map([...modelsMap.entries()].sort());

    for (let model of modelsMap.entries()) {

        option = document.createElement('option');
        var longtext = model[1][1] + " (" +
            model[1][3] + ", " + model[1][2] + ")";
        var objectId = model[0];
        //console.log(longtext);
        option.text = longtext;
        option.value = objectId;
        dropdown.add(option);
    };
}









/*
function sendMailDelete(flip_id) {
    var link = "mailto:flipper.finder2@gmail.com" +
        "?subject=" + escape("Retrait du flipper no " + flip_id + "(Web)") +
        "&body=" + escape("ID : " + flip_id + "\nCe flipper n'existe plus");
    window.location.href = link;
}
*/



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


