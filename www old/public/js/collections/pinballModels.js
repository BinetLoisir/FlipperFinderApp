define([
    'underscore',
    'backbone',
    'models/pinballModel',
    'conf',
    'backbone.localStorage'
], function (_, Backbone, PinballModel, conf) {
    'use strict';
    var PinballsCollection = Backbone.Collection.extend({
        model: PinballModel,
        sort_key : 'MOFL_NOM',
        comparator: function(item) {
            return item.get(this.sort_key);
        },
        sortByField: function(fieldName) {
            this.sort_key = fieldName;
            this.sort();
        },
        localStorage: new Backbone.LocalStorage("pinballModels"),
        urlRoot:  conf.get_url(conf.mode) + "v1/models",
        url: function() {
            return this.urlRoot;
        },

    });
    return new PinballsCollection();
});
