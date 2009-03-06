package com.sd_editions.collatex.match;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.SortedArraySet;
import com.sd_editions.collatex.Block.Block;
import com.sd_editions.collatex.Block.BlockStructure;
import com.sd_editions.collatex.Block.BlockStructureListIterator;
import com.sd_editions.collatex.Block.Word;

public class WordMatchMap {
  private final Map<String, WordMatches> wordMatchMap;
  public String[][] witnessWordsMatrix;

  public WordMatchMap(List<BlockStructure> witnessList) {
    this.wordMatchMap = Maps.newHashMap();

    int maxWords = maximumWordsInWitness(witnessList);
    witnessWordsMatrix = new String[witnessList.size()][maxWords];

    int witnessIndex = 0;
    for (BlockStructure blockStructure : witnessList) {
      int wordIndex = 0;
      BlockStructureListIterator<? extends Block> iterator = blockStructure.listIterator();
      while (iterator.hasNext()) {
        Object object = iterator.next();
        if (object instanceof Word) {
          String word = ((Word) object).getContent();
          String nWord = normalizeWord(word);
          WordMatches currentMatches = new WordMatches(nWord);
          if (this.wordMatchMap.containsKey(nWord)) {
            currentMatches = this.wordMatchMap.remove(nWord);
          }
          currentMatches.addExactMatch(new WordCoordinate(witnessIndex, wordIndex));
          this.wordMatchMap.put(nWord, currentMatches);
          witnessWordsMatrix[witnessIndex][wordIndex] = word;
          wordIndex++;
        }
      }
      witnessIndex++;
    }

    // Lev matches
    Set<String> allWords = this.wordMatchMap.keySet();
    for (String word1 : allWords) {
      for (String word2 : allWords) {
        Word w1 = new Word(word1);
        Word w2 = new Word(word2);
        if (w1.alignsWith(w2) && word1 != word2) {
          WordMatches currentMatches1 = this.wordMatchMap.get(word1);
          WordMatches currentMatches2 = this.wordMatchMap.get(word2);
          for (WordCoordinate match2 : currentMatches2.getExactMatches()) {
            currentMatches1.addLevMatch(match2);
          }
        }
      }
    }

  }

  private int maximumWordsInWitness(List<BlockStructure> witnessList) {
    int cols = 0;
    for (BlockStructure blockStructure : witnessList) {
      cols = Math.max(cols, blockStructure.getNumberOfBlocks() - 1);
    }
    return cols;
  }

  private String normalizeWord(String word) {
    return word.toLowerCase().replaceAll("[,.:; ]", "");
  }

  public List<String> getWords() {
    return Lists.newArrayList(wordMatchMap.keySet());
  }

  public SortedArraySet<WordCoordinate> getExactMatches(String word) {
    final WordMatches wordMatches = wordMatchMap.get(word);
    if (wordMatches == null) return null;
    return wordMatchMap.get(word).getExactMatches();
  }

  public int[] getExactMatchesForWitness(String word, int i) {
    Set<WordCoordinate> exactMatches = getExactMatches(word);
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

  public SortedArraySet<WordCoordinate> getLevMatches(String word) {
    final WordMatches wordMatches = wordMatchMap.get(word);
    if (wordMatches == null) return null;
    return wordMatches.getLevMatches();
  }

  //  @SuppressWarnings("boxing")
  //  public int[][] getColorMatrix() {
  //    // MatchMatrix is a matrix, where each row corresponds with a witness,
  //    // and each colum corresponds with a word in a witness.
  //    // each cell contains a word i top indicate which words match
  //
  //    // start off with a matrix filled with 0 for words, and null at the end of rows to fill out the row.
  //    String[] witness1Words = witnessWordsMatrix[0];
  //    int[][] colormatrix = new int[witnessWordsMatrix.length][witness1Words.length];
  //    for (int witnessId = 0; witnessId < colormatrix.length; witnessId++) {
  //      for (int wordId = 0; wordId < colormatrix[witnessId].length; wordId++) {
  //        colormatrix[witnessId][wordId] = (witnessWordsMatrix[witnessId][wordId] == null) ? END_OF_WITNESS : NO_COLOR_ASSIGNED;
  //      }
  //    }
  //
  //    int colorId = 1;
  //    for (int witnessId = 0; witnessId < colormatrix.length; witnessId++) {
  //      for (int wordId = 0; wordId < colormatrix[witnessId].length; wordId++) {
  //        if (colormatrix[witnessId][wordId] == END_OF_WITNESS) {
  //          break;
  //        } else if (colormatrix[witnessId][wordId] == NO_COLOR_ASSIGNED) {
  //          colormatrix[witnessId][wordId] = colorId;
  //          // give the matching words in other rows the same color
  //          String word = witnessWordsMatrix[witnessId][wordId];
  //          WordMatches wordMatches = wordMatchMap.get(word);
  //          // TODO: do something with the permutations in wordMatches
  //          // If this is the best place to deal with it..
  //          colorId++;
  //        }
  //      }
  //    }
  //
  //    return colormatrix;
  //  }

  @SuppressWarnings("boxing")
  public Set<ColorMatrix> getColorMatrixPermutations() {
    // colormatrix is a matrix where each row corresponds with a witness,
    // and each colum corresponds with a word in a witness.
    // each cell contains a color index to indicate which words match

    // start with a matrix filled with NO_COLOR_ASSIGNED (0) for words, and END_OF_WITNESS (-1) at the end of rows to fill out the row.
    String[] witness1Words = witnessWordsMatrix[0];
    ColorMatrix colormatrix = new ColorMatrix(witnessWordsMatrix.length, witness1Words.length);
    for (int witnessId = 0; witnessId < colormatrix.getHeight(); witnessId++) {
      for (int wordId = 0; wordId < colormatrix.getWidth(); wordId++) {
        colormatrix.setCell(witnessId, wordId, (witnessWordsMatrix[witnessId][wordId] == null) ? ColorMatrix.END_OF_WITNESS : ColorMatrix.NO_COLOR_ASSIGNED);
      }
    }
    return getColorMatrixPermutations(colormatrix, wordMatchMap, 1, 0, 0);
  }

  @SuppressWarnings("boxing")
  private Set<ColorMatrix> getColorMatrixPermutations(ColorMatrix initialColormatrix, Map<String, WordMatches> wordMatchMap1, int initialColorId, int startWitnessId, int initialWordId) {
    ColorMatrix colormatrix = new ColorMatrix(initialColormatrix);
    int startWordId = initialWordId;
    int colorId = initialColorId;
    Set<ColorMatrix> set = Sets.newHashSet();
    for (int witnessId = startWitnessId; witnessId < colormatrix.getHeight(); witnessId++) {
      for (int wordId = startWordId; wordId < colormatrix.getWidth(); wordId++) {
        if (colormatrix.getCell(witnessId, wordId) == ColorMatrix.END_OF_WITNESS) {
          break;
        } else if (colormatrix.getCell(witnessId, wordId) == ColorMatrix.NO_COLOR_ASSIGNED) {
          colormatrix.setCell(witnessId, wordId, colorId);
          // give the matching words in other rows the same color
          String word = normalizeWord(witnessWordsMatrix[witnessId][wordId]);
          WordMatches wordMatches = wordMatchMap1.get(word);
          List<WordMatches> permutations = wordMatches.getPermutations();
          if (permutations.size() > 1) {
            for (WordMatches permutation : permutations) {
              Map<String, WordMatches> tmpWordMatchMap = new HashMap<String, WordMatches>(wordMatchMap1);
              tmpWordMatchMap.remove(word);
              tmpWordMatchMap.put(word, permutation);
              set.addAll(getColorMatrixPermutations(colormatrix, tmpWordMatchMap, colorId + 1, witnessId, wordId));
            }
          } else {
            WordMatches permutation = permutations.get(0);
            for (WordCoordinate match : permutation.getAllMatches()) {
              if (colormatrix.getCell(match.witnessNumber, match.positionInWitness) == ColorMatrix.NO_COLOR_ASSIGNED) {
                colormatrix.setCell(match.witnessNumber, match.positionInWitness, colorId);
              }
            }
            set.add(colormatrix);
          }
          colorId++;
        }
      }
      startWordId = 0;
    }
    return set;
  }
}
