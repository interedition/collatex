<@ie.page "Upload text">
<h1>Upload text</h1>

<div id="error-message" class="error center" style="margin: 5em 0.5em 0 0"><br></div>

<form id="upload" name="upload" method="post" enctype="multipart/form-data">
    <table style="margin: 0 auto">
        <tr>
            <th class="right"><label for="file">File:</label></th>
            <td><input type="file" name="file" id="file"/></td>
        </tr>
        <tr>
            <th class="right"><label for="fileType">Type:</label></th>
            <td><select id="fileType" name="fileType">
                <option value="XML" selected="selected">XML</option>
                <option value="PLAIN">Plain Text</option>
            </select></td>
        </tr>
        <tr>
            <th class="right"><label for="fileEncoding">Encoding:</label></th>
            <td><select id="fileEncoding" name="fileEncoding" disabled="disabled">
                <option value="UTF-8">UTF-8</option>
                <option value="ISO-8859-1">ISO-8859-1</option>
                <option value="UTF-16">UTF-16</option>
            </select></td>
        </tr>
        <tr>
            <td colspan="2" class="center"><input type="submit" value="Upload"/></td>
        </tr>
    </table>
</form>

<script type="text/javascript">
    YUI().use("node", "event", function(Y) {
        Y.on("domready", function() {
            var file = Y.one("#file");
            var fileType = Y.one("#fileType");
            var fileEncoding = Y.one("#fileEncoding");

            Y.on("change", function() {
                fileEncoding.set("disabled", this.get("value") == "XML");
            }, fileType);

            Y.on("change",function() {
                if (this.get("value").match(/\.xml$/i)) {
                    fileType.set("value", "XML");
                    fileEncoding.set("disabled", true);
                } else {
                    fileType.set("value", "PLAIN");
                    fileEncoding.set("disabled", false);
                }
            }, file);

            Y.on("submit", function(e) {
                var errorMessage = Y.one("#error-message");
                var fileValue = file.get("value");
                if (fileValue == null || fileValue.trim().length == 0) {
                    errorMessage.setContent("Please enter a file to upload!");
                    e.preventDefault();
                } else {
                    errorMessage.setContent("<br>");
                }
            }, "#upload");
        });
    });
</script>
</@ie.page>