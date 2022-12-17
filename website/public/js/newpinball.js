//Intialize PinMyBalls Back4App.
Parse.initialize("wx8ZJI9628FDGq39REy6rMlZjKdP5ERUMXjZpqjE", "HVHpDx5BgQG54UDLdZhLv7cFoontQUA8eIE8YC2D");
Parse.serverURL = 'https://parseapi.back4app.com';


var placeSearch, autocomplete;
var latlng;
var componentForm = {
    street_number: 'short_name',
    route: 'long_name',
    locality: 'long_name',
    country: 'long_name',
    postal_code: 'short_name'
};

var nbmodel = 1;
var nbmodelmax = 5;
var firstmodel = "modeleflipper1";
var defaultOption = 'Choisir Modèle';
var modelsMap = new Map();
var minimap

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
                populate(firstmodel);
            };
        },
        error: function (error) {
            console.log("Error: " + error.code + " " + error.message);
        }
    });
}

function initMap() {

    // The location of Paris
    const paris = { lat: 46.36, lng: 1.52 };

    // The map, centered at Paris
    minimap = new google.maps.Map(document.getElementById("minimap"), {
        zoom: 4,
        center: paris,
        mapTypeControl : false,
        streetViewControl: false
    });

}

window.initMap = initMap;


function initAutocomplete() {
    initMap()
    // Create the autocomplete object, restricting the search to geographical
    // location types.
    autocomplete = new google.maps.places.Autocomplete(
        /** @type {!HTMLInputElement} */
        (document.getElementById('autocomplete')));
    // When the user selects an address from the dropdown, populate the address
    // fields in the form.
    autocomplete.addListener('place_changed', fillInAddress);
}

function fillInAddress() {
    // Get the place details from the autocomplete object.
    var place = autocomplete.getPlace();

    document.getElementById("place_name").value = place.name;


    for (var component in componentForm) {
        document.getElementById(component).value = '';
        document.getElementById(component).disabled = false;
    }

    // Get each component of the address from the place details
    // and fill the corresponding field on the form.
    for (var i = 0; i < place.address_components.length; i++) {
        var addressType = place.address_components[i].types[0];
        if (componentForm[addressType]) {
            var val = place.address_components[i][componentForm[addressType]];
            document.getElementById(addressType).value = val;
        }
    }
    latlng = place.geometry.location;

    //Center Map
    minimap.setCenter(place.geometry.location)
    minimap.setZoom(17)


}

// Bias the autocomplete object to the user's geographical location,
// as supplied by the browser's 'navigator.geolocation' object.
function geolocate() {
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(function (position) {
            var geolocation = {
                lat: position.coords.latitude,
                lng: position.coords.longitude
            };
            var circle = new google.maps.Circle({
                center: geolocation,
                radius: position.coords.accuracy
            });
            autocomplete.setBounds(circle.getBounds());
        });
    }
}

//-- -- -- -- -- - ADD MODEL -- -- -- -- -- -- -
function addModel() {
    if (nbmodel == nbmodelmax) {
        alert("Nombre max de modeles atteint")
    } else {
        nbmodel++;
        var nextmodel = document.getElementById('modele' + nbmodel);
        populate("modeleflipper" + nbmodel);
        nextmodel.style.display = 'block';

    }
}

//-- -- -- -- -- - REMOVE MODEL -- -- -- -- -- -- -
function removeModel() {
    if (nbmodel == 1) {
        alert("Choisir au moins un modéle")
    } else {
        var nextmodel = document.getElementById('modele' + nbmodel);
        nextmodel.style.display = 'none';
        nbmodel--;
    }
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

//-- -- -- -- -- - Parse interactions -- -- -- -- -- -

var Flipper = Parse.Object.extend("FLIPPER");
var Enseigne = Parse.Object.extend("ENSEIGNE");
var Modele = Parse.Object.extend("MODELE_FLIPPER");

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

function saveEnseigne(name, address, postcode, city, country, datemaj, lat, lng) {
    var Enseigne = Parse.Object.extend("ENSEIGNE");
    var newEnseigne = new Enseigne();

    newEnseigne.set(enseigneFields.name, name);
    newEnseigne.set(enseigneFields.address, address);
    newEnseigne.set(enseigneFields.postcode, postcode);
    newEnseigne.set(enseigneFields.city, city);
    newEnseigne.set(enseigneFields.country, country);
    newEnseigne.set(enseigneFields.datemaj, datemaj);
    newEnseigne.set(enseigneFields.latlng, [lat, lng]);
    newEnseigne.set(enseigneFields.lat, lat);
    newEnseigne.set(enseigneFields.lng, lng);
    newEnseigne.set(enseigneFields.id, new Date().getTime());

    saveParseObject(newEnseigne);
};

function saveFlipper(modeleId, enseigneId, datemaj, actif) {
    var Flipper = Parse.Object.extend("FLIPPER");
    var newFlipper = new Flipper();

    newFlipper.set(flipperFields.modele, modeleId);
    newFlipper.set(flipperFields.enseigne, enseigneId);
    newFlipper.set(flipperFields.datemaj, datemaj);
    newFlipper.set(flipperFields.actif, actif);
    var id = new Date().getTime;
    newFlipper.set(flipperFields.id, new Date().getTime());

    saveParseObject(newFlipper);
};

function saveModel(name, year, brand) {
    var Modele = Parse.Object.extend("MODELE_FLIPPER");
    var newModele = new Modele();

    newModele.set(modeleFields.name, name);
    newModele.set(modeleFields.year, year);
    //TODO Set id to be MAX_ID +1
    newModele.set(modeleFields.brand, brand);

    saveParseObject(newModele);
};

function saveFlipperEnseigne(name, address, postcode, city, country, datemaj, lat, lng, modelObjectIdArray) {
    var listToSave = [];
    var Enseigne = Parse.Object.extend("ENSEIGNE");
    var newEnseigne = new Enseigne();
    newEnseigne.set(enseigneFields.name, name);
    newEnseigne.set(enseigneFields.address, address);
    newEnseigne.set(enseigneFields.postcode, postcode);
    newEnseigne.set(enseigneFields.city, city);
    newEnseigne.set(enseigneFields.country, country);
    newEnseigne.set(enseigneFields.datemaj, datemaj);
    newEnseigne.set(enseigneFields.latlng, new Parse.GeoPoint(lat, lng));
    newEnseigne.set(enseigneFields.lat, lat);
    newEnseigne.set(enseigneFields.lng, lng);
    newEnseigne.set(enseigneFields.id, new Date().getTime());
    listToSave.push(newEnseigne);

    for (const modelObjectId of modelObjectIdArray) {
        var i = 0;
        var Flipper = Parse.Object.extend("FLIPPER");
        var Modele = Parse.Object.extend("MODELE_FLIPPER");
        var newFlipper = new Flipper();

        newFlipper.set(flipperFields.id, new Date().getTime() + i);
        newFlipper.set(flipperFields.modele, getMODELID(modelObjectId));
        newFlipper.set(flipperFields.modele_p, Modele.createWithoutData(modelObjectId));
        newFlipper.set(flipperFields.enseigne, newEnseigne.get(enseigneFields.id));
        newFlipper.set(flipperFields.enseigne_p, newEnseigne);
        newFlipper.set(flipperFields.datemaj, datemaj);
        newFlipper.set(flipperFields.actif, true);
        listToSave.push(newFlipper);

        var Commentaire = Parse.Object.extend("COMMENTAIRE");
        var newCommentaire = new Commentaire();

        newCommentaire.set(commentFields.id, new Date().getTime() + i);
        newCommentaire.set(commentFields.text, "Nouveau (web)");
        newCommentaire.set(commentFields.actif, true);
        newCommentaire.set(commentFields.date, datemaj);
        newCommentaire.set(commentFields.flipperid, newFlipper.get(flipperFields.id));
        newCommentaire.set(commentFields.flipperid_p, newFlipper);
        newCommentaire.set(commentFields.type, "NEW");
        newCommentaire.set(commentFields.pseudo, "WEB");
        newCommentaire.set(commentFields.flipperid_p, newFlipper);
        listToSave.push(newCommentaire);
        i++;
    }

    saveAllParseObjects(listToSave);

};

//-- -- -- -- -- - Helper functions-- -- -- -- -- -

function formatDate(date) {
    var d = new Date(date),
        month = '' + (d.getMonth() + 1),
        day = '' + d.getDate(),
        year = d.getFullYear();

    if (month.length < 2) month = '0' + month;
    if (day.length < 2) day = '0' + day;

    return [year, month, day].join('/');
}

function getMODELID(objectid) {
    return modelsMap.get(objectid)[0];
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
            console.log('Failed to create new object, with error code: ' + error.message);
        },
    });
};


//-- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -

//-- -- -- -- -- - Submit new pinball-- -- -- -- -- -- -
function sendbutton() {
    var place_name = document.getElementById("place_name").value;
    var street_number = document.getElementById("street_number").value;
    var route = document.getElementById("route").value;
    var postal_code = document.getElementById("postal_code").value;
    var locality = document.getElementById("locality").value;
    var country = document.getElementById("country").value;

    var modele1_e = document.getElementById("modeleflipper1");
    var modele2_e = document.getElementById("modeleflipper2");
    var modele3_e = document.getElementById("modeleflipper3");
    var modele4_e = document.getElementById("modeleflipper4");
    var modele5_e = document.getElementById("modeleflipper5");

    var modelArray = [];
    var modelOIDArray = [];

    if (modele1_e.value.length > 0) {
        var modele1_value = modele1_e.options[modele1_e.selectedIndex].value;
        modelOIDArray.push(modele1_value)
        var modele1_text = modele1_e.options[modele1_e.selectedIndex].text;
        modelArray.push(modele1_text);
    }
    if (modele2_e.value.length > 0) {
        var modele2_value = modele2_e.options[modele2_e.selectedIndex].value;
        modelOIDArray.push(modele2_value)
        var modele2_text = modele2_e.options[modele2_e.selectedIndex].text;
        modelArray.push(modele2_text);
    }
    if (modele3_e.value.length > 0) {
        var modele3_value = modele3_e.options[modele3_e.selectedIndex].value;
        modelOIDArray.push(modele3_value)
        var modele3_text = modele3_e.options[modele3_e.selectedIndex].text;
        modelArray.push(modele3_text);
    }
    if (modele4_e.value.length > 0) {
        var modele4_value = modele4_e.options[modele4_e.selectedIndex].value;
        modelOIDArray.push(modele4_value)
        var modele4_text = modele4_e.options[modele4_e.selectedIndex].text;
        modelArray.push(modele4_text);
    }
    if (modele5_e.value.length > 0) {
        var modele5_value = modele5_e.options[modele5_e.selectedIndex].value;
        modelOIDArray.push(modele5_value)
        var modele5_text = modele5_e.options[modele5_e.selectedIndex].text;
        modelArray.push(modele5_text);
    }

    var modelString = "";
    for (var i = 0; i < modelArray.length; i++) {
        modelString = modelString.concat(modelArray[i] + "\n");
    }

    /*
    //Send by Mail----------------------------- OLD
    var email = "flipper.finder2@gmail.com";
    var subject = "Nouveau flipper : " + locality;
    var enseigne_complete_addresse = place_name + "\n" + street_number + " " + route + "\n" + postal_code + " " + locality + "\n" + country;
    var body = modelString + enseigne_complete_addresse;
    //alert (body);

    alert(modele1_text + ", " + modele1_value + " @ " + latlng.lat);
    var link = "mailto:" + email + "?subject=" + escape(subject) + "&body=" + escape(body);
    window.location.href = link;
    //----------------------------------------- 
    */

    var txt, r;
    r = confirm("Do you want to add" + "\n" +
        modelString + "\n" +
        place_name + "\n" +
        street_number + " " + route + "\n" +
        postal_code + " " + locality + "\n" +
        country + "\n"
    );
    if (r == true) {
        saveFlipperEnseigne(
            place_name,
            street_number + " " + route,
            postal_code,
            locality,
            country,
            formatDate(new Date()),
            latlng.lat(),
            latlng.lng(),
            modelOIDArray);
    } else {
        //Do nothing
    }
}

function runit() {
    var modele1_e = document.getElementById("modeleflipper1");
    var modele2_e = document.getElementById("modeleflipper2");
    console.log(modele1_e.value);
    console.log(modele2_e.value.length);

}
