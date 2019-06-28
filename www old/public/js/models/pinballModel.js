define([
    'underscore',
    'backbone',
    'conf'
], function (_, Backbone, conf) {
    'use strict';

    var Pinball = Backbone.Model.extend({
        idAttribute: 'MOFL_ID',
        defaults: {
            MOFL_ANNEE_LANCEMENT: null,
            MOFL_ID: -1,
            MOFL_MARQUE: null,
            MOFL_NOM: "Unknown"
        },
        urlRoot:  conf.get_url(conf.mode) + "v1/model",

    });

    return Pinball;
});
