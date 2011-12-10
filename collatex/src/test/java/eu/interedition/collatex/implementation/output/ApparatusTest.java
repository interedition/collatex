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

package eu.interedition.collatex.implementation.output;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.interfaces.IWitness;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

public class ApparatusTest extends AbstractTest {
  private DocumentBuilder documentBuilder;
  private Transformer transformer;

  @Before
  public void initXMLInfrastructure() throws Exception {
    documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    transformer = TransformerFactory.newInstance().newTransformer();
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    transformer.setOutputProperty(OutputKeys.INDENT, "no");
  }

  private void assertApparatusEquals(String apparatusStr, String... witnessContents) throws Exception {
    final IWitness[] witnesses = createWitnesses(witnessContents);

    final Document xml = documentBuilder.newDocument();
    final Element root = xml.createElementNS(Apparatus.TEI_NS, "text");
    xml.appendChild(root);

    merge(witnesses).toApparatus().serialize(root);
    StringWriter out = new StringWriter();
    transformer.transform(new DOMSource(xml), new StreamResult(out));
    final String result = out.toString();
    assertEquals(apparatusStr, result.substring("<text xmlns=\"http://www.tei-c.org/ns/1.0\">".length(), result.length() - "</text>".length()));
  }

  /**
   * The first example from #6
   * (http://arts-itsee.bham.ac.uk/trac/interedition/ticket/6) (without
   * witness
   * C for now)
   *
   * @throws Exception
   */
  @Test
  public void testSimpleSubstitutionOutput() throws Exception {
    assertApparatusEquals(//
        "the black <app><rdg wit=\"#A\">cat</rdg><rdg wit=\"#B #C\">dog</rdg></app> and the black mat",//
        "the black cat and the black mat",//
        "the black dog and the black mat",//
        "the black dog and the black mat");
  }

  /**
   * Second example from #6. Tests addition, deletion and multiple words in
   * one
   * variant
   *
   * @throws Exception
   */
  @Test
  public void testSimpleAddDelOutput() throws Exception {
    assertApparatusEquals(//
        "the black <app><rdg wit=\"#A\"/><rdg wit=\"#B #C\">saw the black</rdg></app> cat on the <app><rdg wit=\"#A\">white</rdg><rdg wit=\"#B #C\"/></app> table",//
        "the black cat on the white table",//
        "the black saw the black cat on the table",//
        "the black saw the black cat on the table");
  }

  @Test
  public void testMultiSubstitutionOutput() throws Exception {
    assertApparatusEquals(//
        "the <app><rdg wit=\"#A\">black cat</rdg><rdg wit=\"#B #C\">big white dog</rdg></app> and the black mat",//
        "the black cat and the black mat",//
        "the big white dog and the black mat",//
        "the big white dog and the black mat");
  }

  // Additional unit tests (not present in ticket #6)
  @Test
  public void testAllWitnessesEqual() throws Exception {
    assertApparatusEquals("the black cat",//
        "the black cat",//
        "the black cat",//
        "the black cat");
  }

  // Note: There are some problems with whitespace here!
  @Test
  public void testAWordMissingAtTheEnd() throws Exception {
    assertApparatusEquals(//
        "the black <app><rdg wit=\"#A #B\">cat</rdg><rdg wit=\"#C\"/></app>",//
        "the black cat",//
        "the black cat",//
        "the black");
  }

  // Note: There might be some problems with whitespace here!
  @Test
  public void testCrossVariation() throws Exception {
    assertApparatusEquals(//
        "the <app><rdg wit=\"#A\"/><rdg wit=\"#B #C\">white</rdg></app> <app><rdg wit=\"#A #C\"/><rdg wit=\"#B\">and</rdg></app> <app><rdg wit=\"#A #B\">black</rdg><rdg wit=\"#C\"/></app> cat",//
        "the black cat",//
        "the white and black cat",//
        "the white cat");
  }

  // Note: There might be some problems with whitespace here!
  @Test
  public void testAddition() throws Exception {
    assertApparatusEquals(//
        "the <app><rdg wit=\"#A\"/><rdg wit=\"#B\">white and</rdg></app> black cat",//
        "the black cat",//
        "the white and black cat");
  }
}
