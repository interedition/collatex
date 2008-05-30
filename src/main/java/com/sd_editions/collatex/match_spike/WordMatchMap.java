package com.sd_editions.collatex.match_spike;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sd_editions.collatex.Block.Block;
import com.sd_editions.collatex.Block.BlockStructure;
import com.sd_editions.collatex.Block.BlockStructureListIterator;
import com.sd_editions.collatex.Block.Word;

public class WordMatchMap {
  private HashMap<String, WordMatches> map;

  public WordMatchMap(List<BlockStructure> witnessList) {
    this.map = Maps.newHashMap();

    int witnessIndex = 0;
    for (BlockStructure blockStructure : witnessList) {
      int wordIndex = 0;
      BlockStructureListIterator<? extends Block> iterator = blockStructure.listIterator();
      while (iterator.hasNext()) {
        Object object = iterator.next();
        if (object instanceof Word) {
          String word = normalizeWord(((Word) object).getContent());
          WordMatches currentMatches = new WordMatches(word);
          if (this.map.containsKey(word)) {
            currentMatches = this.map.get(word);
          }
          currentMatches.addExactMatch(new WordCoordinate(witnessIndex, wordIndex));
          this.map.put(word, currentMatches);
          wordIndex++;
        }
      }
      witnessIndex++;
    }

    Set<String> allWords = this.map.keySet();
    for (String word1 : allWords) {
      for (String word2 : allWords) {
        Word w1 = new Word(word1);
        Word w2 = new Word(word2);
        if (w1.alignsWith(w2) && word1 != word2) {
          WordMatches currentMatches1 = this.map.get(word1);
          WordMatches currentMatches2 = this.map.get(word2);
          for (WordCoordinate match2 : currentMatches2.getExactMatches()) {
            currentMatches1.addLevMatch(match2);
          }
        }
      }
    }
  }

  private String normalizeWord(String word) {
    return word.toLowerCase().replaceAll("[,.:; ]", "");
  }

  public List<String> getWords() {
    return Lists.newArrayList(map.keySet());
  }

  public List<WordCoordinate> getExactMatches(String word) {
    final WordMatches wordMatches = map.get(word);
    if (wordMatches == null) return null;
    return map.get(word).getExactMatches();
  }

  public int[] getExactMatchesForWitness(String word, int i) {
    List<WordCoordinate> exactMatches = getExactMatches(word);
    List<Integer> positionList = Lists.newArrayList();
    for (WordCoordinate wordCoordinate : exactMatches) {
      if (wordCoordinate.witnessNumber == i) {
        positionList.add(new Integer(wordCoordinate.positionInWitness));
      }
    }
    int[] positionArray = new int[positionList.size()];
    int n = 0;
    for (Integer j : positionList) {
      positionArray[n++] = j.intValue();
    }
    return positionArray;
  }

  public List<WordCoordinate> getLevMatches(String word) {
    final WordMatches wordMatches = map.get(word);
    if (wordMatches == null) return null;
    return wordMatches.getLevMatches();
  }

}
