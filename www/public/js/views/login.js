/*global define*/
define([
    'jquery',
    'underscore',
    'backbone',
    'text!templates/login.html',
    'conf'
], function ($, _, Backbone, tpl, conf) {
    'use strict';
    var LoginView = Backbone.View.extend({

        template: _.template(tpl),
        initialize:function () {
        },

        events: {
            "click #loginButton": "login"
        },

        render:function () {
            $(this.el).html(this.template());
            return this;
        },

        login:function (event) {
            event.preventDefault();    // Don't let this button submit the form
            $('.alert-error').empty(); // Purge any errors on a new submit
            $('.alert-error').hide();  // Hide any errors on a new submit
            var url = conf.get_url(conf.mode, "auth") + 'v1/account/auth';
            var formValues = {
                email: $('#inputEmail').val(),
                password: $('#inputPassword').val()
            };

            localStorage.removeItem("jwt");
            $.ajax({
                url:url,
                type:'POST',
                dataType:"json",
                contentType: "application/json; charset=utf-8",
                data: JSON.stringify(formValues),
                success:function (data) {
                    if(data.error) {  // If there is an error, show the error messages
                        $('.alert-error').text(data.error.text).show();
                    }
                    else { // If not, send them back to the home page
                        localStorage.setItem("jwt", data.token);
                        var user = JSON.parse(atob(data.token.split('.')[1]));
                        localStorage.setItem('locale', user.context.locale.toLowerCase());
                        location.reload();
                    }
                }
            });
        }
    });
    return LoginView;
});

