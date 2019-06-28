define([
    'underscore',
    'backbone',
    'models/pinball',
    'conf',
    'backbone.localStorage'
], function (_, Backbone, Pinball, conf) {
    'use strict';
    var PinballsCollection = Backbone.Collection.extend({
        model: Pinball,
        localStorage: new Backbone.LocalStorage("pinballs"),
        urlRoot:  conf.get_url(conf.mode) + "v1/pinballs",
        url: function() {
            return this.urlRoot;
        },

    });
    return new PinballsCollection();
});
