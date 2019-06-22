/*global define*/
define([
    'backbone',
    'baseRouter',
    'views/bars',
    'views/nav',
    'views/login',
    'views/pinballs',
    'conf'
], function (Backbone, BaseRouter, BarsView, NavBar, LoginView, PinBallsView,
             UsersView, ProductsView, FamiliesView, conf) {
    var Router = BaseRouter.extend({
        initialize: function() {
            this.views = {};
            this._G = {};
            this.navBar = new NavBar({el:document.getElementById('nav-item-container')});
        },
        routes: {
            'login': 'login',
            'logout': 'logout',
            'pinballs': 'pinballs',
            'pinballs/:id': 'pinball',
            'bars': 'bars',
            'bars/:id': 'bar',
            '': 'pinballs'
        },
        noAuth : ['', '#login', '#pinballs', '#bars'],
        before: function(params, next) {
            var jwt = localStorage.getItem("jwt");
            var path = Backbone.history.location.hash;
            var needAuth = !_.contains(this.noAuth, path);
            if (jwt == null && needAuth) {
                Backbone.history.navigate('login', { trigger : true });
            } else {
                next();
            }

        },
        after:  function() {
            var path = Backbone.history.location.hash;
            var jwt = localStorage.getItem("jwt");
            this.navBar.render(path, jwt != null);
        },
        changeView : function(view){
            function setView(view){
                if(this.currentView){
                    if (this.currentView.close != undefined) {
                        this.currentView.close();
                    }
                }
                this.currentView = view;
                $('.main').html(view.$el);
                view.render();
            }

            $('.wait').hide();
            setView(view);
            if (this.fetchDone === undefined) {
                this.cols.pinballs.fetch({ajaxSync: true ,reset: true});
                this.fetchDone = true;
            }
        },
        login: function(force = false) {
            var jwt = localStorage.getItem("jwt");
            var path = Backbone.history.location.hash;
            if (!force && jwt != null) {
                Backbone.history.navigate('', { trigger : true });
                return;
            }
            var loginView = new LoginView();
            this.changeView(loginView);
        },
        logout: function() {
            localStorage.removeItem('jwt');
            Backbone.history.navigate('login', { trigger : true });
        },
        bars: function () {
            if (this.views.barsView == undefined || this.views.barsView == null) {
                this.views.barsView = new BarsView({collection: this.cols.bars, router: this});
            }
            this.changeView(this.views.barsView);

        },
        pinballs: function () {
            if (this.views.pinballsView == undefined || this.views.pinballsView == null) {
                this.views.pinballsView = new PinBallsView({collection: this.cols.pinballModels, router: this});
            }
            this.changeView(this.views.pinballsView);

        },
        pinball: function (id) {
        }
    });
    return Router;
});
