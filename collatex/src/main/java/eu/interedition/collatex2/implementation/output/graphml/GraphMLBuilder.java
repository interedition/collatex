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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.google.common.collect.HashBiMap;

import eu.interedition.collatex2.interfaces.IVariantGraph;
import eu.interedition.collatex2.interfaces.IVariantGraphEdge;
import eu.interedition.collatex2.interfaces.IVariantGraphVertex;
import eu.interedition.collatex2.interfaces.IWitness;


public class GraphMLBuilder {

	private static final String WITNESS_ID_PREFIX = "w";
	private static final String NODE_TAG = "node";
	private static final String TARGET_ATT = "target";
	private static final String SOURCE_ATT = "source";
	private static final String EDGE_TAG = "edge";
	private static final String EDGEDEFAULT_DEFAULT_VALUE = "directed";
	private static final String EDGEDEFAULT_ATT = "edgedefault";
	private static final String GRAPH_ID = "0";
	private static final String GRAPH_TAG = "graph";
	private static final String GRAPHML_NS = "http://graphml.graphdrawing.org/xmlns";
	private static final String XMLNS_ATT = "xmlns";
	private static final String GRAPHML_TAG = "graphml";
	private static final String XMLNSXSI_ATT = "xmlns:xsi";
	private static final String XSISL_ATT = "xsi:schemaLocation";
	private static final String GRAPHML_XMLNSXSI = "http://www.w3.org/2001/XMLSchema-instance";
	private static final String GRAPHML_XSISL = "http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd";
	private static final String ATTR_TYPE_ATT = "attr.type";
	private static final String ATTR_NAME_ATT = "attr.name";
	private static final String FOR_ATT = "for";
	private static final String ID_ATT = "id";
	private static final String KEY_TAG = "key";
	private static final String DATA_TAG = "data";

	private enum Keys {
		NODE_TOKEN("d0", NODE_TAG, "token", "string"), NODE_NUMBER("d1",
				NODE_TAG, "number", "int"), NODE_IDENTICAL("d2", NODE_TAG, 
						"identical", "string");

		private String name;
		private String keyId;
		private String forElement;
		private String type;

		private Keys(String id, String forElement, String name, String type) {
			this.name = name;
			this.keyId = id;
			this.forElement = forElement;
			this.type = type;
		}

		public Element getDataElement(String data, Document doc) {
			Element dataElement = doc.createElement(DATA_TAG);
			dataElement.setAttribute(KEY_TAG, keyId);
			dataElement.setTextContent(data);
			return dataElement;
		}

		public Element getKeyElement(Document doc) {
			Element keyElement = doc.createElement(KEY_TAG);
			keyElement.setAttribute(ID_ATT, keyId);
			keyElement.setAttribute(FOR_ATT, forElement);
			keyElement.setAttribute(ATTR_NAME_ATT, name);
			keyElement.setAttribute(ATTR_TYPE_ATT, type);
			return keyElement;
		}
	}

	public static void build(IVariantGraph variantGraph, Document graphXML) {

		// add root element
		Element rootElement = createRootElement(graphXML);
		graphXML.appendChild(rootElement);

		// add key elements for node
		rootElement.appendChild(Keys.NODE_NUMBER.getKeyElement(graphXML));
		rootElement.appendChild(Keys.NODE_TOKEN.getKeyElement(graphXML));
		rootElement.appendChild(Keys.NODE_IDENTICAL.getKeyElement(graphXML));

		// add key elements (witnesses) and prepare mapping of sigil to its
		// graphml id
		Map<String, Integer> sigilToId = new HashMap<String, Integer>();
		int witnessNumber = 0;
		for (IWitness w : variantGraph.getWitnesses()) {
			rootElement.appendChild(createKeyWitnessElementForEdge(w, graphXML,
					WITNESS_ID_PREFIX + witnessNumber));
			sigilToId.put(w.getSigil(), witnessNumber);
			witnessNumber++;
		}

		// add graph element
		Element graph = graphXML.createElement(GRAPH_TAG);
		graph.setAttribute(ID_ATT, GRAPH_ID);
		graph.setAttribute(EDGEDEFAULT_ATT, EDGEDEFAULT_DEFAULT_VALUE);

		rootElement.appendChild(graph);

		// get nodes and edges from variant graph
		List<Element> nodes = new ArrayList<Element>();
		List<Element> edges = new ArrayList<Element>();

		// TODO is it possible to improve the performance by not using this map?
		HashBiMap<IVariantGraphVertex, String> vertexToId = HashBiMap.create();
		Map<IVariantGraphVertex, IVariantGraphVertex> transpositions = variantGraph.getTransposedTokens();
		
		Iterator<IVariantGraphVertex> vertexIterator = variantGraph.iterator();
		int vertexNumber = 0;
		while (vertexIterator.hasNext()) {
			String vertexNodeID = "n" + vertexNumber;
			IVariantGraphVertex vertex = vertexIterator.next();
			String token = vertex.getNormalized();
			nodes.add(createNodeElement(vertexNodeID, token, graphXML));
			vertexToId.put(vertex, vertexNodeID);
			// Do we need to keep track of a transposed node to add later?
			edges.addAll(createEdgeElements(
					variantGraph.incomingEdgesOf(vertex), vertexToId,
					sigilToId, variantGraph, graphXML, edges.size()));
			vertexNumber++;
		}
		
		// add nodes
		for (Element n : nodes) {
			// See if we need to add an 'identical' tag for a transposition.
			// This is best done here, after all the nodes have been created.
			IVariantGraphVertex vertex = vertexToId.inverse().get(n.getAttribute(ID_ATT));
			if(transpositions.containsKey(vertex)) {
				String txpID = vertexToId.get(transpositions.get(vertex));
				n.appendChild(Keys.NODE_IDENTICAL.getDataElement(txpID, graphXML));
			}
			graph.appendChild(n);
		}

		// add edges
		for (Element e : edges) {
			graph.appendChild(e);
		}
	}

	private static Element createRootElement(Document graphXML) {
		Element rootElement = graphXML.createElement(GRAPHML_TAG);
		rootElement.setAttribute(XMLNS_ATT, GRAPHML_NS);
		rootElement.setAttribute(XMLNSXSI_ATT, GRAPHML_XMLNSXSI);
		rootElement.setAttribute(XSISL_ATT, GRAPHML_XSISL);
		return rootElement;
	}

	private static Element createKeyWitnessElementForEdge(IWitness w,
			Document doc, String id) {
		Element keyElement = doc.createElement(KEY_TAG);
		keyElement.setAttribute(ID_ATT, id);
		keyElement.setAttribute(FOR_ATT, EDGE_TAG);
		keyElement.setAttribute(ATTR_NAME_ATT, w.getSigil());
		keyElement.setAttribute(ATTR_TYPE_ATT, "string");
		return keyElement;
	}

	private static List<? extends Element> createEdgeElements(
			Set<IVariantGraphEdge> incomingEdges,
			HashBiMap<IVariantGraphVertex, String> vertexToID,
			Map<String, Integer> witnessToNumber, IVariantGraph variantGraph,
			Document graphXML, Integer nextEdgeNumber) {

		List<Element> edges = new ArrayList<Element>();

		int counter = nextEdgeNumber;
		for (IVariantGraphEdge edge : incomingEdges) {
			IVariantGraphVertex source = variantGraph.getEdgeSource(edge);
			IVariantGraphVertex target = variantGraph.getEdgeTarget(edge);

			Element edgeElement = createEdgeElement(source, target,
					vertexToID, graphXML, counter++);

			// append information about witnesses to the edge
			for (IWitness w : edge.getWitnesses()) {
				edgeElement.appendChild(createDataElement(w.getSigil(),
						WITNESS_ID_PREFIX + witnessToNumber.get(w.getSigil()),
						graphXML));
			}

			edges.add(edgeElement);
		}
		return edges;
	}

	private static Element createDataElement(String data, String id,
			Document doc) {
		Element dataElement = doc.createElement(DATA_TAG);
		dataElement.setAttribute(KEY_TAG, id);
		dataElement.setTextContent(data);
		return dataElement;
	}

	private static Element createEdgeElement(IVariantGraphVertex source,
			IVariantGraphVertex target,
			HashBiMap<IVariantGraphVertex, String> vertexToID, Document doc,
			Integer edgeNumber) {

		Element edge = doc.createElement(EDGE_TAG);
		edge.setAttribute(ID_ATT, edgeNumber.toString());
		edge.setAttribute(SOURCE_ATT, vertexToID.get(source));
		edge.setAttribute(TARGET_ATT, vertexToID.get(target));

		return edge;
	}

	private static Element createNodeElement(String vertexID, String token, Document doc) {
		Element node = doc.createElement(NODE_TAG);
		node.setAttribute(ID_ATT, vertexID);
		node.appendChild(Keys.NODE_TOKEN.getDataElement(token, doc));
		node.appendChild(Keys.NODE_NUMBER.getDataElement(vertexID, doc));
		return node;
	}

}
