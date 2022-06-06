
//test login
Parse.User.logIn("user", "pass", {
    success: function (user) {
        query.find({
            success: function (results) {
                results[0].save({ key: value }, {
                    success: function (result) {
                        // the object was saved.
                    }
                });
            }
        });
    }
});



Parse.User.logIn("user", "pass").then(function (user) {
    return query.find();
}).then( (results) => {
    return results[0].save({ key: value });
}).then( (result) => {
    // the object was saved.
});


const query = new Parse.Query("Student");
query.descending("gpa");
query.find().then(function(students) {
  students[0].set("valedictorian", true);
  return students[0].save();

}).then(function(valedictorian) {
  return query.find();

}).then(function(students) {
  students[1].set("salutatorian", true);
  return students[1].save();

}).then(function(salutatorian) {
  // Everything is done!

});

