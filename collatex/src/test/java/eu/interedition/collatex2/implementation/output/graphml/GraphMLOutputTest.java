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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.*;
import javax.xml.xpath.*;
import javax.xml.parsers.DocumentBuilderFactory;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.IWitness;
import eu.interedition.collatex2.interfaces.IVariantGraph;

public class GraphMLOutputTest {
	private static CollateXEngine engine;
	private static IWitness witA;
	private static IWitness witB;
	private static IWitness witC;
	private static IVariantGraph graph;
	private static Document graphXML;
	
	private static XPath xpath;
	private static String tokenKey;
	
	@Before
	public void setup() throws Exception {
		engine = new CollateXEngine();
		witA = engine.createWitness( "A", "the black cat");
		witB = engine.createWitness( "B", "the black and white cat");
		witC = engine.createWitness("C", "the white and black cat");
		graph = engine.graph( witA, witB, witC );
		graphXML = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		GraphMLBuilder.build(graph, graphXML);
		XPathFactory factory = XPathFactory.newInstance();
		xpath = factory.newXPath();
	}

	@Test
	public void testDocument() throws Exception {
		// Check that we got a GraphML file.
		String docElementName = "graphml";
		Assert.assertEquals(docElementName, graphXML.getDocumentElement().getTagName());
		
		// Get the key for the token information, for later tests, now that
		// we have ascertained that the GraphML file is one.
		tokenKey = xpath.evaluate("/graphml/key[@attr.name='token']/attribute::id", graphXML);
		Assert.assertEquals("d0", tokenKey);
		
		// Now do some more exciting tests.
	}
	
	@Test
	public void testElements() throws Exception {
		// Check that the graphML has seven node elements and seven edge elements.
		NodeList vertexElements = graphXML.getElementsByTagName( "node" );
		NodeList edgeElements = graphXML.getElementsByTagName( "edge" );
		Assert.assertEquals(9, vertexElements.getLength());
		Assert.assertEquals(11, edgeElements.getLength());			
	}
	
	@Test
	public void testConnections() throws Exception {
		Object result = xpath.evaluate("//node[data[@key='"+tokenKey+"']='black']", graphXML, XPathConstants.NODESET);
		NodeList blackNodes = (NodeList) result;
		Assert.assertEquals(blackNodes.getLength(), 2);
		Element firstBlackNode = (Element) blackNodes.item(0);
		String firstBlackNodeID = firstBlackNode.getAttribute("id");
		result = xpath.evaluate("//edge[@source='"+firstBlackNodeID+"']", graphXML, XPathConstants.NODESET);
		NodeList edgesOut = (NodeList) result;
		Assert.assertEquals(edgesOut.getLength(), 2);
	}
	
	@Test
	public void testTranspositions() throws Exception {
		String transpositionKey = xpath.evaluate("/graphml/key[@attr.name='identical']/attribute::id", graphXML);
		Object result = xpath.evaluate("//node[data[@key='"+transpositionKey+"']]", graphXML, XPathConstants.NODESET);
		NodeList transposedNodes = (NodeList) result;
		Assert.assertEquals(2, transposedNodes.getLength());
		// There should be a 'white' that is transposed, and a 'black' that is transposed.
		// For each node in the 'transposed' list, check that its token matches the token of its target.
		for(int i = 0; i < transposedNodes.getLength(); i=i+1) {
			Element thisNode = (Element) transposedNodes.item(i);
			String myToken = xpath.evaluate("./data[@key='"+tokenKey+"']/child::text()", thisNode);
			String transNodeID = xpath.evaluate("./data[@key='"+transpositionKey+"']/child::text()", thisNode);
			String otherToken = xpath.evaluate("//node[@id='"+transNodeID+"']/data[@key='"+tokenKey+"']/child::text()", graphXML);
			Assert.assertEquals(myToken, otherToken);
		}
	}
}