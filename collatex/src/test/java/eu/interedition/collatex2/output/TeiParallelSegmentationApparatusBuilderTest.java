/**
 * CollateX - a Java library for collating textual sources,
 * for example, to produce an apparatus.
 *
 * Copyright (C) 2010 ESF COST Action "Interedition".
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.collatex2.output;

import static org.junit.Assert.assertTrue;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import org.junit.Before;
import org.junit.Test;

import eu.interedition.collatex2.implementation.CollateXEngine;

public class TeiParallelSegmentationApparatusBuilderTest {
	private static CollateXEngine engine = new CollateXEngine();
	private static DocumentBuilderFactory dbf = DocumentBuilderFactory
			.newInstance();
	private static TransformerFactory tf = TransformerFactory.newInstance();
	private DocumentBuilder documentBuilder;
	private Transformer transformer;

	@Before
	public void setUp() throws Exception {
		documentBuilder = dbf.newDocumentBuilder();
		transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.setOutputProperty(OutputKeys.INDENT, "no");
	}

	@Test
	public void dummy() {
		assertTrue(true);
	}

	// /**
	// * The first example from #6
	// * (http://arts-itsee.bham.ac.uk/trac/interedition/ticket/6) (without
	// witness
	// * C for now)
	// *
	// * @throws Exception
	// */
	// @Test
	// public void testSimpleSubstitutionOutput() throws Exception {
	// assertApparatusEquals(//
	// "the black <app><rdg wit=\"#W1\">cat</rdg><rdg wit=\"#W2 #W3\">dog</rdg></app> and the black mat",//
	// "the black cat and the black mat",//
	// "the black dog and the black mat",//
	// "the black dog and the black mat");
	// }

	// /**
	// * Second example from #6. Tests addition, deletion and multiple words in
	// one
	// * variant
	// * @throws Exception
	// */
	// @Test
	// public void testSimpleAddDelOutput() throws Exception {
	// assertApparatusEquals(//
	// "the black <app><rdg wit=\"#W1\"/><rdg wit=\"#W2 #W3\">saw the black</rdg></app> cat on the <app><rdg wit=\"#W1\">white</rdg><rdg wit=\"#W2 #W3\"/></app> table",//
	// "the black cat on the white table",//
	// "the black saw the black cat on the table",//
	// "the black saw the black cat on the table");
	// }

	// @Test
	// public void testMultiSubstitutionOutput() throws Exception {
	// assertApparatusEquals(//
	// "the <app><rdg wit=\"#W1\">black cat</rdg><rdg wit=\"#W2 #W3\">big white dog</rdg></app> and the black mat",//
	// "the black cat and the black mat",//
	// "the big white dog and the black mat",//
	// "the big white dog and the black mat");
	// }

	// // Additional unit tests (not present in ticket #6)
	// @Test
	// public void testAllWitnessesEqual() throws Exception {
	// assertApparatusEquals("the black cat",//
	// "the black cat",//
	// "the black cat",//
	// "the black cat");
	// }

	// // Note: There are some problems with whitespace here!
	// @Test
	// public void testAWordMissingAtTheEnd() throws Exception {
	// assertApparatusEquals(//
	// "the black <app><rdg wit=\"#W1 #W2\">cat</rdg><rdg wit=\"#W3\"/></app>",//
	// "the black cat",//
	// "the black cat",//
	// "the black");
	// }

	// // Note: There might be some problems with whitespace here!
	// @Test
	// public void testCrossVariation() throws Exception {
	// assertApparatusEquals(//
	// "the <app><rdg wit=\"#W1\"/><rdg wit=\"#W2 #W3\">white</rdg></app> <app><rdg wit=\"#W1 #W3\"/><rdg wit=\"#W2\">and</rdg></app> <app><rdg wit=\"#W1 #W2\">black</rdg><rdg wit=\"#W3\"/></app> cat",//
	// "the black cat",//
	// "the white and black cat",//
	// "the white cat");
	// }

	// // Note: There might be some problems with whitespace here!
	// @Test
	// public void testAddition() throws Exception {
	// assertApparatusEquals(//
	// "the <app><rdg wit=\"#W1\"/><rdg wit=\"#W2\">white and</rdg></app> black cat",//
	// "the black cat",//
	// "the white and black cat");
	// }

	// private void assertApparatusEquals(String apparatusStr, String...
	// witnesses) throws Exception {
	// IAligner aligner = engine.createAligner();
	// int wc = 0;
	// for (String witness : witnesses) {
	// aligner.add(engine.createWitness("W" + (++wc), witness));
	// }
	// Document xml = documentBuilder.newDocument();
	// Element root =
	// xml.createElementNS(TeiParallelSegmentationApparatusBuilder.TEI_NS,
	// "text");
	// xml.appendChild(root);
	//
	// TeiParallelSegmentationApparatusBuilder.build(engine.createApparatus(aligner.getResult()),
	// root);
	// StringWriter out = new StringWriter();
	// transformer.transform(new DOMSource(xml), new StreamResult(out));
	// String result = out.toString();
	// result =
	// result.substring("<text xmlns=\"http://www.tei-c.org/ns/1.0\">".length());
	// result = result.substring(0, result.length() - "</text>".length());
	// Assert.assertEquals(apparatusStr, result);
	// }
	// TODO: reenable test!
	// @Test
	// public void testNearMatches() {
	// Witness w1 = builder.build("A", "the black cat");
	// Witness w2 = builder.build("B", "the blak cat");
	// Witness w3 = builder.build("C", "the black cat");
	// WitnessSet set = new WitnessSet(w1, w2, w3);
	// AlignmentTable2 table = set.createAlignmentTable();
	// String expected = "<collation>the black cat</collation>";
	// Assert.assertEquals(expected, table.toXML());
	// }

}
