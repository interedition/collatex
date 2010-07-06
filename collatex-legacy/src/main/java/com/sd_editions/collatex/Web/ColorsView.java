/**
 * CollateX - a Java library for collating textual sources,
 * for example, to produce an apparatus.
 *
 * Copyright (C) 2010 ESF COST Action "Interedition".
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.sd_editions.collatex.Web;

import java.util.List;

import com.google.common.collect.Lists;
import com.sd_editions.collatex.permutations.WordDistanceMatch;
import com.sd_editions.collatex.permutations.collate.Transposition;

import eu.interedition.collatex.alignment.Alignment;
import eu.interedition.collatex.alignment.functions.Aligner;
import eu.interedition.collatex.alignment.multiple_witness.AlignmentTable2;
import eu.interedition.collatex.alignment.multiple_witness.AlignmentTableCreator;
import eu.interedition.collatex.collation.CollateCore;
import eu.interedition.collatex.experimental.ngrams.alignment.Modification;
import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.WitnessSet;
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
    WitnessSet set = new WitnessSet(witnesses);
    AlignmentTable2 alignmentTable = AlignmentTableCreator.createAlignmentTable(set);
    return AlignmentTable2.alignmentTableToHTML(alignmentTable);
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
      html.append("<b>Witness " + (row + 1) + "</b>: " + colors.witnesses.get(row).toString() + "<br/>");
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
        new Aligner();
        Witness a = witnesses.get(base - 1);
        Witness b = witnesses.get(w - 1);
        Alignment align = Aligner.align(a.getFirstSegment(), b.getFirstSegment());
        Modifications modifications = Visualization.getModifications(align);
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
//        } else if (modification instanceof Addition) {
//          html.append("<li>" + additionView((Addition) modification, base) + "</li>");
//        } else if (modification instanceof Omission) {
//          html.append("<li>" + removalView((Omission) modification) + "</li>");
        } else if (modification instanceof Transposition) {
          html.append("<li>" + transpositionView((Transposition) modification) + "</li>");
//        } else if (modification instanceof Replacement) {
//          html.append("<li>" + replacementView((Replacement) modification) + "</li>");
       }
      }
    }
    html.append("</ul>");
    return html.toString();
  }

  private String wordDistanceMatch(WordDistanceMatch modification) {
    return "<i>" + modification.base() + "</i> matches with <i>" + modification.witness() + "</i>";
  }

  private String transpositionView(Transposition transposition) {
    //    return "<i>" + "</i> transposed from position " + transposition. + " to " + y;
    return "<i>" + transposition.getLeft() + "</i> transposed with <i>" + transposition.getRight() + "</i>";
  }


}
