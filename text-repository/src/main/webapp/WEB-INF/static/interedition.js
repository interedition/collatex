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

function TextView(config) {
    TextView.superclass.constructor.apply(this, arguments);
}

TextView.NAME = "text-view";

YUI().use("widget", function(Y) {
    Y.extend(TextView, Y.Widget, {
        renderUI: function() {
            alert("Text View!");
        }
    });
});
