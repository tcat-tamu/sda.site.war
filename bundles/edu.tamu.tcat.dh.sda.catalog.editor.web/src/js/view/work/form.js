define(function (require) {

    var Backbone = require('backbone'),

        AuthorRef              = require('js/model/author_ref'),
        Title                  = require('js/model/title'),
        TitleSubform           = require('js/view/work/title_subform'),
        AuthorRefSubform       = require('js/view/work/author_ref_subform'),
        PublicationInfoSubform = require('js/view/work/publication_info_subform'),

        Message = require('js/view/message');

    require('backbone.epoxy');

    var WorkFormView = Backbone.Epoxy.View.extend({
        tagName: 'form',

        template: require('tpl!templates/work/form.html.ejs'),

        clearAfterSave: false,

        bindings: {
            '.series': 'value:series,events:["keyup"]',
            '.summary': 'value:summary,events:["keyup"]'
        },

        events: {
            'click .add-primary-author': 'addPrimaryAuthorForm',
            'click .add-other-author': 'addOtherAuthorForm',
            'click .add-title': 'addTitleForm',
            'submit': 'submit',
            'click .save-add-button': function (evt) { this.clearAfterSave = true; }
        },

        addTitleForm: function (evt) {
            var model = new Title();
            this.model.get('titles').add(model);

            var view = new TitleSubform({ model: model, allowRemoval: true });
            var view$el = view.render().$el;

            view$el.hide();
            this.$el.find('.title-forms').append(view$el);
            view$el.slideDown(300);
        },

        addPrimaryAuthorForm: function (evt) {
            var model = new AuthorRef();

            this.model.get('authors').add(model);

            var view = new AuthorRefSubform({ model: model, allowRemoval: true });
            var view$el = view.render().$el;

            view$el.hide();
            this.$el.find('.author-forms').append(view$el);
            view$el.slideDown(300);
        },

        addOtherAuthorForm: function (evt) {
            var model = new AuthorRef();

            this.model.get('otherAuthors').add(model);

            var view = new AuthorRefSubform({ model: model, allowRemoval: true });
            var view$el = view.render().$el;

            view$el.hide();
            this.$el.find('.other-author-forms').append(view$el);
            view$el.slideDown(300);
        },

        submit: function (evt) {
            evt.preventDefault();

            var _this = this;
            this.model.save({}, {
                success: function (model, response, options) {
                    var alert = new Message({
                        type: 'success',
                        ttl: 5000,
                        message: 'Work saved successfully.'
                    });

                    alert.render();

                    if (_this.clearAfterSave) {
                        console.log('save and new');
                    }
                },
                error: function (model, response, options) {
                    var alert = new Message({
                        type: 'error',
                        message: 'Unable to save work.'
                    });

                    alert.render();
                }
            });

            return false;
        },

        render: function () {
            this.$el.html(this.template({
                model: this.model.toJSON()
            }));

            var $authorForms = this.$el.find('.author-forms').empty();
            this.model.get('authors').each(function (author) {
                var subForm = new AuthorRefSubform({ model: author, allowRemoval: true });
                $authorForms.append(subForm.render().el);
            });

            var $titleForms = this.$el.find('.title-forms').empty();
            this.model.get('titles').each(function (title) {
                var subForm = new TitleSubform({ model: title, allowRemoval: true });
                $titleForms.append(subForm.render().el);
            });

            var $otherAuthorForms = this.$el.find('.other-author-forms').empty();
            this.model.get('otherAuthors').each(function (otherAuthor) {
                var subForm = new AuthorRefSubform({ model: otherAuthor });
                $otherAuthorForms.append(subForm.render().el);
            });

            var pubInfoSubform = new PublicationInfoSubform({ model: this.model.get('pubInfo') });
            this.$el.find('.pub-info-form').html(pubInfoSubform.render().el);

            this.applyBindings();

            return this;
        }
    });

    return WorkFormView;

});
