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
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilderFactory;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.IWitness;
import eu.interedition.collatex2.interfaces.IVariantGraph;

public class GraphMLOutputTest {
	private static CollateXEngine engine = new CollateXEngine();

	@Test
	public void testSimpleGraphML() throws Exception {
		final IWitness witA = engine.createWitness( "A", "the black cat");
		final IWitness witB = engine.createWitness( "B", "the black and white cat");
		final IVariantGraph graph = engine.graph( witA, witB );
		Document graphXML = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		GraphMLBuilder.build(graph, graphXML);
		// Check that we got a GraphML file.
		String docElementName = "graphml";
		Assert.assertEquals(graphXML.getDocumentElement().getTagName(), docElementName);
		// Check that the graphML has seven node elements and seven edge elements.
		NodeList vertexElements = graphXML.getElementsByTagName( "node" );
		NodeList edgeElements = graphXML.getElementsByTagName( "edge" );
		Assert.assertEquals(vertexElements.getLength(), 7);
		Assert.assertEquals(edgeElements.getLength(), 7);
		
    //assertTrue(cyclicVariantGraph.edgeSet().isEmpty());
    //assertEquals(2, cyclicVariantGraph.vertexSet().size());
	}
}