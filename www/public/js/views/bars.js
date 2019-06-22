/*global define*/
define([
    'jquery',
    'underscore',
    'backbone',
    'moment',
    'text!templates/bar/view.html',
    'text!templates/bar/list.html',
    'i18n!nls/colors',
    'conf'
], function ($, _, Backbone,  Moment,tpl, container, lang, conf) {
    'use strict';
    var BarsView = Backbone.View.extend({
        family : [],
        initialize: function(obj) {
            this.list_el= '#list';
            this.template= _.template(tpl);
            this.cont= _.template(container);
            this.collection.on('reset', function() {this.renderList()}, this);
            this.listenTo(obj.router, "allDone", this.render);
            if (obj.router.colsLoaded) {
                this.render();
            }
            this.cols = obj.router.cols;
        },
        renderList: function(col) {
            this.collection.sort();
            if (col == undefined)
                col = this.collection.models;
            if (col.length <= 0)
                return;
            $('.wait').hide();
            var $list_el =  $(this.el).find(this.list_el);
            $list_el.empty();
            var that = this;
            _.each(col, function(elem) {
                elem.attributes._updateDate = Moment(elem.attributes.updatedAt).format('DD/MM/YY, HH:mm');
                $list_el.append(that.template(elem.attributes));
            });
            $('.maplink').unbind();
            $('.maplink').click(function(){
                $("tr.active").removeClass("active");
                $(this).closest("tr").addClass("active");
                var pos = {lat:$(this).data("lat"), lng:$(this).data("long")};
                that.marker.setPosition(pos);
                that.map.panTo(pos);
                that.infoW.setContent($(this).data('name'));
                that.infoW.open(that.map, that.marker);
                var bar_id = $(this).data('id');
                var pinballs = that.cols.pinballs.filter(function(pb){
                    return pb.get("FLIP_ENSEIGNE") == bar_id});
                var info = '<h1>'+$(this).data('name')+'</h1><ul>';
                var tpl = _.template("<li><%= MOFL_NOM %></li>");
                _.each(pinballs, function(pb) {
                    var model = that.cols.pinballModels.get(pb.get("FLIP_MODELE"));
                    info += tpl(model.attributes);
                });
                info += '</ul>';
                that.infoW.setContent(info);
            });
        },
        render: function() {
            $(this.el).html(this.cont({lang: lang}));
            this.renderList();
            var that = this;
            require(['async!http://maps.google.com/maps/api/js?key=' + conf.google.apiKey + '!callback'], function() {
                    // Google Maps API and all its dependencies will be loaded here.
                console.log("bite");
                var mapDiv = document.getElementById('map-canvas');

                var myLatLng = {lat: 48.7118386, lng: 2.2099839};
                that.map = new google.maps.Map(mapDiv, {
                    center: myLatLng,
                    zoom: 17,
                    mapTypeId: google.maps.MapTypeId.ROADMAP,
                    navigationControl: true,
                    navigationControlOptions: {
                        style: google.maps.NavigationControlStyle.SMALL
                    }
                });
                that.marker = new google.maps.Marker({
                        position: myLatLng,
                        map: that.map,
                        title: 'Where all begins'
                      });
                that.infoW = new google.maps.InfoWindow({
                    content: 'Where all begins'
                });
                that.infoW.open(that.map, that.marker);
            });
            return this;
        }
    });
    return BarsView;
});
