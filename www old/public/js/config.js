"use strict";
define(function(){
    return {
        mode: "dev",
        google: {
            apiKey:"AIzaSyAV-jLlRRCIPYAvmTwVM_e2sE-LnDrCNSg"
        },
        get_url: function (mode) {
            if (mode == "prod") {
                return "https://prod-api.findMyBalls.com/";
            }
            if (mode == "preprod") {
                return "https://api.findMyBalls.com/";
            }
            return "http://localhost/flipper/";
        }
    };
});
