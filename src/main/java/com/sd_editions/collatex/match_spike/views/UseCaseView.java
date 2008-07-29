package com.sd_editions.collatex.match_spike.views;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.base.Join;
import com.google.common.collect.Lists;
import com.sd_editions.collatex.Block.Block;
import com.sd_editions.collatex.Block.BlockStructure;
import com.sd_editions.collatex.Block.BlockStructureListIterator;
import com.sd_editions.collatex.Block.Util;
import com.sd_editions.collatex.Block.Word;
import com.sd_editions.collatex.match_spike.ColorMatrix;
import com.sd_editions.collatex.match_spike.WordColorTuple;
import com.sd_editions.collatex.match_spike.WordMatchMap;
import com.sd_editions.collatex.spike2.Colors;

public class UseCaseView {

  private final WordMatchMap wordMatchMap;
  private final List<BlockStructure> witnessList;
  private final Colors colors;
  private final Set<ColorMatrix> colorMatrixPermutations;

  //  private final List<List<int[]>> matchMatrixList;

  public UseCaseView(String[] usecase) {
    colors = new Colors(usecase);
    witnessList = Lists.newArrayList();
    for (String witness : usecase) {
      witnessList.add(Util.string2BlockStructure(witness));
    }
    wordMatchMap = new WordMatchMap(witnessList);
    //    matchMatrixList = makeMatchMatrixList();
    fillWordMatrix();
    colorMatrixPermutations = wordMatchMap.getColorMatrixPermutations();
  }

  //  public List<List<int[]>> makeMatchMatrixList() {
  //    List<List<int[]>> matrixList = Lists.newArrayList();
  //    List<int[]> matrix = Lists.newArrayList();
  //    int numberOfWitnessess = witnessList.size();
  //    for (int i = 0; i < numberOfWitnessess; i++) {
  //      //      vector[i] = ;
  //    }
  //    matrixList.add(matrix);
  //    return matrixList;
  //  }

  private void fillWordMatrix() {
    int cols = 0;
    for (BlockStructure blockStructure : witnessList) {
      cols = Math.max(cols, blockStructure.getNumberOfBlocks());
    }
    //    Util.p(cols);
    WordColorTuple[][] wcm = new WordColorTuple[witnessList.size()][cols];

    for (int witness_index = 0; witness_index < witnessList.size(); witness_index++) {
      BlockStructure witness = witnessList.get(witness_index);
      //      Util.p(witness);
      BlockStructureListIterator<? extends Block> listIterator = witness.listIterator();
      listIterator.next(); // first blockstructure is always a sentence
      for (int word_index = 0; word_index < wcm.length; word_index++) {
        //        Util.p(word_index);
        if (listIterator.hasNext()) {
          wcm[witness_index][word_index] = null;
        } else {
          final Word next = (Word) listIterator.next();
          String word = (next).getContent();
          //          Util.p(word);
          String normalizedWord = word.toLowerCase();
          //          String color = "color" + colorMap.determineColor(normalizedWord, witness_index, word_index);
          //          wcm[witness_index][word_index] = new WordColorTuple(word, color);
        }
      }
    }
  }

  //  public String toHtml() {
  //    String html = "<ol type=\"A\">";
  //    int color = 1;
  //    HashMap<String, Integer> colorMap = Maps.newHashMap();
  //    for (BlockStructure witness : witnessList) {
  //      html += "<li>";
  //      BlockStructureListIterator<? extends Block> iterator = witness.listIterator();
  //      while (iterator.hasNext()) {
  //        Block block = iterator.next();
  //        if (block instanceof Word) {
  //          String word = ((Word) block).getContent();
  //          String normalizedWord = word.toLowerCase();
  //          if (!colorMap.containsKey(normalizedWord)) colorMap.put(normalizedWord, new Integer(color++));
  //          html += "<span class=\"color" + colorMap.get(normalizedWord) + "\">" + word + "</span> ";
  //        }
  //      }
  //      html += "</li>";
  //    }
  //    html += "</ol>";
  //    html += new WordMatchMapView(wordMatchMap).toHtml();
  //    return html;
  //  }

  //  public String toHtml() {
  //    String html = new WordMatchMapView(wordMatchMap).toHtml();
  //    Set<ColorMatrix> colorMatrixPermutations = wordMatchMap.getColorMatrixPermutations();
  //    html += "<h4>Permutations:</h4>";
  //    html += "<ol>";
  //    for (ColorMatrix colorMatrix : colorMatrixPermutations) {
  //      html += "<li><ol type=\"A\">";
  //      for (int row = 0; row < colorMatrix.getHeight(); row++) {
  //        html += "<li>";
  //        List<String> htmlWords = Lists.newArrayList();
  //        for (int col = 0; col < colorMatrix.getWidth(); col++) {
  //          String word = wordMatchMap.witnessWordsMatrix[row][col];
  //          if (word != null) htmlWords.add(new WordColorTuple(word, "color" + colorMatrix.getCell(row, col)).toHtml());
  //        }
  //        html += Join.join(" ", htmlWords);
  //        html += "</br></li>";
  //      }
  //      html += "<pre>" + colorMatrix.toString() + "</pre>";
  //      html += "</ol></li>";
  //    }
  //    html += "</ol>";
  //    return html;
  //  }
  public String toHtml() {
    String html = new WordMatchMapView(wordMatchMap).toHtml();
    html += colorsView(colors);
    //    html += permutationsView(colorMatrixPermutations);
    return html;
  }

  private String colorsView(Colors colors2) {
    String html = "<h4>Colors:</h4>";
    html += "<ol type=\"A\">";
    for (int row = 0; row < colors2.numberOfWitnesses(); row++) {
      html += "<li>";
      List<String> htmlWords = Lists.newArrayList();
      Set<Integer> colorsPerWitness = colors2.getWitnessIndex(row + 1).getWords();
      final Iterator<Integer> iterator = colorsPerWitness.iterator();
      System.out.println(Join.join(",", wordMatchMap.witnessWordsMatrix[row]));
      System.out.println(colorsPerWitness);
      for (int col = 0; col < colorsPerWitness.size(); col++) {
        System.out.println(col);
        String word = wordMatchMap.witnessWordsMatrix[row][col];
        if (word != null) {
          htmlWords.add(new WordColorTuple(word, "color" + iterator.next()).toHtml());
        }
      }
      html += Join.join(" ", htmlWords);
      html += "</br></li>";
    }
    html += "</ol>";
    return html;
  }

  private String permutationsView(Set<ColorMatrix> colorMatrixPermutations) {
    String html = "<h4>Permutations:</h4>";
    html += "<ol>";
    for (ColorMatrix colorMatrix : colorMatrixPermutations) {
      html += "<li><ol type=\"A\">";
      for (int row = 0; row < colorMatrix.getHeight(); row++) {
        html += "<li>";
        List<String> htmlWords = Lists.newArrayList();
        for (int col = 0; col < colorMatrix.getWidth(); col++) {
          String word = wordMatchMap.witnessWordsMatrix[row][col];
          if (word != null) htmlWords.add(new WordColorTuple(word, "color" + colorMatrix.getCell(row, col)).toHtml());
        }
        html += Join.join(" ", htmlWords);
        html += "</br></li>";
      }
      html += "<pre>" + colorMatrix.toString() + "</pre>";
      html += "</ol></li>";
    }
    html += "</ol>";
    return html;
  }
}
