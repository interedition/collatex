package eu.interedition.collatex.match;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Maps;

import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.Word;

public class WordPairCollectionTest {
  private WordPairCollection wpc;
  private HashMap<String, Witness> witnessHash;

  @Before
  public void SetUp() {
    witnessHash = Maps.newHashMap();
    wpc = new WordPairCollection(witnessHash);
    assertNotNull(wpc);
  }

  @Test
  public void testAddWordPair() {
    Map<String, Map<String, List<List<Word>>>> wordPairs = wpc.wordPairs;
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
    Map<String, Map<String, List<List<Word>>>> wordPairs = wpc.wordPairs;
    assertEquals(0, wordPairs.size());
    Word word0 = new Word("a", "next", 1);
    Word word1 = new Word("a", "men", 2);
    wpc.addWordPair(word0, word1);
    word0 = new Word("a", "next", 3);
    word1 = new Word("a", "men", 4);
    wpc.addWordPair(word0, word1);
    wordPairs.containsKey("next men");
    assertEquals(1, wordPairs.size());
    assertEquals(2, wordPairs.get("next men").get("a").size());

    List<WordSegment> wordSegments = wpc.getWordSegments();
    assertEquals(0, wordSegments.size());
  }
}
