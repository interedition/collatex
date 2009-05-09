function showSecondary() {
  var s = $$('.secondary');
  for ( var i = 0; i < s.length; i++) {
    s[i].show();
  }
  Form.Element.disable('showButton');
  Form.Element.enable('hideButton');
}

function hideSecondary() {
  var s = $$('.secondary');
  for ( var i = 0; i < s.length; i++) {
    s[i].hide();
  }
  Form.Element.disable('hideButton');
  Form.Element.enable('showButton');
}

function toggleColored() {
  var s = $$('.colored');
  for ( var i = 0; i < s.length; i++) {
    s[i].toggle();
  }
}

function toggleVariance() {
	  var s = $$('.variance');
	  for ( var i = 0; i < s.length; i++) {
	    s[i].toggle();
	  }
	}