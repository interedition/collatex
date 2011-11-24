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

package eu.interedition.collatex2.implementation.output.graphml;

import eu.interedition.collatex2.AbstractTest;
import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IWitness;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import static org.junit.Assert.assertEquals;

public class GraphMLOutputTest extends AbstractTest {

  private IWitness[] witnesses;
  private IVariantGraph graph;
  private Document graphXML;
  private XPath xpath;

  @Before
  public void setup() throws Exception {
    xpath = XPathFactory.newInstance().newXPath();
    witnesses = createWitnesses("the black cat", "the black and white cat", "the white and black cat");
    graph = merge(witnesses);
    graphXML = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    GraphMLBuilder.build(graph, graphXML);
  }

  @Test
  public void document() throws Exception {
    // Check that we got a GraphML file.
    assertEquals("graphml", graphXML.getDocumentElement().getTagName());

    // Get the key for the token information, for later tests, now that
    // we have ascertained that the GraphML file is one.
    assertEquals("d0", xpath.evaluate("/graphml/key[@attr.name='token']/attribute::id", graphXML));
  }

  @Test
  public void elements() throws Exception {
    final NodeList vertexElements = graphXML.getElementsByTagName("node");
    final NodeList edgeElements = graphXML.getElementsByTagName("edge");
    assertEquals(9, vertexElements.getLength());
    assertEquals(11, edgeElements.getLength());
  }

  @Test
  public void connections() throws Exception {
    final NodeList blackNodes = (NodeList) xpath.evaluate("//node[data[@key='d0']='black']", graphXML, XPathConstants.NODESET);
    assertEquals(blackNodes.getLength(), 2);

    final Element firstBlackNode = (Element) blackNodes.item(0);
    final String firstBlackNodeID = firstBlackNode.getAttribute("id");
    final NodeList edgesOut = (NodeList) xpath.evaluate("//edge[@source='" + firstBlackNodeID + "']", graphXML, XPathConstants.NODESET);
    assertEquals(edgesOut.getLength(), 2);
  }

  @Test
  public void transpositions() throws Exception {
    final String transpositionKey = xpath.evaluate("/graphml/key[@attr.name='identical']/attribute::id", graphXML);
    final NodeList transposedNodes = (NodeList) xpath.evaluate("//node[data[@key='" + transpositionKey + "']]", graphXML, XPathConstants.NODESET);
    assertEquals(2, transposedNodes.getLength());

    // There should be a 'white' that is transposed, and a 'black' that is transposed.
    // For each node in the 'transposed' list, check that its token matches the token of its target.
    for (int i = 0; i < transposedNodes.getLength(); i = i + 1) {
      final Element thisNode = (Element) transposedNodes.item(i);
      final String myToken = xpath.evaluate("./data[@key='d0']/child::text()", thisNode);
      final String transNodeID = xpath.evaluate("./data[@key='" + transpositionKey + "']/child::text()", thisNode);
      final String otherToken = xpath.evaluate("//node[@id='" + transNodeID + "']/data[@key='d0']/child::text()", graphXML);
      assertEquals(myToken, otherToken);
    }
  }
}