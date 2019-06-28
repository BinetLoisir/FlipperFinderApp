//Intialize PinMyBalls Back4App.
Parse.initialize("3uaqg1hxB7a5a2aN3wA3lpOmuWHfSObgZLKYTojH", "LBM1PD8VKAsGedV2BKnDPTVChalW0EzBbp8lKX9p");
Parse.serverURL = 'https://parseapi.back4app.com';
// Simple syntax to create a new subclass of Parse.Object.
var Flipper = Parse.Object.extend("FLIPPER");
var Enseigne = Parse.Object.extend("ENSEIGNE");
var Modele = Parse.Object.extend("MODELE_FLIPPER");

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
    datemaj: "ENS_DATEMAJ",
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


function saveFlipperEnseigne(name, address, postcode, city, country, datemaj, lat, lng, modeleId, modelObjectId, actif) {
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

    var Flipper = Parse.Object.extend("FLIPPER");
    var Modele = Parse.Object.extend("MODELE_FLIPPER");
    var newFlipper = new Flipper();

    newFlipper.set(flipperFields.id, new Date().getTime());
    newFlipper.set(flipperFields.modele, modeleId);
    newFlipper.set(flipperFields.modele_p, Modele.createWithoutData(modelObjectId));
    newFlipper.set(flipperFields.enseigne, newEnseigne.get(enseigneFields.id));
    newFlipper.set(flipperFields.enseigne_p, newEnseigne);
    newFlipper.set(flipperFields.datemaj, datemaj);
    newFlipper.set(flipperFields.actif, actif);

    var Commentaire = Parse.Object.extend("COMMENTAIRE");
    var newCommentaire = new Commentaire();

    newCommentaire.set(commentFields.id, new Date().getTime());
    newCommentaire.set(commentFields.text, "Nouveau");
    newCommentaire.set(commentFields.actif, true);
    newCommentaire.set(commentFields.datemaj, datemaj);
    newCommentaire.set(commentFields.flipperid, newFlipper.get(flipperFields.id));
    newCommentaire.set(commentFields.flipperid_p, newFlipper);
    
    saveAllParseObjects([newCommentaire,newEnseigne,newFlipper]);

};


function saveModel(name, year, brand) {
    var Modele = Parse.Object.extend("MODELE_FLIPPER");
    var newModele = new Modele();
    var numberofmodels = 998;

    newModele.set(modeleFields.name, name);
    newModele.set(modeleFields.year, year);
    newModele.set(modeleFields.id, numberofmodels + 1);
    newModele.set(modeleFields.brand, brand);

    saveParseObject(newModele);
};

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
            alert('All objects were saved');
        },
        error: function (error) {
            // An error occurred while saving one of the objects.
            alert('Failed to create new object, with error code: ' + error.message);
        },
    });
};


function launch() {

    var ens = {
        name: "Les Mouettes",
        datemaj: "26/11/2018",
        address: "2, rue du Port",
        postcode: "29200",
        city: "Brest",
        country: "France",
        lat: 2.3,
        lng: 43.4
    };
    
    var modelid = 1;
    var modelObjectID = "zKmSki1NZe";
    
    //saveFlipperEnseigne(ens.name,ens.address, ens.postcode,ens.city,ens.country,ens.datemaj,ens.lat,ens.lng, modelid, modelObjectID, true);

    //saveModel("Stiff",1984,"Bally");

}



//--------------CLASSES DEFINITIONS-------


