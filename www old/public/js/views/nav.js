/*global define*/
define([
    'jquery',
    'underscore',
    'backbone',
    'text!templates/login.html',
    'i18n!nls/main',
    'conf'
], function ($, _, Backbone, tpl, lang, conf) {
    'use strict';
    var NavBar = Backbone.View.extend({
        initialize:function(options){
            Backbone.history.on('route', function(source, path){
                this.render(path);
            }, this);
        },
        titlesAuth: {
            "#logout": lang.logout
        },
        titlesNoAuth: {
            '#bars': lang.bars,
            '#pinballs': lang.pinballs,
            "#login": lang.login,
            "#signup": lang.signup,
        },
        events:{
            'click a':function(source) {
                var hrefRslt = source.target.getAttribute('href');
                Backbone.history.navigate(hrefRslt, {trigger:true});
                return false;
            }
        },
        //Each time the routes change, we refresh the navigation
        //items.
        render:function(route, auth){
            this.$el.empty();
            var template = _.template("<li class='<%=active%>'><a href='<%=url%>'><%=name%></a></li>");
            var titles = auth ? this.titlesAuth : this.titlesNoAuth;
            route = route.split('/')[0];
            for (var key in titles) {
                this.$el.append(template({url:key,
                                         name:titles[key],
                                         active: (route === key) ? 'active' : ''})
                               );
            }
        }
    });
    return NavBar;
});
