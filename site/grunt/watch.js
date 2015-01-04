module.exports = {
    livereload: {
        files: 'htdocs/*',
        options: { livereload: true }
    },
    templates: {
        files: '**/*.jade',
        tasks: 'jade'
    },
    stylesheets: {
        files: '**/*.less',
        tasks: 'less'
    }
};