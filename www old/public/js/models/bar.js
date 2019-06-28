define([
    'underscore',
    'backbone',
    'conf'
], function (_, Backbone, conf) {
    'use strict';

    var Bar = Backbone.Model.extend({
        idAttribute: 'ENS_ID',
        defaults: {
            ENS_ADRESSE:"",
            ENS_CODE_POSTAL:"",
            ENS_DATMAJ:null,
            ENS_HORAIRE:null,
            ENS_ID:-1,
            ENS_LATITUDE:null,
            ENS_LONGITUDE:null,
            ENS_NOM:"",
            ENS_PAYS:"France",
            ENS_TYPE:"Bar",
            ENS_VILLE:""
        },
        urlRoot:  conf.get_url(conf.mode) + "v1/bar",

    });

    return Bar;
});
