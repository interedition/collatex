package de.tud.kom.stringutils.tests;

import org.junit.Test;

import de.tud.kom.stringutils.tokenization.XMLWordTokenizer;
import static org.junit.Assert.*;

public class TestXMLTokenizer {

	@Test
	public void testTokenStringLength(){
		String testString = "Lorem ipsum dolor<br class=\"longbreak\"/> sit <i>amet</i> consectetur <a class=\"a b and c\" style=\"background-color: #aaa\">adipiscing</a> elit.";
		
		XMLWordTokenizer tokenizer = new XMLWordTokenizer();
		String[] tokens = tokenizer.tokenize(testString);
		
		assertEquals(13, tokens.length);
		assertEquals("<br class=\"longbreak\"/>", tokens[3]);
	}
}
