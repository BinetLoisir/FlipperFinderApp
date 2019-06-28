var placeSearch, autocomplete;
var componentForm = {
    street_number: 'short_name',
    route: 'long_name',
    locality: 'long_name',
    country: 'long_name',
    postal_code: 'short_name'
};

function initAutocomplete() {
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

//-- -- -- -- -- - ADD MODEL-- -- -- -- -- -- -
var counter = 1;
var limit = 10;

function addInput(divName) {
    if (counter == limit) {
        alert("You have reached the limit of adding " + counter + " inputs");
    } else {
        var newdiv = document.createElement('div');
        newdiv.innerHTML = "<label for='select'> Modèle " + (counter + 1) + "</label>" + "<select class='selectpicker' data-live-search='true' data-width=100% id='modele-flipper5'</select>";
        document.getElementById(divName).appendChild(newdiv);
        counter++;
    }
}

//-- -- -- -- -- - ADD MODEL VARIANTE-- -- -- -- -- -- -

var nbmodel = 1
var nbmodelmax = 5


function addModel() {
    if (nbmodel == nbmodelmax) {
        alert("Nombre max de modeles atteint")
    } else {
        nbmodel++;
        var nextmodel = document.getElementById('modele' + nbmodel);
        populateModels("modeleflipper" + nbmodel);
        nextmodel.style.display = 'block';

    }
}

//-- -- -- -- -- - REMOVE MODEL VARIANTE-- -- -- -- -- -- -
function removeModel() {
    if (nbmodel == 1) {
        alert("Choisir au moins un modéle")
    } else {
        var nextmodel = document.getElementById('modele' + nbmodel);
        nextmodel.style.display = 'none';
        nbmodel--;


    }
}

//-- -- -- -- -- - POPULATE MODEL-- -- -- -- -- -- -
var dropdownName = "modeleflipper1";
populateModels(dropdownName);

function populateModels(selectElement) {
    let dropdown = document.getElementById(selectElement);

    dropdown.length = 0;

    let defaultOption = document.createElement('option');
    defaultOption.text = 'Choisir Modèle';

    dropdown.add(defaultOption);
    dropdown.selectedIndex = 0;

    const url = 'https://parseapi.back4app.com/classes/MODELE_FLIPPER/?limit=250';

    fetch(url, {
            headers: {
                "X-Parse-Application-Id": "wx8ZJI9628FDGq39REy6rMlZjKdP5ERUMXjZpqjE",
                "X-Parse-Rest-Api-Key": "FUuJ7wft2vxbzFGHEZno6sx5AuuJEYl7yEYQlwCc"
            }
        })
        .then(
            function (response) {
                if (response.status !== 200) {
                    console.warn('Looks like there was a problem. Status Code: ' +
                        response.status);
                    return;
                }
                // Examine the text in the response
                response.json().then(function (data) {
                    let option;
                    //order the results
                    data.results.sort(function (a, b) {
                        return compareStrings(a.MOFL_NOM, b.MOFL_NOM);
                    })
                    for (let i = 0; i < data.results.length; i++) {
                        option = document.createElement('option');
                        option.text = data.results[i].MOFL_NOM + " (" +
                            data.results[i].MOFL_MARQUE + ", " + data.results[i].MOFL_ANNEE_LANCEMENT + ")";
                        option.value = data.results[i].MOFL_NOM;
                        dropdown.add(option);

                    }
                    $('#' + selectElement).selectpicker('refresh');
                });
            }
        )
        .catch(function (err) {
            console.error('Fetch Error -', err);
        });
};


//String Comparison Function
function compareStrings(a, b) {
    // Assuming you want case-insensitive comparison
    a = a.toLowerCase();
    b = b.toLowerCase();

    return (a < b) ? -1 : (a > b) ? 1 : 0;
}




function updateFlip(flipID) {
    var today = new Date();
    var todayFormated = formatDate(today);
    var url = 'https://parseapi.back4app.com/classes/FLIPPER/' + flipID;
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
