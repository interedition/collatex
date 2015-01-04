module.exports = {
    options: {
        //transform: [['jstify', { engine: 'lodash' }]],
        watch: true,
        browserifyOptions: { debug: true }
    },
    dist: {
        dest: 'htdocs/collatex.js',
        src: [ 'google-code-prettify/prettify.js', 'collatex.js' ]
    }
};