//Intialize PinMyBalls Back4App.
Parse.initialize("wx8ZJI9628FDGq39REy6rMlZjKdP5ERUMXjZpqjE", "HVHpDx5BgQG54UDLdZhLv7cFoontQUA8eIE8YC2D");
Parse.serverURL = 'https://parseapi.back4app.com';

let liste = document.getElementById("myList");

//-- -- -- -- -- - Const-- -- -- -- -- -

var Flipper = Parse.Object.extend("FLIPPER");
var Enseigne = Parse.Object.extend("ENSEIGNE");
var Modele = Parse.Object.extend("MODELE_FLIPPER");
var Commentaire = Parse.Object.extend("COMMENTAIRE");

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

var TYPE_POST = "POST";
var TYPE_NEW = "NEW";
var TYPE_DELETE = "DELETED";
var TYPE_REINSTALL = "REINSTALLED";
var TYPE_REFRESH = "REFRESHED";
var TYPE_REPLACE = "REPLACED";
var TYPE_ADDITIONAL = "ADDITIONAL";

var MAX_COMMENTS = 50

loadComments(MAX_COMMENTS,TYPE_POST)

var list = [];

function clearComments() {
    while (liste.firstChild) {
        liste.removeChild(liste.lastChild);
    }
}

function loadComments(max, type) {
    clearComments()
    return list = getCommentaires(max, type);
}

async function getCommentaires(max, commentType) {

    var query = new Parse.Query(Commentaire)
    query.limit(max)
    query.equalTo(commentFields.actif, true)
    query.equalTo(commentFields.type, commentType)
    query.descending("createdAt");

    query.include(commentFields.flipperid_p)
    query.include(commentFields.flipperid_p + "." + flipperFields.modele_p)
    query.include(commentFields.flipperid_p + "." + flipperFields.enseigne_p)

    const comments = await query.find();

    var posts = []

    for (let i = 0; i < comments.length; i++) {
        var post = {
            comment: "",
            modele: "",
            enseigne: "",
            ville: ""
        }
        //Commentaire
        var comment = commentFactory(comments[i])
        post.comment = comment
        //Modele
        var modelePO = comments[i].get(commentFields.flipperid_p).get(flipperFields.modele_p)
        post.modele = modeleFactory(modelePO);
        //Enseigne
        var enseignePO = comments[i].get(commentFields.flipperid_p).get(flipperFields.enseigne_p)
        post.enseigne = enseigneFactory(enseignePO);
        post.ville = villeFactory(enseignePO);

        //console.log(post)
        posts.push(post)
    }

    populateList(posts);
    return posts;
}

//-- -- -- -- -- - UI -- -- -- -- -- --
//populate from list of flips

function populateList(posts) {
    document.querySelector('#top-banner').style.display = "none"

    posts.forEach(post => {
        let li = document.createElement("li");
        li.className = "list-group-item";
        liste.appendChild(li);

        let card = document.createElement("div");
        card.className = "card";
        li.appendChild(card);

        let cardheader = document.createElement('div');
        cardheader.className = "card-header d-flex justify-content-between align-items-center bg-primary text-white";
        cardheader.innerText = post.enseigne + " (" + post.ville + ")";
        card.appendChild(cardheader);

        let cardheaderright = document.createElement('span');
        cardheaderright.className = "badge bg-dark";
        cardheaderright.innerText = post.comment.com_pseudo;
        cardheader.appendChild(cardheaderright);


        let cardbody = document.createElement('div');
        cardbody.className = "card-body";
        card.appendChild(cardbody);

        let title = document.createElement('h6');
        title.className = "card-title";
        title.innerText = post.modele;
        cardbody.appendChild(title);

        let subtitle = document.createElement('h7');
        subtitle.className = "card-subtitle mb-2 text-muted";
        subtitle.innerHTML = post.comment.com_texte;
        cardbody.appendChild(subtitle);

        let cardfooter = document.createElement('h9');
        cardfooter.className = "card-footer text-muted";
        cardfooter.innerHTML = postedSince(post.comment.com_createdAt);
        card.appendChild(cardfooter);
    });
}

//-- -- -- -- -- - Helper functions-- -- -- -- -- -

function commentFactory(result) {

    if (result != null) {
        var comment = {
            com_ObjId: result.id,
            com_id: result.get(commentFields.id),
            com_date: result.get(commentFields.date),
            com_actif: result.get(commentFields.actif),
            com_pseudo: result.get(commentFields.pseudo),
            com_texte: result.get(commentFields.text),
            com_type: result.get(commentFields.type),
            com_flipid: result.get(commentFields.flipperid),
            com_flipid_p: result.get(commentFields.flipperid_p),
            com_createdAt: result.createdAt
        }
        return comment;
    }
    return null;
}

function modeleFactory(modeleParseObject) {
    var modele_name = modeleParseObject.get(modeleFields.name);
    var modele_brand = modeleParseObject.get(modeleFields.brand);
    var modele_year = modeleParseObject.get(modeleFields.year);
    var modele_string_long = `${modele_name}, ${modele_brand} (${modele_year})`
    return modele_string_long
}

function enseigneFactory(enseigneParseObject) {
    return enseigne_nom = enseigneParseObject.get(enseigneFields.name);
}

function villeFactory(enseigneParseObject) {
    return enseigne_ville = enseigneParseObject.get(enseigneFields.city);
}


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

function postedSince(date) {
    var days = diffDateDays(date);
    if (days > 360) {
        return '<i class="fas fa-exclamation-triangle"></i> Vu il y a ' + days + ' jours';
    }
    switch (days) {
        case 0: return "Posté aujourdhui";
            break;
        case 1: return "Posté hier";
            break;
        default: return "Posté il y a " + days + " jours";
    }
}