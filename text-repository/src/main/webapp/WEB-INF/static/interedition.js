function post(uri, req, callback) {
    YUI().use("io", "json", function(Y) {
        resp = Y.io(uri, {
            method: "post",
            headers: {
                "Content-Type": "application/json",
                "Accept": "application/json"
                },
            data: Y.JSON.stringify(req),
            on: {
                success: function(transactionId, resp) { callback(Y.JSON.parse(resp.responseText)); }
                }
            });
    });
}

function TextEditor(config) {
    TextEditor.superclass.constructor.apply(this, arguments);
}

TextEditor.NAME = "text-editor";

YUI().use("editor-base", "widget", function(Y) {
    Y.extend(TextEditor, Y.Widget, {
        renderUI: function() {

        }
    });
});
