package com.sd_editions.collatex.Web;

import java.util.List;
import java.util.Map;

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
    StringBuffer html = new StringBuffer("<b>Base</b>: " + colors.witnesses.get(0).sentence + "<br/>");
    for (int row = 1; row < colors.numberOfWitnesses(); row++) {
      html.append("<b>Witness " + row + "</b>: " + colors.witnesses.get(row).sentence + "<br/>");
    }
    html.append("<br/>");
    return html.toString();
  }

  private String modifications() {
    StringBuffer html = new StringBuffer();
    final int numberOfWitnesses = colors.numberOfWitnesses();
    for (int base = 1; base < numberOfWitnesses; base++) {
      if (base > 1) html.append("<span class=\"secondary\">");
      for (int w = base + 1; w <= numberOfWitnesses; w++) {
        html.append("Comparing base - witness " + (w - 1) + ":<ol>");
        List<Modifications> modificationsList = colors.compareWitness(base, w);
        int pn = 1;
        for (Modifications modifications : modificationsList) {
          if (pn > 1) html.append("<span class=\"secondary\">");
          html.append("<span class=\"secondary\">Permutation " + pn++ + "/" + modificationsList.size() + "</span><ul>");
          html.append("<span class=\"colored\" style=\"display:none\">");
          html.append(witnessPairView(base, w, modifications));
          html.append("</span>");
          html.append(modificationsView(base, modifications));
          html.append("<br/></ul>");
          if (pn > 1) html.append("</span>");
        }
        html.append("</ol>");
      }
      if (base > 1) html.append("</span>");
    }
    return html.toString();
  }

  private String modificationsView(int base, Modifications modifications) {
    StringBuffer html = new StringBuffer("<li>Modifications:</li><ul>");
    List<Modification> modificationsL = modifications.getModifications();
    if (modificationsL.isEmpty()) {
      html.append("<li>no additions, removals or transpositions</li>");
    } else {
      for (Modification modification : modificationsL) {
        if (modification instanceof LevenshteinMatch) {
          html.append("<li>" + levenshteinMatch((LevenshteinMatch) modification) + "</li>");
        } else if (modification instanceof Addition) {
          html.append("<li>" + additionView((Addition) modification, base) + "</li>");
        } else if (modification instanceof Removal) {
          html.append("<li>" + removalView((Removal) modification) + "</li>");
        } else if (modification instanceof Transposition) {
          html.append("<li>" + transpositionView((Transposition) modification) + "</li>");
        } else if (modification instanceof Replacement) {
          html.append("<li>" + replacementView((Replacement) modification) + "</li>");
        }
      }
    }
    html.append("</ul>");
    return html.toString();
  }

  @SuppressWarnings("boxing")
  private String witnessPairView(int base, int w, Modifications modifications) {
    StringBuffer html = new StringBuffer("<li>Colored Witnesses:<ol>");

    html.append("<li value=\"" + base + "\">");
    Witness witness1 = colors.witnesses.get(base - 1);
    int colorcounter = 0;
    List<String> words = Lists.newArrayList();
    Map<Word, Integer> matchedWitnessWordColors = Maps.newHashMap();
    int lastMatchedWitnessPosition = -1;
    for (Word word1 : witness1.getWords()) {
      Match match = modifications.getMatchAtBasePosition(word1.position);
      if (match == null) {
        if (lastMatchedWitnessPosition != -1) colorcounter++;
        lastMatchedWitnessPosition = -1;
      } else {
        Word witnessWord = match.getWitnessWord();
        int matchedWitnessPosition = witnessWord.position;
        if (lastMatchedWitnessPosition + 1 != matchedWitnessPosition) colorcounter++;
        matchedWitnessWordColors.put(witnessWord, colorcounter);
        lastMatchedWitnessPosition = matchedWitnessPosition;
      }
      words.add(new WordColorTuple(word1.original, "color" + colorcounter).toHtml());
    }
    html.append(Join.join(" ", words));
    html.append("</li>");

    html.append("<li value=\"" + w + "\">");
    Witness witness2 = colors.witnesses.get(w - 1);
    words = Lists.newArrayList();
    boolean lastWitnessWordWasAMatch = true;
    for (Word word2 : witness2.getWords()) {
      int color;
      boolean thisWitnessWordIsAMatch = matchedWitnessWordColors.containsKey(word2);
      if (thisWitnessWordIsAMatch) {
        color = matchedWitnessWordColors.get(word2);
      } else {
        if (lastWitnessWordWasAMatch) colorcounter++;
        color = colorcounter;
      }
      words.add(new WordColorTuple(word2.original, "color" + color).toHtml());
      lastWitnessWordWasAMatch = thisWitnessWordIsAMatch;
    }
    html.append(Join.join(" ", words));
    html.append("</li></ol></li>");

    return html.toString();
  }

  private String levenshteinMatch(LevenshteinMatch modification) {
    return "<i>" + modification.base() + "</i> matches with <i>" + modification.witness() + "</i>";
  }

  private String replacementView(Replacement replacement) {
    int position = replacement.getPosition();
    return "<i>" + replacement.getOriginalWords() + "</i> replaced by <i>" + replacement.getReplacementWords() + "</i> at position " + position; // TODO: TEMP!
  }

  private String transpositionView(Transposition transposition) {
    //    return "<i>" + "</i> transposed from position " + transposition. + " to " + y;
    return "<i>" + transposition.getLeft() + "</i> transposed with <i>" + transposition.getRight() + "</i>";
  }

  private String removalView(Removal removal) {
    int position = removal.getPosition();
    return "<i>" + removal.getRemovedWords() + "</i> at position " + (position) + " removed ";
  }

  private String additionView(Addition addition, int base) {
    Witness baseIndex = colors.getWitness(base);
    StringBuffer html = new StringBuffer("<i>" + addition.getAddedWords() + "</i> added ");
    List<Word> baseWords = baseIndex.getWords();
    int position = addition.getPosition();
    if (position == 1) {
      html.append("before <i>" + baseWords.get(0) + "</i>");
    } else if (position > baseWords.size()) {
      html.append(" after <i>" + baseWords.get(baseWords.size() - 1) + "</i>");
    } else {
      html.append("between <i>" + baseWords.get(position - 2) + "</i> and <i>" + baseWords.get(position - 1) + "</i>");
    }
    return html.toString();
  }
}
