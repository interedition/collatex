package com.sd_editions.collatex.Web;

import java.util.HashMap;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sd_editions.collatex.Block.Block;
import com.sd_editions.collatex.Block.BlockStructure;
import com.sd_editions.collatex.Block.BlockStructureListIterator;
import com.sd_editions.collatex.Block.Util;
import com.sd_editions.collatex.Block.Word;
import com.sd_editions.collatex.match_spike.WordMatchMap;
import com.sd_editions.collatex.match_spike.views.WordMatchMapView;

public class UseCaseView {

  private WordMatchMap wordMatchMap;
  private List<BlockStructure> witnessList;
  private List<List<int[]>> matchMatrixList;

  public UseCaseView(String[] usecase) {
    witnessList = Lists.newArrayList();
    for (String witness : usecase) {
      witnessList.add(Util.string2BlockStructure(witness));
    }
    wordMatchMap = new WordMatchMap(witnessList);
    matchMatrixList = makeMatchMatrixList();
  }

  public List<List<int[]>> makeMatchMatrixList() {
    List<List<int[]>> matrixList = Lists.newArrayList();
    List<int[]> matrix = Lists.newArrayList();
    int numberOfWitnessess = witnessList.size();
    for (int i = 0; i < numberOfWitnessess; i++) {
      //      vector[i] = 
    }
    matrixList.add(matrix);
    return matrixList;
  }

  public String toHtml() {
    String html = "<ol type=\"A\">";
    int color = 1;
    HashMap<String, Integer> colorMap = Maps.newHashMap();
    for (BlockStructure witness : witnessList) {
      html += "<li>";
      BlockStructureListIterator<? extends Block> iterator = witness.listIterator();
      while (iterator.hasNext()) {
        Block block = iterator.next();
        if (block instanceof Word) {
          String word = ((Word) block).getContent();
          String normalizedWord = word.toLowerCase();
          if (!colorMap.containsKey(normalizedWord)) {
            colorMap.put(normalizedWord, new Integer(color++));
          }
          html += "<span class=\"color" + colorMap.get(normalizedWord) + "\">" + word + "</span> ";
        }
      }
      html += "</li>";
    }
    html += "</ol>";
    html += new WordMatchMapView(wordMatchMap).toHtml();
    return html;
  }

}
