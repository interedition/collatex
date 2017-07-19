var testCollateX = {
  "witnesses": [
    {"id":"W1", "content":"In windows, a new line is: \\\\r\\\\n:\r\n.Lorem ipsum."},
    {"id":"W2", "content":"In linux, a new line is: \\\\n:\n.Lorem ipsum."},
    {"id":"W3", "content":"In mac, a new line is: \\\\r:\r.Lorem ipsum."}
  ],
  "algorithm":"dekker",
  "tokenComparator":
    {"type":"equality"},
  "joined":true,
  "transpositions":true
};

function test(){
  $.ajax({
    url: 'https://collatex.net/demo/collate',
    type: 'POST',
    data: JSON.stringify(testCollateX),
    accepts:
      { svg: "image/svg+xml" },
    converters:
      { "text svg": jQuery.parseXML },
    contentType: 'application/json; charset=utf-8',
    dataType: 'svg'
  })
  .done(function( data ) {
    if ( console && console.log ) {
      console.log( "Sample of data:", data );
      xml = new XMLSerializer().serializeToString(data)
      $('#result').html(xml);
    }
  })
  .fail(function( jqXHR, textStatus, errorThrown)
    { alert( "error" + errorThrown ); }
  );
}