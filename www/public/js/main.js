'use strict';
var locale = localStorage.getItem('locale') || 'fr-fr';
// convert Google Maps into an AMD module

require.config({
        shim: {
                underscore: {
                        exports: '_'
                },
                backbone: {
                        deps: [
                                'underscore',
                                'jquery'
                        ],
                        exports: 'Backbone'
                },
                'backbone.localStorage': {
                    deps: ['backbone'],
                    exports: 'Backbone'
                },
                highcharts: {
                    exports: "Highcharts",
                    deps: ["jquery"]
                },
                bootstrap: {
                    deps: ["jquery"]
                }
        },
        paths: {
            async: '../lib/requirejs-plugins/src/async',
            backbone: '../lib/backbone/backbone-min',
            'backbone.localStorage':'../lib/backbone.localStorage/backbone.localStorage',
            bootstrap: '../lib/bootstrap/dist/js/bootstrap.min',
            conf:'config',
            highcharts:'../lib/highcharts/highcharts',
            i18n:'../lib/i18n/i18n',
            jquery: '../lib/jquery/dist/jquery.min',
            moment:'../lib/moment/min/moment.min',
            text:'../lib/text/text',
            underscore: '../lib/underscore/underscore-min'

        },
        config: {
            i18n: {
                locale: locale
            }
        },
        waitSeconds : 300,

});

require([
        'jquery',
        'backbone',
        'bootstrap',
        'collections/pinballs',
        'collections/pinballModels',
        'collections/bars',
        'router'
], function ($, Backbone, Bootstrap, Pinballs, PinballModels, Bars, Router, Workspace) {
    var originalSync = Backbone.sync;
    Backbone.sync = function(method, model, options) {
        options.headers = options.headers || {};
        var token = localStorage.getItem("jwt");
        if (token != null) {
            _.extend(options.headers, { 'Authorization': 'Bearer ' + token});
            $.ajaxSetup({
                beforeSend: function(xhr, settings) {
                    if (settings !== undefined && settings.jwt !== undefined) {
                        xhr.setRequestHeader('Authorization', 'Bearer ' + settings.jwt);
                        return;
                    }
                    var token = localStorage.getItem("jwt");
                    if (token != null) {
                        xhr.setRequestHeader('Authorization', 'Bearer ' + token);
                    }
                }
            });
        }
        originalSync.call(model, method, model, options);
    }

    var router = new Router();
    router.cols = {};
    router.cols.pinballs = Pinballs;
    router.cols.bars = Bars;
    router.cols.pinballModels = PinballModels;
    router.colsLoaded = false;
    $.when(router.cols.pinballModels.fetch({ajaxSync: true ,reset: true}),
        router.cols.bars.fetch({ajaxSync: true ,reset: true}),
        router.cols.pinballs.fetch({ajaxSync: true ,reset: true}))
        .done(function(){
            router.colsLoaded = true;
            router.trigger('allDone');
            console.log("AllDone");
        });
    $.ajaxSetup({
        cache: false,
        statusCode: {
            401: function (data) {
                router.login(true);
                $('.alert-error').text("Unauthorized").show();
            },
            403: function (data) {
                router.login(true);
                $('.alert-error').text("Bad credentials").show();
            }
        }
    });

    Backbone.history.start();
});
