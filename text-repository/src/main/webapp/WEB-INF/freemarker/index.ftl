<@ie.page "Home">

<h1>Text Repository</h1>

<div style="font-size: larger; text-align: center; margin: 2em; padding-top: 6em">

    <div id="error-message" style="color: #FF3366; margin-bottom: 0.5em">&nbsp;</div>

    <form id="goto-text-form">
        <label for="text-id">Text&nbsp;#&nbsp;</label>
        <input type="text" name="text" id="text-id" style="width: 10em" />
        <input type="submit" value="Go">
    </form>
</div>

<script type="text/javascript">
    YUI().use("node", function(Y) {
        Y.on("domready", function() {
            Y.one("#text-id").focus();

            Y.one("#goto-text-form").on("submit", function(e) {
                e.preventDefault();
                var errorMessage = Y.one("#error-message");
                var textIdInput = Y.one("#text-id");
                var textId = textIdInput.get("value");

                if (/^[0-9]+$/.test(textId)) {
                    errorMessage.setContent("&nbsp;");
                    textIdInput.set("value", "");
                    window.location.pathname = (cp + "/text/" + textId);
                } else {
                    errorMessage.setContent("Please enter a number");
                }
            });
        })
    });
</script>

</@ie.page>