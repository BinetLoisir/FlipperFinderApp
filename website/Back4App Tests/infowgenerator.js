var iw = document.createElement('div');
iw.id = "iw-container;

var title = document.createElement('div');
title.classList.add("iw-title");
iw.append(title);

var list = document.createElement('div');
list.classList.add("iw-content");
list.append("<ul></ul>");


array = [flip1, flip2.flip3];
array[0].modele_nom = "stiff";
array[0].modele_nom = "circus";
array[0].modele_nom = "trek";
array[0].modele_marque = "stern";
array[0].modele_marque = "bally";
array[0].modele_marque = "stern";
array[0].modele_annee = "1987";
array[0].modele_annee = "1876";
array[0].modele_annee = "1987";
array[0].flip_objectId = "kjsdbfkdjfytks";
array[0].flip_objectId = "fsldjnfdkddfcf";
array[0].flip_objectId = "piosjfdfdksbfd";
array[0].flip_id = 12;
array[0].flip_id =10;
array[0].flip_id = 56;

for (var flip in array) {
    var listitem = document.createElement('li');
    var li = "<li>";
    var nomlong = flip.modele_nom + ' (' + flip.modele_marque + ', ' + flip.modele_annee + ') &nbsp';
    var upt_button = document.createElement('button');
    upt_button.id = "update";
    upt_button.title="Actualiser!";
    //button.setAttribute('class', 'btn btn-default');
    button.setAttribute('data-toggle', "tooltip");
    upt_button.innerHTML('<i class="fa fa-fw fa-refresh"></i>')
    upt_button.onclick(updateFlip(flip.flip_objectId));

        li = li.concat(array[i].) $("ul").append(li);
}









var arr = ["list", "items", "here"];
list.append("<ul></ul>");
for (var i in arr) {
    var li = "<li>";
    $("ul").append(li.concat(arr[i]))
}
