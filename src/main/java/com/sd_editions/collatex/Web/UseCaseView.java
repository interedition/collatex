package com.sd_editions.collatex.Web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import com.sd_editions.collatex.Block.Block;
import com.sd_editions.collatex.Block.BlockStructure;
import com.sd_editions.collatex.Block.BlockStructureListIterator;
import com.sd_editions.collatex.Block.Util;
import com.sd_editions.collatex.Block.Word;
import com.sd_editions.collatex.match_spike.WordMatchMap;

public class UseCaseView {

  private WordMatchMap wordMatchMap;
  private List<BlockStructure> witnessList;

  public UseCaseView(String[] usecase) {
    witnessList = new ArrayList<BlockStructure>();
    for (String witness : usecase) {
      witnessList.add(Util.string2BlockStructure(witness));
    }
    wordMatchMap = new WordMatchMap(witnessList);
  }

  public String toHtml() {
    String html = "<ol>";
    int color = 1;
    HashMap<String, Integer> colorMap = new HashMap<String, Integer>();
    for (BlockStructure witness : witnessList) {
      html += "<li>";
      BlockStructureListIterator<? extends Block> iterator = witness.listIterator();
      while (iterator.hasNext()) {
        Block block = iterator.next();
        if (block instanceof Word) {
          String word = ((Word) block).getContent();
          String normalizedWord = word.toLowerCase(Locale.GERMAN);
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
