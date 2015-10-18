module.exports = {
    dist: {
        expand: true,
        cwd: 'templates',
        src: ['**/*.jade', '!page.jade'],
        dest: 'htdocs',
        ext: '.html',
        options: {
            data: {
                version: "1.7.0"
            }
        }
    }
};
