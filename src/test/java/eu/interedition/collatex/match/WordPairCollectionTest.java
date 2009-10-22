package eu.interedition.collatex.match;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sd_editions.collatex.Block.Util;

import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.Word;
import eu.interedition.collatex.input.builders.WitnessBuilder;

public class WordPairCollectionTest {
  private WordPairCollection wpc;
  private Map<String, Map<String, List<List<Word>>>> wordPairs;
  private Segment a;
  private Segment b;

  @Before
  public void setUp() {
    WitnessBuilder builder = new WitnessBuilder();
    a = builder.build("op zijn pad liep zijn hond aan zijn hand op zijn dag");
    b = builder.build("zijn hond liep aan zijn hand op zijn dag");
    HashMap<String, Segment> witnessHash = Maps.newHashMap();
    for (Segment witness : Lists.newArrayList(a, b)) {
      witnessHash.put(witness.id, witness);
    }
    wpc = new WordPairCollection(witnessHash);
    wordPairs = wpc.wordPairs;
    assertNotNull(wpc);
  }

  @Test
  public void testAddWordPair() {
    assertEquals(0, wordPairs.size());
    Word word0 = new Word("a", "next", 1);
    Word word1 = new Word("a", "men", 2);
    wpc.addWordPair(word0, word1);
    assertEquals(1, wordPairs.size());
    wordPairs.containsKey("next men");
    word0 = new Word("b", "amazing", 1);
    word1 = new Word("b", "spider-man", 2);
    wpc.addWordPair(word0, word1);
    assertEquals(2, wordPairs.size());
    word0 = new Word("c", "amazing", 1);
    word1 = new Word("c", "spider-man", 2);
    wpc.addWordPair(word0, word1);
    assertEquals(2, wordPairs.size());
    wordPairs.containsKey("amazing spider-man");
    assertEquals(2, wordPairs.get("amazing spider-man").size());
  }

  @Test
  public void testGetWordSegments() {
    assertEquals(0, wordPairs.size());
    Word word0 = new Word(a.id, "op", 1);
    Word word1 = new Word(a.id, "zijn", 2);
    wpc.addWordPair(word0, word1);
    word0 = new Word(a.id, "op", 10);
    word1 = new Word(a.id, "zijn", 11);
    wpc.addWordPair(word0, word1);
    word0 = new Word(b.id, "op", 7);
    word1 = new Word(b.id, "zijn", 8);
    wpc.addWordPair(word0, word1);
    wordPairs.containsKey("op zijn");
    assertEquals(1, wordPairs.size());
    assertEquals(2, wordPairs.get("op zijn").get(a.id).size());
    assertEquals(1, wordPairs.get("op zijn").get(b.id).size());

    List<String> wordsInSegments = Lists.newArrayList();
    List<WordSegment> wordSegments = wpc.getWordSegments(wordsInSegments);
    Util.p(wordSegments);
    assertEquals(2, wordSegments.size());
    String title0 = wordSegments.get(0).title;
    String title1 = wordSegments.get(1).title;
    List<String> titles = Lists.newArrayList(title0, title1);
    assertTrue(titles.contains("op zijn dag"));
  }
}
