package com.sd_editions.collatex.Web;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.base.Join;
import com.google.common.collect.Lists;
import com.sd_editions.collatex.match_spike.WordColorTuple;
import com.sd_editions.collatex.spike2.Colors;
import com.sd_editions.collatex.spike2.Comparison;
import com.sd_editions.collatex.spike2.Modification;
import com.sd_editions.collatex.spike2.WitnessIndex;
import com.sd_editions.collatex.spike2.collate.Addition;
import com.sd_editions.collatex.spike2.collate.Removal;
import com.sd_editions.collatex.spike2.collate.Transposition;

@SuppressWarnings("boxing")
public class ColorsView {

  private final Colors colors;

  public ColorsView(Colors _colors) {
    this.colors = _colors;
  }

  public String toHtml() {
    return coloredWitnesses() + modifications();
  }

  private String coloredWitnesses() {
    String html = "<ol>";
    for (int row = 0; row < colors.numberOfWitnesses(); row++) {
      html += "<li>";
      List<String> htmlWords = Lists.newArrayList();
      WitnessIndex witnessIndex = colors.getWitnessIndex(row + 1);
      Set<Integer> colorsPerWitness = witnessIndex.getWordCodes();
      final Iterator<Integer> iterator = colorsPerWitness.iterator();
      for (int col = 0; col < colorsPerWitness.size(); col++) {
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

  private String modifications() {
    String html = "";
    final int numberOfWitnesses = colors.numberOfWitnesses();
    for (int base = 1; base < numberOfWitnesses; base++) {
      for (int w = base + 1; w <= numberOfWitnesses; w++) {
        html += "Comparing witness " + base + " with witness " + w + ":<ul>";
        Comparison compareWitness = colors.compareWitness(base, w);
        List<Modification> modifications = compareWitness.getModifications();
        if (modifications.isEmpty()) {
          html += "no additions, removals or transpositions";
        } else {
          for (Modification modification : modifications) {
            if (modification instanceof Addition) {
              html += "<li>" + additionView((Addition) modification, base, w) + "</li>";
            } else if (modification instanceof Removal) {
              html += "<li>" + removalView((Removal) modification, base) + "</li>";
            } else if (modification instanceof Transposition) {
              html += "<li>" + transpositionView((Transposition) modification, base, w) + "</li>";
            }
          }
        }
        html += "</ul>";
      }
    }
    return html;
  }

  private String transpositionView(Transposition transposition, int base, int w) {
    //    return "<i>" + "</i> transposed from position " + transposition. + " to " + y;
    return "<i>" + transposition.getTransposedWord() + "</i> transposed over distance " + transposition.getTranspositionDistance();
  }

  private String removalView(Removal removal, int base) {
    WitnessIndex baseIndex = colors.getWitnessIndex(base);
    int position = removal.getPosition();
    return "<i>" + baseIndex.getWordOnPosition(position) + "</i> at position " + (position + 1) + " removed ";
  }

  private String additionView(Addition addition, int base, int w) {
    WitnessIndex baseIndex = colors.getWitnessIndex(base);
    WitnessIndex witnessIndex = colors.getWitnessIndex(w);
    int position = addition.getPosition();
    String html = "<i>" + witnessIndex.getWordOnPosition(position) + "</i> added ";
    List<String> baseWords = baseIndex.getWords();
    List<String> witnessWords = witnessIndex.getWords();
    if (position == 0) {
      html += "before <i>" + baseWords.get(0) + "</i>";
    } else if (position == witnessWords.size() - 1) {
      html += " after <i>" + baseWords.get(baseWords.size() - 1) + "</i>";
    } else {
      html += "between <i>" + witnessWords.get(position - 1) + "</i> and <i>" + witnessWords.get(position + 1) + "</i>";
    }
    return html;
  }
}
