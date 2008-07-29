package com.sd_editions.collatex.Web;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.base.Join;
import com.google.common.collect.Lists;
import com.sd_editions.collatex.match_spike.WordColorTuple;
import com.sd_editions.collatex.spike2.Colors;
import com.sd_editions.collatex.spike2.WitnessIndex;

public class ColorsView {

  private final Colors colors;

  public ColorsView(Colors _colors) {
    this.colors = _colors;
  }

  public String toHtml() {
    String html = "<ol type=\"A\">";
    for (int row = 0; row < colors.numberOfWitnesses(); row++) {
      html += "<li>";
      List<String> htmlWords = Lists.newArrayList();
      WitnessIndex witnessIndex = colors.getWitnessIndex(row + 1);
      Set<Integer> colorsPerWitness = witnessIndex.getWordCodes();
      final Iterator<Integer> iterator = colorsPerWitness.iterator();
      //      System.out.println(Join.join(",", wordMatchMap.witnessWordsMatrix[row]));
      //      System.out.println(colorsPerWitness);
      for (int col = 0; col < colorsPerWitness.size(); col++) {
        //        System.out.println(col);
        String word = witnessIndex.getWords().get(col);
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

}
