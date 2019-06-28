define([
    'underscore',
    'backbone',
    'conf'
], function (_, Backbone, conf) {
    'use strict';

    var Pinball = Backbone.Model.extend({
        idAttribute: 'FLIP_ID',

        defaults: {
            FLIP_ACTIF: false,
            FLIP_DATMAJ: "",
            FLIP_ENSEIGNE: -1,
            FLIP_ID: -1,
            FLIP_MODELE: -1

        },
        urlRoot:  conf.get_url(conf.mode) + "v1/pinnball",

    });

    return Pinball;
});
