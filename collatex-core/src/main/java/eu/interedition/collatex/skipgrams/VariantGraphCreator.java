package eu.interedition.collatex.skipgrams;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.simple.SimpleToken;

import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/*
 * @authro: Ronald Haentjens Dekker
 * Date; 25-10-2018
 */
/*
 * We use a skiplist per witness
 * In each of the index position we put a Node (of the variant graph)
 * We should be able to detect transpositions that way but that is for now not yet the point.
 *
 * Op deze manier krijgen we de tokens binnen een witness op een rijtje
 * Maar zitten we nog met dubbelen tussen de witnessrs
 * We kunnrn wel trnaspositeis ontdekkeen.
 * Ik kan natuurlijk in een aparate map opslaan dat die nodes bij elkaar horen..
 * Maar het voelt vreemd als ik toch al weet dat voorkoemns van dezelfde genormaliseerde versie bij
 * elkaar horen.
 * Mijn eerste stelregerl zou kunnn zijn dat we een set van nodes ana maken per normalized skipgram
 * Dat gaat natuurlijk niet op voor repeats.
 */
public class VariantGraphCreator {
    private Map<String, NavigableMap<Token, VariantGraph.Vertex>> theMap = new HashMap<>();
    VariantGraph variantgraph = new VariantGraph();



    public void selectSkipgram(String witnessId, Skipgram skipgram, VariantGraph.Vertex vertexForHead, VariantGraph.Vertex vertexForTail) {
        NavigableMap<Token, VariantGraph.Vertex> tokenVertexNavigableMap = theMap.getOrDefault(witnessId, new TreeMap());

        // first place the head token in the map
        SimpleToken head  = (SimpleToken) skipgram.head;
        vertexForHead.tokens().add(head);
        tokenVertexNavigableMap.put(head, vertexForHead);

        // then place the tail token in the map
        SimpleToken tail = (SimpleToken) skipgram.tail;
        vertexForTail.tokens().add(tail);
        tokenVertexNavigableMap.put(tail, vertexForTail);
    }

    public VariantGraph.Vertex createVertexForToken() {
        VariantGraph.Vertex vertex = new VariantGraph.Vertex(variantgraph);
        return vertex;
    }


    // We doen een nieuwe poging...
    //    private Map<String, NavigableMap<Integer, VariantGraph.Vertex>> theMap;
//    private ConcurrentSkipListMap<Integer, VariantGraph.Vertex> theMap;

    // after selecting a normalez d skipgram we create a node for it an put in there..
    // misschien maak ik het te moeilijk ... a
    // Misschien moet ik gewoon beginnen met de normalized form

    // eerst de witness id... dan gaan we op zoek naar iets wat daar staate
    // I don't even..


    // Ai; Met een skiplist per witness 1 zitten mrt ht probleem dat we niet een nieuw node
    // kunnen aanmaken per token
    // Er zit hier iets mis.

//    public void selectSkipgram(String w1, Skipgram result) {
//        // We halen de skiplist
//        NavigableMap<Integer, VariantGraph.Vertex> theSKiplist = theMap.get(w1);
//        // aargh nu heb ik de token positie binnen een witness nodig van de eerste token van de skipgram.
//        SimpleToken head  = (SimpleToken) result.head;
//        head.
//        theSKiplist.floorEntry()
//    }
}
