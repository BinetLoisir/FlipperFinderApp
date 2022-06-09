//Callback to programatically adds JS script for Google maps Once DOMContentLoaded. Will fire initmap(). Downloaded from web.
document.addEventListener('DOMContentLoaded', function () {
    if (document.querySelectorAll('#map').length > 0) {
        if (document.querySelector('html').lang)
            lang = document.querySelector('html').lang
        else
            lang = 'en'

        var js_file = document.createElement('script')
        js_file.type = 'text/javascript'
        js_file.src = 'https://maps.googleapis.com/maps/api/js?callback=initMap&key=AIzaSyBWzLO7XJTK0qp3hWkX599YdiUWGc_yFYc&libraries=places&language=' + lang
        document.getElementsByTagName('head')[0].appendChild(js_file)
    }
})

var map, bounds, ne, sw
var markers = []
var myLocation
//const defaultLocation = new google.maps.LatLng(48.883461, 2.340561)

const QUERYLIMIT = 50


//TODO HIDE ID and KEY for PARSE
Parse.initialize("wx8ZJI9628FDGq39REy6rMlZjKdP5ERUMXjZpqjE", "HVHpDx5BgQG54UDLdZhLv7cFoontQUA8eIE8YC2D")
Parse.serverURL = 'https://parseapi.back4app.com'
// Simple syntax to create a new subclass of Parse.Object.
var Flipper = Parse.Object.extend("FLIPPER")
var Enseigne = Parse.Object.extend("ENSEIGNE")
var FlipTrash = Parse.Object.extend("FLIPTRASH")
var Commentaire = Parse.Object.extend("COMMENTAIRE")
var Modele = Parse.Object.extend("MODELE_FLIPPER")

const modeleFields = {
    id: "MOFL_ID",
    name: "MOFL_NOM",
    year: "MOFL_ANNEE_LANCEMENT",
    brand: "MOFL_MARQUE"
}

const flipperFields = {
    id: "FLIP_ID",
    modele: "FLIP_MODELE",
    modele_p: "FLIP_MODELE_P",
    enseigne: "FLIP_ENSEIGNE",
    enseigne_p: "FLIP_ENSEIGNE_P",
    datemaj: "FLIP_DATMAJ",
    actif: "FLIP_ACTIF"
}

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
}

const commentFields = {
    id: "COMM_ID",
    date: "COMM_DATE",
    text: "COMM_TEXTE",
    type: "COMM_TYPE",
    pseudo: "COMM_PSEUDO",
    actif: "COMM_ACTIF",
    flipperid: "COMM_FLIPPER_ID",
    flipperid_p: "COMM_FLIPPER_ID_P"
}

const TrashListFields = {
    id: "FLIP_ID",
    processed: "PROCESSED",
    pseudo: "PSEUDO",
}

var flip_id_test = "1602265500436"
async function test() {
    var query = new Parse.Query(Commentaire)
    query.limit(10)
    query.equalTo(commentFields.flipperid, "1602265500436")
    query.equalTo(commentFields.actif, true)
    query.equalTo(commentFields.type, "POST")
    query.descending("createdAt")

    //console.log(query)

    const results = await query.find()
    console.log("Successfully retrieved " + results.length + " comments.")

    /*
    query.find({
        success: (results) => {
            console.log("Successfully retrieved " + results.length + " comments.")
        }
    })
    */
}
//test()

function initMap() {
    const defaultLocation = new google.maps.LatLng(48.883461, 2.340561)
    var location = defaultLocation
    var myMarker = new google.maps.Marker({
        map: map,
        animation: google.maps.Animation.DROP,
        position: location
    })

    //WORKS but comes after map is loaded
    //If location found, place myLocation marker and center map on this marker
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
            (position) => {
                //show the map
                document.querySelector('#map').style.visibility = 'visible'
                document.querySelector('#pac-input').style.visibility = 'visible'
                document.querySelector('#top-banner').style.display = "none"

                //update location, center map and show marker
                location = new google.maps.LatLng(position.coords.latitude, position.coords.longitude)
                myLocation = location
                map.setCenter(myLocation)
                var myMarkerGeo = new google.maps.Marker({
                    map: map,
                    animation: google.maps.Animation.DROP,
                    position: myLocation
                })
                //myMarker.setPosition(myLocation)
            },
            (err) => {
                console.warn(`ERROR(${err.code}): ${err.message}`)
                //show the map and hide the loading animation
                document.querySelector('#map').style.visibility = 'visible'
                document.querySelector('#pac-input').style.visibility = 'visible'
                document.querySelector('#top-banner').style.display = "none"

            },
            {
                enableHighAccuracy: false,
                timeout: 10000,
                maximumAge: 0
            }
        )

        // Map creation
        map = new google.maps.Map(document.getElementById('map'), {
            center: location,
            zoom: 13,
            disableDefaultUI: true,
            zoomControl: true,
            scaleControl: true,
        })

        //Adds the location button and search buttons on the map  
        addLocationButton(map, myMarker)
        addAutoCompleteSearchBox(map)

        //Load markers after map is loaded, and after map stops moving
        map.addListener('idle', function () {
            //console.log('Map : idle event fired')
            geoqueryandplotmarkers()
        })

    }

}

function geoqueryandplotmarkers() {
    //Definition of the innerquery
    var geoboxquery = new Parse.Query(Enseigne)
    geoboxquery.withinGeoBox(
        "ENS_GEO",
        new Parse.GeoPoint({
            latitude: map.getBounds().getSouthWest().lat(),
            longitude: map.getBounds().getSouthWest().lng()
        }),
        new Parse.GeoPoint({
            latitude: map.getBounds().getNorthEast().lat(),
            longitude: map.getBounds().getNorthEast().lng()
        })
    )

    //Definition of the FlipperQuery
    var query = new Parse.Query(Flipper)
    query.equalTo("FLIP_ACTIF", true)
    query.matchesQuery("FLIP_ENSEIGNE_P", geoboxquery)
    query.include("FLIP_ENSEIGNE_P")
    query.include("FLIP_MODELE_P")
    query.limit(QUERYLIMIT)
    query.find({
        success: function (results) {
            console.log("Successfully retrieved " + results.length + " flippers.")

            if (results.length > 0) {
                //var modele_nom, modele_annee, modele_marque, ens_nom, ens_adresse, ens_cp, ens_ville, lat, lng, flip_datemaj, flip_id, flip_objectId
                var flipperArray = []
                //enseigneMap is a Map<ensId, flip[]>
                let enseigneMap = new Map()
                for (var i = 0; i < results.length; i++) {
                    var flip = {
                        modele_nom: results[i].get("FLIP_MODELE_P").get("MOFL_NOM"),
                        modele_annee: results[i].get("FLIP_MODELE_P").get("MOFL_ANNEE_LANCEMENT"),
                        modele_marque: results[i].get("FLIP_MODELE_P").get("MOFL_MARQUE"),
                        ens_objectId: results[i].get("FLIP_ENSEIGNE_P").id,
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
                    flipperArray.push(flip)

                    var ensId = flip.ens_objectId
                    var tempFlipArray = [flip]

                    //if key is in map already
                    if (enseigneMap.get(ensId) != undefined) {
                        //add flip to the array
                        tempFlipArray = tempFlipArray.concat(enseigneMap.get(ensId))
                    }
                    //set or reset the key,value
                    enseigneMap.set(ensId, tempFlipArray)
                }

                //var snackText = results.length + " flippers trouvés."
                //snack(snackText)

                plotMarkers(enseigneMap)
            } else {
                snack("Pas de flippers aux environs")
            }
        },
        error: function (error) {
            console.log("Error: " + error.code + " " + error.message)
        }
    })
}

//Plot marker from the values of a Map
function plotMarkers(enseigneMap) {
    //bounds = new google.maps.LatLngBounds()
    //console.log("Plotting : " + m.length + " markers.")
    enseigneMap.forEach(function (value) {
        //value is an Array of Flippers of the same Enseigne
        var position = new google.maps.LatLng(value[0].lat, value[0].lng)
        var infowindow = new google.maps.InfoWindow()
        infowindow.setContent(infoWindow(value))

        var mapMarker = new google.maps.Marker({
            position: position,
            map: map,
            title: value[0].ens_nom,
            icon: iconSelect(makeDate(value[0].flip_datemaj))
        })

        markers.push(mapMarker)

        mapMarker.addListener('click', () => {
            infowindow.open({
                anchor: mapMarker,
                map,
                shouldFocus: false,
            }
            )
        })

        //bounds.extend(position)
    })

    //map.fitBounds(bounds)
}

//Create Info Window - August2021
function infoWindow(flipArray) {
    let node = document.createElement("div")
    node.id = "iw-container2"

    let iw_title_container = document.createElement("h6")
    let iw_title = document.createElement("span")
    iw_title.className = "badge bg-secondary"
    iw_title.innerText = flipArray[0].ens_nom
    iw_title_container.appendChild(iw_title)

    let iw_subtitle_container = document.createElement("h7")
    iw_subtitle_container.style.display = "none"
    let iw_subtitle = document.createElement("p")
    iw_subtitle.className = "text-muted ps-1 mb-1"
    iw_subtitle.innerText = flipArray[0].ens_adresse + ", " + flipArray[0].ens_ville
    iw_subtitle_container.appendChild(iw_subtitle)


    iw_title_container.onclick = () => { iw_subtitle_container.style.display = "block" }


    let iw_body = document.createElement("div")
    iw_body.className = "iw-content2"

    let ul_flips = document.createElement("ul")
    ul_flips.className = "list-group"
    for (const flip of flipArray) {
        let li_flip = flipHtml(flip)
        ul_flips.appendChild(li_flip)
    }

    node.appendChild(iw_title_container)
    node.appendChild(iw_subtitle_container)
    node.appendChild(iw_body)
    iw_body.appendChild(ul_flips)
    return node
}

//Create list item of each flipper
function flipHtml(flip) {
    let node = document.createElement("li")
    node.className = "list-group-item py-1 px-2 d-flex justify-content-between align-items-center"
    node.id = flip.flip_objectId

    //node.innerText = flip.modele_nom
    node.setAttribute("data-flip-object-id", flip.flip_objectId)
    node.setAttribute("data-flip-id", flip.flip_id)
    node.setAttribute("data-flip-modele-nom", flip.modele_nom)

    let modelSpan = document.createElement('span')
    modelSpan.className = "model-span"
    modelSpan.innerText = flip.modele_nom

    let btn_group = document.createElement("div")
    btn_group.className = "btn-group ms-3"
    btn_group.setAttribute("role", "group")

    let btn_confirm = document.createElement("button")
    btn_confirm.type = "button"
    btn_confirm.className = "btn btn-primary btn-circle"
    btn_confirm.title = "Actualiser"
    btn_confirm.addEventListener('click', confirmerClicked)
    btn_confirm.innerHTML = '<i class="fas fa-clipboard-check"></i>'

    let btn_refresh = document.createElement("button")
    btn_refresh.type = "button"
    btn_refresh.className = "btn btn-secondary btn-circle"
    btn_refresh.title = "Changement"
    btn_refresh.setAttribute("data-bs-toggle", "modal")
    btn_refresh.setAttribute("data-bs-target", "#changeModal")
    btn_refresh.addEventListener('click', changementClicked)
    btn_refresh.innerHTML = '<i class="fa fa-refresh"></i>'

    //ADDED
    let btn_comment = document.createElement("button")
    btn_comment.type = "button"
    btn_comment.className = "btn btn-info btn-circle"
    btn_comment.title = "Commentaires"
    btn_comment.setAttribute("data-bs-toggle", "modal")
    btn_comment.setAttribute("data-bs-target", "#commentsModal")
    btn_comment.addEventListener('click', commentButtonClicked)
    btn_comment.innerHTML = '<i class="fa fa-regular fa-comment" style="color:#ffffff"></i>'

    let btn_delete = document.createElement("button")
    btn_delete.type = "button"
    btn_delete.className = "btn btn-danger btn-circle"
    btn_delete.title = "Supprimer"
    btn_delete.addEventListener('click', supprimeClicked)
    btn_delete.innerHTML = '<i class="fa fa-trash"></i>'

    node.appendChild(modelSpan)
    btn_group.appendChild(btn_confirm)
    btn_group.appendChild(btn_refresh)
    btn_group.appendChild(btn_comment) //ADDED
    btn_group.appendChild(btn_delete)
    node.appendChild(btn_group)

    return node
}

async function fetchComments(flipId, max, commentType) {
    var posts = []

    //Query definition
    var query = new Parse.Query(Commentaire)
    query.limit(max)
    query.equalTo(commentFields.flipperid, parseInt(flipId))
    query.equalTo(commentFields.actif, true)
    query.equalTo(commentFields.type, commentType)
    query.descending("createdAt")

    const comments = await query.find()

    if (comments.length == 0) {
        console.log("Pas de commentaires")
        return posts
    }

    console.log("Successfully fetched " + comments.length + " comments")

    for (let i = 0; i < comments.length; i++) {
        var post = {
            pseudo: comments[i].get(commentFields.pseudo).toUpperCase() ,
            date: comments[i].get(commentFields.date),
            texte: comments[i].get(commentFields.text)
        }
        posts.push(post)
    }
    //console.log(posts)
    //updateModal(posts)

    return posts
}

function updateModal(posts) {
    var modalCommments = document.querySelector("#commentsModal")
    var listComments = modalCommments.querySelector("#comments-list")
    if (posts.length == 0) {
        var linocom = document.createElement("li")
        linocom.className = 'list-group-item d-flex justify-content-between align-items-center'
        linocom.innerText = 'Pas encore de commentaires'
        listComments.appendChild(linocom)
    }
    posts.forEach(post => {
        var li = document.createElement("li")
        li.className = 'list-group-item d-flex justify-content-between align-items-center fs-6'
        li.innerHTML = post.texte

        var pseudoBadge = document.createElement('span')
        pseudoBadge.className = 'badge bg-primary'
        pseudoBadge.innerText = post.pseudo

        var dateBadge = document.createElement('span')
        dateBadge.className = 'badge bg-light text-dark '
        dateBadge.innerText = post.date

        listComments.appendChild(dateBadge)
        li.appendChild(pseudoBadge)
        listComments.appendChild(li)
    })

}


//========HELPER FUNCTIONS===============================================

//function to make Date object from FLIP_DATMAJ. 
function makeDate(dateString) {
    var parts = dateString.split("/")
    var date = new Date(parts[0], parts[1] - 1, parts[2])
    return date
}

//function to Format date YYYY/MM/DD as String
function formatDate(date) {
    var d = new Date(date),
        month = '' + (d.getMonth() + 1),
        day = '' + d.getDate(),
        year = d.getFullYear()

    if (month.length < 2) month = '0' + month
    if (day.length < 2) day = '0' + day

    return [year, month, day].join('/')
}

//function to add days to a given date. 
function addDays(startDate, numberOfDays) {
    var returnDate = new Date(
        startDate.getFullYear(),
        startDate.getMonth(),
        startDate.getDate() + numberOfDays,
        startDate.getHours(),
        startDate.getMinutes(),
        startDate.getSeconds())
    return returnDate
}
//function to remove days to a given date.
function removeDays(startDate, numberOfDays) {
    var returnDate = new Date(
        startDate.getFullYear(),
        startDate.getMonth(),
        startDate.getDate() - numberOfDays,
        startDate.getHours(),
        startDate.getMinutes(),
        startDate.getSeconds())
    return returnDate
}
//function to calculate the number of days between 2 Dates.
function diffDays(date1, date2) {
    var oneDay = 24 * 60 * 60 * 1000
    return Math.round(Math.abs((date1.getTime() - date2.getTime()) / (oneDay)))
}
//function to calculate days since last update date and return as String
function daysSinceUpdate(datemaj) {
    var today = new Date()
    try {
        var delta = diffDays(today, makeDate(datemaj))
        return "(" + delta + "j)"

    } catch (error) {
        console.log(error.message)
        return "(_j)"
    }
}
//function to return map icon based on FLIP_DATMAJ.
function iconSelect(datemaj) {
    var today = new Date()
    var delta = diffDays(today, datemaj)
    var img = "img/ic_flipmarker_new_mini.png"

    if (delta > 7) {
        img = "img/ic_flipmarker_blue_mini.png"
    }
    if (delta > 60) {
        img = "img/ic_flipmarker_lightblue_mini.png"
    }
    if (delta > 360) {
        img = "img/ic_flipmarker_grey_mini.png"
    }
    return img
}


//=================DOM ELEMENTS CREATION FUNCTIONS====================

//------The location button-----------------------------------------
function addLocationButton(map, marker) {
    var controlDiv = document.createElement('div')

    var firstChild = document.createElement('button')
    firstChild.style.backgroundColor = '#fff'
    firstChild.style.border = 'none'
    firstChild.style.outline = 'none'
    firstChild.style.width = '28px'
    firstChild.style.height = '28px'
    firstChild.style.borderRadius = '2px'
    firstChild.style.boxShadow = '0 1px 4px rgba(0,0,0,0.3)'
    firstChild.style.cursor = 'pointer'
    firstChild.style.marginRight = '10px'
    firstChild.style.marginTop = '10px'
    firstChild.style.padding = '0'
    firstChild.title = 'Your Location'
    controlDiv.appendChild(firstChild)

    var secondChild = document.createElement('div')
    secondChild.style.margin = '5px'
    secondChild.style.width = '18px'
    secondChild.style.height = '18px'
    secondChild.style.backgroundImage = 'url(https://maps.gstatic.com/tactile/mylocation/mylocation-sprite-2x.png)'
    secondChild.style.backgroundSize = '180px 18px'
    secondChild.style.backgroundPosition = '0 0'
    secondChild.style.backgroundRepeat = 'no-repeat'
    firstChild.appendChild(secondChild)

    google.maps.event.addListener(map, 'center_changed', function () {
        secondChild.style['background-position'] = '0 0'
    })

    firstChild.addEventListener('click', function () {
        var imgX = '0',
            animationInterval = setInterval(function () {
                imgX = imgX === '-18' ? '0' : '-18'
                secondChild.style['background-position'] = imgX + 'px 0'
            }, 500)

        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(function (position) {
                var latlng = new google.maps.LatLng(position.coords.latitude, position.coords.longitude)
                marker.setPosition(latlng)
                map.setCenter(latlng)
                clearInterval(animationInterval)
                secondChild.style['background-position'] = '-144px 0'
            })
        } else {
            clearInterval(animationInterval)
            secondChild.style['background-position'] = '0 0'
        }
    })

    controlDiv.index = 1
    map.controls[google.maps.ControlPosition.RIGHT_TOP].push(controlDiv)
}

//------The PlacesAutoComplete SearchBox----------------------------
function addAutoCompleteSearchBox(map) {
    //The AutocompleteSearchBox
    var inputSearchBox = /** @type {!HTMLInputElement} */ (
        document.getElementById('pac-input'))

    // Create the search box and link it to the UI element.
    map.controls[google.maps.ControlPosition.TOP_CENTER].push(inputSearchBox)

    //Add Autocomplete Bar
    var autocomplete = new google.maps.places.Autocomplete(inputSearchBox)
    //autocomplete.setTypes(['address'])
    var marker = new google.maps.Marker({
        map: map,
        anchorPoint: new google.maps.Point(0, -29)
    })
    autocomplete.addListener('place_changed', function () {
        setMapOnAll(null)
        var place = autocomplete.getPlace()
        console.log("Place selected")
        if (!place.geometry) {
            // User entered the name of a Place that was not suggested and
            // pressed the Enter key, or the Place Details request failed.
            window.alert("No details available for input: '" + place.name + "'")
            return
        }
        // If the place has a geometry, then present it on a map.
        if (place.geometry.viewport) {
            map.fitBounds(place.geometry.viewport)
        } else {
            map.setCenter(place.geometry.location)
            map.setZoom(17) // Why 17? Because it looks good.
        }

        marker.setIcon( /** @type {google.maps.Icon} */({
            url: place.icon,
            size: new google.maps.Size(71, 71),
            origin: new google.maps.Point(0, 0),
            anchor: new google.maps.Point(17, 34),
            scaledSize: new google.maps.Size(35, 35)
        }))
        marker.setPosition(place.geometry.location)
        marker.setVisible(true)

    })

    //Clear the autocomplete when click
    inputSearchBox.addEventListener('click', function () {
        this.value = ''
    })

    /*
    //DEPRECATED
    google.maps.event.addDomListener(inputSearchBox, 'click', function () {
        this.value = ''
    })
    */

}

//-----SNACKBAR-----------------------
function snack(htmlContent) {
    // Get the snackbar DIV
    var x = document.getElementById("snackbar")
    // Add HMTL
    x.innerHTML = htmlContent
    // Add the "show" class to DIV
    x.className = "show"

    // After 3 seconds, remove the show class from DIV

    setTimeout(function () {
        x.className = x.className.replace("show", "")
    }, 3000)

}

//-----BACK4APP INTERFACE-------

//TRYING TO REFACTOR - WORKIN PROGRESS
//geoQuery(map).then(plotMarkers(results))


//Return a Promise with the results of the query
function geoQuery(map) {
    //Definition of the innerquery
    var geoboxquery = new Parse.Query(Enseigne)
    var SW = new Parse.GeoPoint({
        latitude: map.getBounds().getSouthWest().lat(),
        longitude: map.getBounds().getSouthWest().lng()
    })
    var NE = new Parse.GeoPoint({
        latitude: map.getBounds().getNorthEast().lat(),
        longitude: map.getBounds().getNorthEast().lng()
    })
    geoboxquery.withinGeoBox("ENS_GEO", SW, NE)

    //Definition of the FlipperQuery
    var query = new Parse.Query(Flipper)
    query.equalTo("FLIP_ACTIF", true)
    query.matchesQuery("FLIP_ENSEIGNE_P", geoboxquery)
    query.include("FLIP_ENSEIGNE_P")
    query.include("FLIP_MODELE_P")
    query.limit(QUERYLIMIT)
    return query.find()
}

//Return a Map (key: EnseigneID, value : Flipper[])
function mapQueryResults(results) {
    let enseigneMap = new Map()
    var flipperArray = []
    if (results.length > 0) {
        for (var i = 0; i < results.length; i++) {
            var flip = {
                modele_nom: results[i].get("FLIP_MODELE_P").get("MOFL_NOM"),
                modele_annee: results[i].get("FLIP_MODELE_P").get("MOFL_ANNEE_LANCEMENT"),
                modele_marque: results[i].get("FLIP_MODELE_P").get("MOFL_MARQUE"),
                ens_objectId: results[i].get("FLIP_ENSEIGNE_P").id,
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
            flipperArray.push(flip)

            var ensId = flip.ens_objectId
            var tempFlipArray = [flip]

            //if key is in map already
            if (enseigneMap.get(ensId) != undefined) {
                //add flip to fliparray in that key
                tempFlipArray = tempFlipArray.concat(enseigneMap.get(ensId))
            }
            enseigneMap.set(ensId, tempFlipArray)
        }
    }
    return enseigneMap

}

//========ACTIONS IN CONTEXT MENU======================================

//Update flip datemaj
function confirmerClicked(event) {
    const button = event.target.closest(".btn")
    const listGroupItem = event.target.closest(".list-group-item")
    const objectId = listGroupItem.dataset.flipObjectId
    const card = event.target.closest(".card")
    //const cardFooter = card.querySelector(".card-footer")

    console.log(`Updating flip ${objectId}`)

    updateParseObject(Flipper, objectId, flipperFields.datemaj, formatDate(new Date()))
    button.setAttribute('disabled', true)
    //cardFooter.innerHTML = vuSince(new Date())
    /*
    const Flipper = Parse.Object.extend("FLIPPER")
    const query = new Parse.Query(Flipper)
    query.get(objectId)
        .then((flipper) => {
            // The object was retrieved successfully.
            console.log("Object " + objectId + " was retrieved successfully")
            flipper.set("FLIP_DATMAJ", formatDate(new Date()))
            var snackText = "Flipper confirmé. Merci "
            snack(snackText)
            return flipper.save()

        }, (error) => {
            // The object was not retrieved successfully.
            // error is a Parse.Error with an error code and message.
            console.log("Error retrieving object " + objectId + " : " + error.code + " : " + error.message)
        })
    */
}

//Parse updateField function
function updateParseObject(parseObject, objectId, paramName, newValue) {
    console.log("running updateParseObject from script.js")
    const query = new Parse.Query(parseObject)
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
        })
}

//Puts the flip in the TrashList
function supprimeClicked(event) {
    const button = event.target.closest(".btn")
    const listGroupItem = event.target.closest(".list-group-item")
    const objectId = listGroupItem.dataset.flipObjectId
    const flipId = parseInt(listGroupItem.dataset.flipId)
    const card = event.target.closest(".card")

    console.log(`Notifying disparition of flip ${objectId}`)

    const query = new Parse.Query(FlipTrash)
    query.equalTo(trashListFields.processed, false)
    query.equalTo(trashListFields.id, flipId)
    //query.limit(1)
    query.find().then(function (results) {
        if (results.length > 0) {
            console.log("Flip deja present dans la trash list : no need to add ")
        }
        else {
            var newFlipTrash = new Parse.Object("FLIPTRASH")
            newFlipTrash.set(trashListFields.id, flipId)
            newFlipTrash.set(trashListFields.processed, false)
            newFlipTrash.set(trashListFields.pseudo, "WEB")
            return newFlipTrash.save()
        }
    }).then(fliptrash => {
        console.log('New object created with objectId: ' + fliptrash.id)
        snack("Notification de disparition envoyée. Merci")
        button.setAttribute('disabled', true)

    }, error => {
        console.log("Error: " + error.code + " " + error.message)
    }
    )
}

//Opens modal and display comments
function commentButtonClicked(event) {
    console.log("Comment Button clicked!")
    var listItem = event.target.closest(".list-group-item")
    var listComments = document.querySelector("#comments-list")

    const MAX_COMMENTS = 5
    const COMMENT_TYPE = "POST"
    var posts = []

    const flip = {
        "flip_objectId": listItem.dataset.flipObjectId,
        "modele_nom": listItem.dataset.flipModeleNom,
        "flip_id": parseInt(listItem.dataset.flipId)
    }


    //clear existing list items
    while (listComments.firstChild) {
        listComments.removeChild(listComments.lastChild);
    }

    posts = fetchComments(flip.flip_id, MAX_COMMENTS, COMMENT_TYPE)
        .then(posts => {
            updateModal(posts)
        }
        )



}

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
    const found = selectedModeleNom.match(regex)
    var shortSelectedModelNom = found == null ? "refresh page" : found[0]
    console.log(shortSelectedModelNom)


    if (selectedModele == "Choisir Modèle") {
        alert("Selectionner un modèle")
        return
    }
    const newFlipModelObjectId = selectedModele

    console.log("flipObjectId/ID: " + currentFlipObjectId + "(" + currentFlipId + "). Current Modele : " + currentFlipModeleNom + " modeleID selected  " + newFlipModelObjectId)

    //besoin de flip objectid, de flip id, de flip modele id

    console.log(`Changing modele of flip ${currentFlipObjectId}: ${currentFlipModeleNom} > ${newFlipModelObjectId} `)
    //RECUPERER LE FLIP PARSEOBJECT

    const query = new Parse.Query(Flipper)
    query.get(currentFlipObjectId)
        .then((flipper) => {
            // The object was retrieved successfully.
            console.log("Object " + flipper.id + " was retrieved successfully")
            flipper.set(flipperFields.datemaj, formatDate(new Date()))
            flipper.set(flipperFields.modele, getMODELID(newFlipModelObjectId))
            flipper.set(flipperFields.modele_p, Modele.createWithoutData(newFlipModelObjectId))

            var snackText = "Modèle modifié. Merci "
            snack(snackText)
            return flipper.save()

        }, (error) => {
            // The object was not retrieved successfully.
            // error is a Parse.Error with an error code and message.
            console.log("Error retrieving object " + currentFlipObjectId + " : " + error.code + " : " + error.message)
        })

    //TODO fermer le modal et modifier le modele dans l'info window ()

    //CREER COMMENTAIRE REMPLACEMENT
    listToSave = []
    var Commentaire = Parse.Object.extend("COMMENTAIRE")
    var newCommentaire = new Commentaire()

    newCommentaire.set(commentFields.id, new Date().getTime())
    newCommentaire.set(commentFields.text, `Le ${currentFlipModeleNom} a été remplacé`)
    newCommentaire.set(commentFields.actif, true)
    newCommentaire.set(commentFields.date, formatDate(new Date()))
    newCommentaire.set(commentFields.flipperid, parseInt(currentFlipId))
    newCommentaire.set(commentFields.flipperid_p, Flipper.createWithoutData(currentFlipObjectId))
    newCommentaire.set(commentFields.type, "REPLACED")
    newCommentaire.set(commentFields.pseudo, "WEB")
    listToSave.push(newCommentaire)

    saveAllParseObjects(listToSave)

    //Ferme le Modal
    $('#changeModal').modal('hide')

    //update listitem
    var listitem = document.querySelector(`#${currentFlipObjectId}`)
    var texteModel = listitem.querySelector(".model-span")
    console.log(texteModel)
    texteModel.innerText = shortSelectedModelNom
    listitem.setAttribute("data-flip-modele-nom", shortSelectedModelNom)
}

var modelsMap = new Map()

init_modelsMap()

function init_modelsMap() {
    var query = new Parse.Query(Parse.Object.extend("MODELE_FLIPPER"))
    query.limit(350)
    query.ascending("MOFL_NOM")
    query.find({
        success: function (results) {
            console.log("Successfully retrieved " + results.length + " models.")
            if (results.length > 0) {
                for (let i = 0; i < results.length; i++) {
                    modelsMap.set(results[i].id,
                        [results[i].attributes.MOFL_ID,
                        results[i].attributes.MOFL_NOM,
                        results[i].attributes.MOFL_ANNEE_LANCEMENT,
                        results[i].attributes.MOFL_MARQUE
                        ])
                }
                //once modelsMap is initialized, fill first model dropdown
                populate("modeleflipper")
            }
        },
        error: function (error) {
            console.log("Error: " + error.code + " " + error.message)
        }
    })
}

//-- -- -- -- -- - POPULATE DROPDOWN-- -- -- -- -- -- -
function populate(selectElement) {
    let dropdown = document.getElementById(selectElement)
    //dropdown.length = 0
    let defaultOption = document.createElement('option')
    defaultOption.text = 'Choisir Modèle'
    dropdown.add(defaultOption)
    dropdown.selectedIndex = 0

    var modelsMapAsc = new Map([...modelsMap.entries()].sort())

    for (let model of modelsMap.entries()) {

        option = document.createElement('option')
        var longtext = model[1][1] + " (" +
            model[1][3] + ", " + model[1][2] + ")"
        var objectId = model[0]
        //console.log(longtext)
        option.text = longtext
        option.value = objectId
        dropdown.add(option)
    }
}

//Maps results to flip object NOT USED
function flipFromJson(results) {
    var flip
    if (results.length > 0) {
        flip = {
            modele_nom: results[i].get("FLIP_MODELE_P").get("MOFL_NOM"),
            modele_annee: results[i].get("FLIP_MODELE_P").get("MOFL_ANNEE_LANCEMENT"),
            modele_marque: results[i].get("FLIP_MODELE_P").get("MOFL_MARQUE"),
            ens_objectId: results[i].get("FLIP_ENSEIGNE_P").id,
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
        return flip
    } else return null
}

//TRY TO DELETE ++++ only used in setMapOnAll(null)
// Sets the map on all markers in the array.
function setMapOnAll(map) {
    for (var i = 0; i < markers.length; i++) {
        markers[i].setMap(map)
    }
}

//-- -- -- -- -- -Other  Helper functions-- -- -- -- -- -

function getMODELID(objectId) {
    return modelsMap.get(objectId)[0]
}

function getMODELNAME(objectId) {
    return modelsMap.get(objectId)[1]
}


function saveParseObject(parseObject) {
    parseObject.save()
        .then((parseObject) => {
            // Execute any logic that should take place after the object is saved.
            alert('New object created with objectId: ' + parseObject.id)
        }, (error) => {
            // Execute any logic that should take place if the save fails.
            // error is a Parse.Error with an error code and message.
            alert('Failed to create new object, with error code: ' + error.message)
        })
}

function saveAllParseObjects(listParseObjects) {
    Parse.Object.saveAll(listParseObjects, {
        success: function (listParseObjects) {
            console.log('All objects were saved')
        },
        error: function (error) {
            // An error occurred while saving one of the objects.
            alert('Failed to create new object, with error code: ' + error.message)
        },
    })
}

