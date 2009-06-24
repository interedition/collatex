package com.sd_editions.collatex.Web;

import java.util.List;
import java.util.Map;

import com.google.common.base.Join;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sd_editions.collatex.match.WordColorTuple;
import com.sd_editions.collatex.permutations.WordDistanceMatch;
import com.sd_editions.collatex.permutations.collate.Addition;
import com.sd_editions.collatex.permutations.collate.Omission;
import com.sd_editions.collatex.permutations.collate.Replacement;
import com.sd_editions.collatex.permutations.collate.Transposition;

import eu.interedition.collatex.collation.CollateCore;
import eu.interedition.collatex.collation.Match;
import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.Word;
import eu.interedition.collatex.matching.Collation;
import eu.interedition.collatex.matching.Matcher;
import eu.interedition.collatex.superbase.AlignmentTable2;
import eu.interedition.collatex.superbase.Column;
import eu.interedition.collatex.superbase.SuperbaseAlgorithm;
import eu.interedition.collatex.visualization.Modification;
import eu.interedition.collatex.visualization.Modifications;
import eu.interedition.collatex.visualization.Visualization;

public class ColorsView {

  private final CollateCore colors;

  private final List<String> messages;

  private final List<Witness> witnesses;

  public ColorsView(List<String> _messages, List<Witness> _witnesses) {
    this.witnesses = _witnesses;
    this.colors = new CollateCore(_witnesses); // this is legacy!
    this.messages = _messages;
  }

  public ColorsView(Witness... _witnesses) {
    this.witnesses = Lists.newArrayList(_witnesses);
    this.colors = new CollateCore(_witnesses); // this is legacy!
    this.messages = Lists.newArrayList();
  }

  public String toHtml() {
    return messages() + witnesses() + alignment() + modifications();
  }

  private String alignment() {
    SuperbaseAlgorithm algorithm = new SuperbaseAlgorithm(witnesses);
    AlignmentTable2 alignmentTable = algorithm.createAlignmentTable();

    StringBuilder tableHTML = new StringBuilder("<div id=\"alignment-table\"><h4>Alignment Table:</h4>\n<table class=\"alignment\">\n");

    for (Witness witness : witnesses) {
      tableHTML.append("<tr>");
      tableHTML.append("<th>Witness ").append(witness.id).append(":</th>");
      for (Column column : alignmentTable.getColumns()) {
        tableHTML.append("<td>");
        if (column.containsWitness(witness)) {
          tableHTML.append(column.getWord(witness));
        }
        tableHTML.append("</td>");
      }
      tableHTML.append("</tr>\n");
    }
    tableHTML.append("</table>\n</div>\n\n");
    //    return alignmentTable.toString().replaceAll("\n", "<br/>") + "<br/>";
    return tableHTML.toString();
  }

  private String messages() {
    if (messages.isEmpty()) return "";
    StringBuffer buffer = new StringBuffer();
    buffer.append("<h4>Messages:</h4>");
    for (String msg : messages) {
      buffer.append(msg + "<br/>");
    }
    return buffer.toString();
  }

  private String witnesses() {
    StringBuffer html = new StringBuffer("<h4>Witnesses:</h4>");
    for (int row = 0; row < colors.numberOfWitnesses(); row++) {
      html.append("<b>Witness " + (row + 1) + "</b>: " + colors.witnesses.get(row).sentence + "<br/>");
    }
    html.append("<br/>");
    return html.toString();
  }

  private String modifications() {
    StringBuffer html = new StringBuffer("<h4>Modifications:</h4>");
    final int numberOfWitnesses = witnesses.size();
    for (int base = 1; base < numberOfWitnesses; base++) {
      for (int w = base + 1; w <= numberOfWitnesses; w++) {
        html.append("Comparing witness " + base + " - witness " + (w) + ":");
        Matcher matcher = new Matcher();
        Witness a = witnesses.get(base - 1);
        Witness b = witnesses.get(w - 1);
        Collation collate = matcher.collate(a, b);
        Modifications modifications = Visualization.getModifications(collate);
        html.append(modificationsView(base, modifications));
        html.append("<br/>");
      }
    }
    return html.toString();
  }

  //  private String modifications() {
  //    StringBuffer html = new StringBuffer("<h4>Modifications:</h4>");
  //    MatchesView matchesView = new MatchesView();
  //    final int numberOfWitnesses = colors.numberOfWitnesses();
  //    for (int base = 1; base < numberOfWitnesses; base++) {
  //      if (base > 1) html.append("<span class=\"secondary\">");
  //      for (int w = base + 1; w <= numberOfWitnesses; w++) {
  //        html.append("Comparing witness " + base + " - witness " + (w) + ":<ol>");
  //        List<MatchNonMatch> matchNonMatchList = colors.doCompareWitnesses(colors.getWitness(base), colors.getWitness(w));
  //        int pn = 1;
  //        for (MatchNonMatch matchNonMatch : matchNonMatchList) {
  //          if (pn > 1) html.append("<span class=\"secondary\">");
  //          html.append("<span class=\"secondary\">Permutation " + pn++ + "/" + matchNonMatchList.size() + "</span><ul>");
  //          html.append("<span class=\"colored\" style=\"display:none\">");
  //          Modifications modifications = colors.getModifications(matchNonMatch);
  //          html.append(witnessPairView(base, w, modifications));
  //          html.append("</span>");
  //          html.append(modificationsView(base, modifications));
  //          html.append("<br/></ul>");
  //          html.append(matchesView.renderPermutation(matchNonMatch));
  //          if (pn > 1) html.append("</span>");
  //        }
  //        html.append("</ol>");
  //      }
  //      if (base > 1) html.append("</span>");
  //    }
  //    return html.toString();
  //  }

  private String modificationsView(int base, Modifications modifications) {
    StringBuffer html = new StringBuffer("<span class=\"secondary\"><li>Modifications:</li></span><ul>");
    List<Modification> modificationsL = modifications.getModifications();
    if (modificationsL.isEmpty()) {
      html.append("<li>no additions, omissions or transpositions</li>");
    } else {
      for (Modification modification : modificationsL) {
        if (modification instanceof WordDistanceMatch) {
          html.append("<li>" + wordDistanceMatch((WordDistanceMatch) modification) + "</li>");
        } else if (modification instanceof Addition) {
          html.append("<li>" + additionView((Addition) modification, base) + "</li>");
        } else if (modification instanceof Omission) {
          html.append("<li>" + removalView((Omission) modification) + "</li>");
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

  private String wordDistanceMatch(WordDistanceMatch modification) {
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

  private String removalView(Omission removal) {
    int position = removal.getPosition();
    return "<i>" + removal.getOmittedWords() + "</i> at position " + (position) + " removed ";
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
