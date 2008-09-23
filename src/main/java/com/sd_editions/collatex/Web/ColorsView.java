package com.sd_editions.collatex.Web;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Join;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sd_editions.collatex.match_spike.WordColorTuple;
import com.sd_editions.collatex.spike2.Colors;
import com.sd_editions.collatex.spike2.LevenshteinMatch;
import com.sd_editions.collatex.spike2.Match;
import com.sd_editions.collatex.spike2.Modification;
import com.sd_editions.collatex.spike2.Modifications;
import com.sd_editions.collatex.spike2.Witness;
import com.sd_editions.collatex.spike2.WitnessIndex;
import com.sd_editions.collatex.spike2.Word;
import com.sd_editions.collatex.spike2.collate.Addition;
import com.sd_editions.collatex.spike2.collate.Removal;
import com.sd_editions.collatex.spike2.collate.Replacement;
import com.sd_editions.collatex.spike2.collate.Transposition;

public class ColorsView {

  private final Colors colors;

  public ColorsView(Colors _colors) {
    this.colors = _colors;
  }

  public String toHtml() {
    return witnesses() + modifications();
  }

  private String witnesses() {
    String html = "<ol>";
    for (int row = 0; row < colors.numberOfWitnesses(); row++) {
      html += "<li>" + colors.witnesses.get(row).sentence + "</li>";
    }
    html += "</ol>";
    return html;
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
        String word = witnessIndex.getWords().get(col).toString();
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
        html += "Comparing witnesses " + base + " and " + w + ":<ol>";
        List<Modifications> modificationsList = colors.compareWitness(base, w);
        int pn = 1;
        for (Modifications modifications : modificationsList) {
          html += "Permutation " + pn++ + "/" + modificationsList.size() + "<ul>";
          html += witnessPairView(base, w, modifications);
          html += modificationsView(base, w, modifications);
          html += "</ul>";
        }
        html += "</ol>";
      }
    }
    return html;
  }

  private String modificationsView(int base, int w, Modifications modifications) {
    String html = "<li>Modifications:</li><ul>";
    List<Modification> modificationsL = modifications.getModifications();
    if (modificationsL.isEmpty()) {
      html += "<li>no additions, removals or transpositions</li>";
    } else {
      for (Modification modification : modificationsL) {
        if (modification instanceof LevenshteinMatch) {
          html += "<li>" + levenshteinMatch((LevenshteinMatch) modification) + "</li>";
        } else if (modification instanceof Addition) {
          html += "<li>" + additionView((Addition) modification, base, w) + "</li>";
        } else if (modification instanceof Removal) {
          html += "<li>" + removalView((Removal) modification, base) + "</li>";
        } else if (modification instanceof Transposition) {
          html += "<li>" + transpositionView((Transposition) modification, base, w) + "</li>";
        } else if (modification instanceof Replacement) {
          html += "<li>" + replacementView((Replacement) modification, base, w) + "</li>";
        }
      }
    }
    html += "</ul>";
    return html;
  }

  @SuppressWarnings("boxing")
  private String witnessPairView(int base, int w, Modifications modifications) {
    StringBuffer html = new StringBuffer("<li>Colored Witnesses:<ol>");
    html.append("<li value=\"" + base + "\">");
    Witness witness1 = colors.witnesses.get(base - 1);
    int colorcounter = 1;
    int basePosition = 1;
    List<String> words = Lists.newArrayList();
    Map<Integer, Integer> witnessWordColors = Maps.newHashMap();
    for (Word word1 : witness1.getWords()) {
      Match match = modifications.getMatchAtBasePosition(basePosition);
      if (match != null) witnessWordColors.put(match.getWitnessWord().position, colorcounter);
      words.add(new WordColorTuple(word1.original, "color" + colorcounter++).toHtml());
      basePosition++;
    }
    html.append(Join.join(" ", words));
    html.append("</li>");
    html.append("<li value=\"" + w + "\">");
    Witness witness2 = colors.witnesses.get(w - 1);
    words = Lists.newArrayList();
    int witnessPosition = 1;
    for (Word word2 : witness2.getWords()) {
      int color = (witnessWordColors.containsKey(witnessPosition)) ? witnessWordColors.get(witnessPosition) : colorcounter++;
      words.add(new WordColorTuple(word2.original, "color" + color).toHtml());
      witnessPosition++;
    }
    html.append(Join.join(" ", words));
    html.append("</li></ol></li>");
    return html.toString();
  }

  private String levenshteinMatch(LevenshteinMatch modification) {
    return "<i>" + modification.base() + "</i> matches with <i>" + modification.witness() + "</i>";
  }

  private String replacementView(Replacement replacement, int base, int w) {
    int position = replacement.getPosition();
    return "<i>" + replacement.getOriginalWords() + "</i> replaced by <i>" + replacement.getReplacementWords() + "</i> at position " + position; // TODO: TEMP!
  }

  private String transpositionView(Transposition transposition, int base, int w) {
    //    return "<i>" + "</i> transposed from position " + transposition. + " to " + y;
    return "<i>" + transposition.getLeft() + "</i> transposed with <i>" + transposition.getRight() + "</i>";
  }

  private String removalView(Removal removal, int base) {
    int position = removal.getPosition();
    return "<i>" + removal.getRemovedWords() + "</i> at position " + (position) + " removed ";
  }

  private String additionView(Addition addition, int base, int w) {
    WitnessIndex baseIndex = colors.getWitnessIndex(base);
    String html = "<i>" + addition.getAddedWords() + "</i> added ";
    List<Word> baseWords = baseIndex.getWords();
    int position = addition.getPosition();
    if (position == 1) {
      html += "before <i>" + baseWords.get(0) + "</i>";
    } else if (position > baseWords.size()) {
      html += " after <i>" + baseWords.get(baseWords.size() - 1) + "</i>";
    } else {
      html += "between <i>" + baseWords.get(position - 2) + "</i> and <i>" + baseWords.get(position - 1) + "</i>";
    }
    return html;
  }
}
