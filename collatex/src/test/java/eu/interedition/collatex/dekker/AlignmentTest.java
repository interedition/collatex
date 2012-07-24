/**
 * CollateX - a Java library for collating textual sources, for example, to
 * produce an apparatus.
 *
 * Copyright (C) 2010 ESF COST Action "Interedition".
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.interedition.collatex.dekker;

import static eu.interedition.collatex.dekker.Match.PHRASE_MATCH_TO_TOKENS;
import static org.junit.Assert.assertEquals;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.junit.Test;

import com.google.common.collect.RowSortedTable;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.CollationAlgorithmFactory;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import eu.interedition.collatex.simple.SimpleToken;
import eu.interedition.collatex.simple.SimpleVariantGraphSerializer;
import eu.interedition.collatex.simple.SimpleWitness;
import eu.interedition.collatex.simple.WhitespaceAndPunctuationTokenizer;

public class AlignmentTest extends AbstractTest {

  @Test
  public void transposition() {
    final SimpleWitness[] w = createWitnesses("the cat is black", "black is the cat");
    final RowSortedTable<Integer, Witness, Set<Token>> t = collate(w).toTable();
    assertEquals("|the|cat|is|black| |", toString(t, w[0]));
    assertEquals("|black| |is|the|cat|", toString(t, w[1]));
  }

  @Test
  public void doubleTransposition2() {
    final SimpleWitness[] w = createWitnesses("a b", "b a");
    final RowSortedTable<Integer, Witness, Set<Token>> t = collate(w).toTable();
    assertEquals("| |a|b|", toString(t, w[0]));
    assertEquals("|b|a| |", toString(t, w[1]));
  }

  @Test
  public void doubleTransposition3() {
    final SimpleWitness[] w = createWitnesses("a b c", "b a c");
    final RowSortedTable<Integer, Witness, Set<Token>> t = collate(w).toTable();
    assertEquals("| |a|b|c|", toString(t, w[0]));
    assertEquals("|b|a| |c|", toString(t, w[1]));
  }

  @Test
  public void additionInCombinationWithTransposition() {
    final SimpleWitness[] w = createWitnesses(//
        "the cat is very happy",//
        "very happy is the cat",//
        "very delitied and happy is the cat");
    final RowSortedTable<Integer, Witness, Set<Token>> t = collate(w).toTable();
    assertEquals("|the|cat| | |is|very|happy|", toString(t, w[0]));
    assertEquals("|very| | |happy|is|the|cat|", toString(t, w[1]));
    assertEquals("|very|delitied|and|happy|is|the|cat|", toString(t, w[2]));
  }

  @Test
  public void additionInCombinationWithTransposition2() {
    final SimpleWitness[] w = createWitnesses(//
        "the cat is black",//
        "black is the cat",//
        "black and white is the cat");
    final RowSortedTable<Integer, Witness, Set<Token>> t = collate(w).toTable();
    assertEquals("|the|cat| |is|black| |", toString(t, w[0]));
    assertEquals("|black| | |is|the|cat|", toString(t, w[1]));
    assertEquals("|black|and|white|is|the|cat|", toString(t, w[2]));
  }

  @Test
  public void simpleTransposition() {
    final SimpleWitness[] w = createWitnesses(//
        "A black cat in a white basket",//
        "A white cat in a black basket");
    final RowSortedTable<Integer, Witness, Set<Token>> t = collate(w).toTable();
    assertEquals("|A|black|cat|in|a|white|basket|", toString(t, w[0]));
    assertEquals("|A|white|cat|in|a|black|basket|", toString(t, w[1]));
  }

  @Test
  public void transposeInOnePair() {
    final SimpleWitness[] w = createWitnesses("y", "x y z", "z y");
    final RowSortedTable<Integer, Witness, Set<Token>> t = collate(w).toTable();
    assertEquals("| |y| |", toString(t, w[0]));
    assertEquals("|x|y|z|", toString(t, w[1]));
    assertEquals("|z|y| |", toString(t, w[2]));
  }

  @Test
  public void transposeInTwoPairs() {
    final SimpleWitness[] w = createWitnesses("y x", "x y z", "z y");
    final RowSortedTable<Integer, Witness, Set<Token>> t = collate(w).toTable();
    assertEquals("| |y|x|", toString(t, w[0]));
    assertEquals("|x|y|z|", toString(t, w[1]));
    assertEquals("|z|y| |", toString(t, w[2]));
  }

  @Test
  public void testOrderIndependence() {
    final SimpleWitness[] w = createWitnesses("Hello cruel world", "Hello nice world", "Hello nice cruel world");
    VariantGraph graph = collate(w[0], w[1]);
    collate(graph, w[2]);
    List<List<Match>> phraseMatches = ((DekkerAlgorithm) collationAlgorithm).getPhraseMatches();
    assertEquals("hello", SimpleToken.toString(PHRASE_MATCH_TO_TOKENS.apply(phraseMatches.get(0))));
    assertEquals("nice", SimpleToken.toString(PHRASE_MATCH_TO_TOKENS.apply(phraseMatches.get(1))));
    assertEquals("cruel", SimpleToken.toString(PHRASE_MATCH_TO_TOKENS.apply(phraseMatches.get(2))));
    assertEquals("world", SimpleToken.toString(PHRASE_MATCH_TO_TOKENS.apply(phraseMatches.get(3))));
    List<List<Match>> transpositions = ((DekkerAlgorithm) collationAlgorithm).getTranspositions();
    assertEquals(0, transpositions.size());
  }

  @Test
  public void testPhraseMatchingShouldNotIgnoreDeletions() {
    final SimpleWitness[] w = createWitnesses("Hello cruel world", "Hello world");
    VariantGraph graph = collate(w);
    List<List<Match>> phraseMatches = ((DekkerAlgorithm) collationAlgorithm).getPhraseMatches();
    assertEquals("hello", SimpleToken.toString(PHRASE_MATCH_TO_TOKENS.apply(phraseMatches.get(0))));
    assertEquals("world", SimpleToken.toString(PHRASE_MATCH_TO_TOKENS.apply(phraseMatches.get(1))));
    List<List<Match>> transpositions = ((DekkerAlgorithm) collationAlgorithm).getTranspositions();
    assertEquals(0, transpositions.size());
  }

  //TODO: check this is still ok!
  @Test
  public void testPhraseMatchingShouldIgnoreAdditions() {
    final SimpleWitness[] w = createWitnesses("Hello world", "Hello cruel world");
    VariantGraph graph = collate(w);
    List<List<Match>> phraseMatches = ((DekkerAlgorithm) collationAlgorithm).getPhraseMatches();
    assertEquals("hello world", SimpleToken.toString(PHRASE_MATCH_TO_TOKENS.apply(phraseMatches.get(0))));
    List<List<Match>> transpositions = ((DekkerAlgorithm) collationAlgorithm).getTranspositions();
    assertEquals(0, transpositions.size());
  }

  @Test
  public void testOrderIndependenceTroy() throws XMLStreamException {
    WhitespaceAndPunctuationTokenizer tokenizer = new WhitespaceAndPunctuationTokenizer();

    List<SimpleWitness> witnesses = new ArrayList<SimpleWitness>();
    witnesses.add(new SimpleWitness("w1", "X A Z ", tokenizer));
    witnesses.add(new SimpleWitness("w2", "Y B Z ", tokenizer));
    witnesses.add(new SimpleWitness("w3", "Y A X ", tokenizer));

    setCollationAlgorithm(CollationAlgorithmFactory.dekker(new EqualityTokenComparator()));
    VariantGraph graph = graphFactory.newVariantGraph();
    collationAlgorithm.collate(graph, witnesses.toArray(new SimpleWitness[0]));

    StringWriter writer = new StringWriter();
    XMLStreamWriter swriter = XMLOutputFactory.newInstance().createXMLStreamWriter(writer);
    SimpleVariantGraphSerializer serializer = new SimpleVariantGraphSerializer(graph);
    serializer.toGraphML(swriter);
  }
}
