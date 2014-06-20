define(function (require) {

    var Backbone = require('backbone');

    var Person = require('js/model/person');

    var PersonCollection = Backbone.Collection.extend({

        url: '/api/people',

        model: Person

    });

    return PersonCollection;

});
