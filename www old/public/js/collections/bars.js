define([
    'underscore',
    'backbone',
    'models/bar',
    'conf',
    'backbone.localStorage'
], function (_, Backbone, Bar, conf) {
    'use strict';
    var BarsCollection = Backbone.Collection.extend({
        model: Bar,
        sort_key : 'ENS_NOM',
        comparator: function(item) {
            return item.get(this.sort_key);
        },
        sortByField: function(fieldName) {
            this.sort_key = fieldName;
            this.sort();
        },
        localStorage: new Backbone.LocalStorage("bars"),
        urlRoot:  conf.get_url(conf.mode) + "v1/bars",
        url: function() {
            return this.urlRoot;
        },

    });
    return new BarsCollection();
});
