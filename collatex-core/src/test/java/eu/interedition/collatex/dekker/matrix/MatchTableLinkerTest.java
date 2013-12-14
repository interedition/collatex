/*
 * Copyright (c) 2013 The Interedition Development Group.
 *
 * This file is part of CollateX.
 *
 * CollateX is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CollateX is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CollateX.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.collatex.dekker.matrix;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javax.xml.stream.XMLStreamException;

import org.junit.Test;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import eu.interedition.collatex.AbstractTest;
import eu.interedition.collatex.CollationAlgorithmFactory;
import eu.interedition.collatex.Token;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.dekker.Match;
import eu.interedition.collatex.dekker.PhraseMatchDetector;
import eu.interedition.collatex.matching.EqualityTokenComparator;
import eu.interedition.collatex.matching.StrictEqualityTokenComparator;
import eu.interedition.collatex.simple.SimpleWitness;

public class MatchTableLinkerTest extends AbstractTest {

  @Test
  public void additionInCombinationWithTransposition2() {
    final SimpleWitness[] w = createWitnesses(//
        "the cat is black",//
        "black is the cat",//
        "black and white is the cat");
    final VariantGraph graph = collate(w[0], w[1]);
    MatchTableLinker linker = new MatchTableLinker(3);
    Map<Token, VariantGraph.Vertex> link = linker.link(graph, w[2], new EqualityTokenComparator());
    Set<Token> tokens = link.keySet();
    Map<String, String> tokensAsString = Maps.newHashMap();
    for (Token token : tokens) {
      tokensAsString.put(token.toString(), link.get(token).toString());
    }
    assertEquals("[B:0:'black']", tokensAsString.get("C:0:'black'"));
  }  
	
  @Test
  public void testUsecase1() {
    final SimpleWitness[] w = createWitnesses("The black cat", "The black and white cat");
    final VariantGraph graph = collate(w[0]);
    MatchTableLinker linker = new MatchTableLinker(3);
    Map<Token, VariantGraph.Vertex> link = linker.link(graph, w[1], new EqualityTokenComparator());
    assertEquals(3, link.size());
  }

  @Test
  public void testGapsEverythingEqual() {
    // All the witness are equal
    // There are choices to be made however, since there is duplication of tokens
    // Optimal alignment has no gaps
    final SimpleWitness[] w = createWitnesses("The red cat and the black cat", "The red cat and the black cat");
    final VariantGraph graph = collate(w[0]);
    MatchTableLinker linker = new MatchTableLinker(3);
    Map<Token, VariantGraph.Vertex> link = linker.link(graph, w[1], new EqualityTokenComparator());
    PhraseMatchDetector detector = new PhraseMatchDetector();
    List<List<Match>> phraseMatches = detector.detect(link, graph, w[1]);
    assertEquals(1, phraseMatches.size());
  }

  @Test
  public void testGapsOmission() {
    // There is an omission
    // Optimal alignment has 1 gap
    // Note: there are two paths here that contain 1 gap
    final SimpleWitness[] w = createWitnesses("The red cat and the black cat", "the black cat");
    final VariantGraph graph = collate(w[0]);
    MatchTableLinker linker = new MatchTableLinker(3);
    Map<Token, VariantGraph.Vertex> link = linker.link(graph, w[1], new EqualityTokenComparator());
    PhraseMatchDetector detector = new PhraseMatchDetector();
    List<List<Match>> phraseMatches = detector.detect(link, graph, w[1]);
    assertEquals(1, phraseMatches.size());
  }

  @Test
  //Note: test taken from HermansTest
  public void testHermansText2c() throws XMLStreamException {
    String textD1 = "Op den Atlantischen Oceaan voer een groote stoomer.";
    String textD9 = "Over de Atlantische Oceaan voer een grote stomer.";
    String textDMD1 = "Over de Atlantische Oceaan voer een vreselijk grote stomer.";
    SimpleWitness[] witnesses = createWitnesses(textD1, textD9, textDMD1);

    VariantGraph graph = collate(witnesses[0], witnesses[1]);

    MatchTableLinker linker = new MatchTableLinker(1);
    Map<Token, VariantGraph.Vertex> linkedTokens = linker.link(graph, witnesses[2], new EqualityTokenComparator());

    Set<Token> tokens = linkedTokens.keySet();
    Set<String> tokensAsString = Sets.newLinkedHashSet();
    for (Token token : tokens) {
      tokensAsString.add(token.toString());
    }
    LOG.fine(Iterables.toString(tokensAsString));
    assertTrue(tokensAsString.contains("C:0:'over'"));
    assertTrue(tokensAsString.contains("C:1:'de'"));
    assertTrue(tokensAsString.contains("C:2:'atlantische'"));
    assertTrue(tokensAsString.contains("C:3:'oceaan'"));
    assertTrue(tokensAsString.contains("C:4:'voer'"));
    assertTrue(tokensAsString.contains("C:5:'een'"));
    assertTrue(tokensAsString.contains("C:7:'grote'"));
    assertTrue(tokensAsString.contains("C:8:'stomer'"));
  }

  //  String newLine = System.getProperty("line.separator");

  @Test
  public void test1() {
    SimpleWitness[] sw = createWitnesses("A B C A B", "A B C A B");
    VariantGraph vg = collate(sw[0]);
    MatchTableLinker linker = new MatchTableLinker(1);
    Map<Token, VariantGraph.Vertex> linkedTokens = linker.link(vg, sw[1], new EqualityTokenComparator());

    Set<Token> tokens = linkedTokens.keySet();
    Set<String> tokensAsString = Sets.newLinkedHashSet();
    for (Token token : tokens) {
      tokensAsString.add(token.toString());
    }
    assertTrue(tokensAsString.contains("B:0:'a'"));
    assertTrue(tokensAsString.contains("B:1:'b'"));
    assertTrue(tokensAsString.contains("B:2:'c'"));
    assertTrue(tokensAsString.contains("B:3:'a'"));
    assertTrue(tokensAsString.contains("B:4:'b'"));
  }

  @Test
  public void testOverDeAtlantischeOceaan() {
    int outlierTranspositionsSizeLimit = 1;
    collationAlgorithm = CollationAlgorithmFactory.dekkerMatchMatrix(new StrictEqualityTokenComparator(), outlierTranspositionsSizeLimit);
    String textD9 = "Over de Atlantische Oceaan voer een grote stomer. De lucht was helder blauw, het water rimpelend satijn.<p/> Op de Atlantische Oceaan voer een ontzaggelijk zeekasteel. Onder de vele passagiers aan boord, bevond zich een bruine, korte dikke man. Hij werd nooit zonder sigaar gezien. Zijn pantalon had lijnrechte vouwen in de pijpen, maar zat toch altijd vol rimpels. De pantalon werd naar boven toe breed, ongelofelijk breed: hij omsloot de buik van de kleine man als een soort balkon.";
    String textDMD1 = "Over de Atlantische Oceaan voer een grote stomer. De lucht was helder blauw, het water rimpelend satijn.<p/>\nOp sommige dekken van de stomer lagen mensen in de zon, op andere dekken werd getennist, op nog andere liepen de passagiers heen en weer en praatten. Wie over de reling hing en recht naar beneden keek, kon vaststellen dat het schip vorderde; of draaide alleen de aarde er onderdoor?<p/>\nOp de Atlantische Oceaan voer een ontzaggelijk zeekasteel. Onder de vele passagiers aan boord, bevond zich een bruine, korte dikke man. Hij werd nooit zonder sigaar gezien. Zijn pantalon had lijnrechte vouwen in de pijpen, maar zat toch altijd vol rimpels. De pantalon werd naar boven toe breed, ongelofelijk breed: hij omsloot de buik van de kleine man als een soort balkon.<p/>";
    SimpleWitness[] sw = createWitnesses(textD9, textDMD1);
    VariantGraph vg = collate(sw[0]);
    Map<Token, VariantGraph.Vertex> linkedTokens = new MatchTableLinker(outlierTranspositionsSizeLimit).link(vg, sw[1], new StrictEqualityTokenComparator());

    Set<Token> tokens = linkedTokens.keySet();
    Set<String> tokensAsString = Sets.newLinkedHashSet();
    for (Token token : tokens) {
      tokensAsString.add(token.toString());
    }
    List<String> l = Lists.newArrayList(tokensAsString);
    Collections.sort(l);
    LOG.log(Level.FINE, Joiner.on('\n').join(l));
    assertTrue(tokensAsString.contains("B:87:'onder'"));
    assertTrue(tokensAsString.contains("B:0:'over'"));
    assertTrue(tokensAsString.contains("B:1:'de'"));
    assertTrue(tokensAsString.contains("B:2:'atlantische'"));
    assertTrue(tokensAsString.contains("B:3:'oceaan'"));
    assertTrue(tokensAsString.contains("B:4:'voer'"));
  }

  @Test
  public void testHermansAllesIsBetrekkelijk1() throws XMLStreamException {
    int outlierTranspositionsSizeLimit = 1;
    String textD1 = "natuurlijk is alles betrekkelijk";
    String textD9 = "Natuurlijk, alles mag relatief zijn";
    String textDmd1 = "Natuurlijk, alles is betrekkelijk";
    SimpleWitness[] sw = createWitnesses(textD1, textD9, textDmd1);
    VariantGraph vg = collate(sw[0], sw[1]);
    Map<Token, VariantGraph.Vertex> linkedTokens = new MatchTableLinker(outlierTranspositionsSizeLimit).link(vg, sw[2], new StrictEqualityTokenComparator());

    Set<Token> tokens = linkedTokens.keySet();
    Set<String> tokensAsString = Sets.newLinkedHashSet();
    for (Token token : tokens) {
      tokensAsString.add(token.toString());
    }
    List<String> l = Lists.newArrayList(tokensAsString);
    Collections.sort(l);
    LOG.log(Level.FINE, "tokensAsString={0}", l);
    //    assertTrue(tokensAsString.contains("B:75:'onder'"));
    //    assertTrue(tokensAsString.contains("B:0:'over'"));
    //    assertTrue(tokensAsString.contains("B:1:'de'"));
    //    assertTrue(tokensAsString.contains("B:2:'atlantische'"));
    //    assertTrue(tokensAsString.contains("B:3:'oceaan'"));
    //    assertTrue(tokensAsString.contains("B:4:'voer'"));
  }

  @Test
  public void testSuscepto() throws XMLStreamException {
    int outlierTranspositionsSizeLimit = 1;
    String a = "Et sumpno suscepto tribus diebus morte morietur et deinde ab inferis regressus ad lucem veniet.";
    String b = "Et mortem sortis finiet post tridui somnum et morte morietur tribus diebus somno suscepto et tunc ab inferis regressus ad lucem veniet.";
    String c = "Et sortem mortis tribus diebus sompno suscepto et tunc ab inferis regressus ad lucem veniet.";
    SimpleWitness[] sw = createWitnesses(a, b, c);
    VariantGraph vg = collate(sw[0], sw[1]);
    Map<Token, VariantGraph.Vertex> linkedTokens = new MatchTableLinker(outlierTranspositionsSizeLimit).link(vg, sw[2], new StrictEqualityTokenComparator());

    Set<Token> tokens = linkedTokens.keySet();
    Set<String> tokensAsString = Sets.newLinkedHashSet();
    for (Token token : tokens) {
      tokensAsString.add(token.toString());
    }
    List<String> l = Lists.newArrayList(tokensAsString);
    Collections.sort(l);
    LOG.log(Level.FINE, "tokensAsString={0}", l);
    assertTrue(tokensAsString.contains("C:6:'suscepto'"));
  }

  @Test
  public void testOutlierTranspositionLimitAndPunctuation() {
    int outlierTranspositionsSizeLimit = 200;
    String w1 = "a b c .";
    String w2 = "a b c Natuurlijk, alles mag relatief zijn.";
    SimpleWitness[] sw = createWitnesses(w1, w2);
    
    // assert that punctuation are separate tokens
    List<Token> tokensA = sw[0].getTokens();
    assertEquals("A:0:'a'", tokensA.get(0).toString());
    assertEquals("A:1:'b'", tokensA.get(1).toString());
    assertEquals("A:2:'c'", tokensA.get(2).toString());
    assertEquals("A:3:'.'", tokensA.get(3).toString());
    assertEquals(4, tokensA.size());
    
    VariantGraph vg = collate(sw[0]);
    Map<Token, VariantGraph.Vertex> linkedTokens = new MatchTableLinker(outlierTranspositionsSizeLimit).link(vg, sw[1], new StrictEqualityTokenComparator());
    
    // assert linked tokens; helper method
    Set<Token> tokens = linkedTokens.keySet();
    Set<String> tokensAsString = Sets.newLinkedHashSet();
    for (Token token : tokens) {
      tokensAsString.add(token.toString());
    }
    List<String> l = Lists.newArrayList(tokensAsString);
    Collections.sort(l);
    
    assertTrue(l.contains("B:0:'a'"));
    assertTrue(l.contains("B:1:'b'"));
    assertTrue(l.contains("B:2:'c'"));
    assertTrue(l.contains("B:9:'.'"));
    assertEquals(4, l.size());
  }
}
