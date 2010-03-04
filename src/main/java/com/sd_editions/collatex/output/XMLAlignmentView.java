package com.sd_editions.collatex.output;

import java.util.List;

import com.sd_editions.collatex.permutations.WordDistanceMatch;
import com.sd_editions.collatex.permutations.collate.Transposition;

import eu.interedition.collatex.experimental.ngrams.alignment.Addition;
import eu.interedition.collatex.experimental.ngrams.alignment.Omission;
import eu.interedition.collatex.experimental.ngrams.alignment.Replacement;
import eu.interedition.collatex.visualization.Modification;
import eu.interedition.collatex.visualization.Modifications;

public class XMLAlignmentView {

  private final Modifications modifications;

  public XMLAlignmentView(Modifications modifications) {
    super();
    this.modifications = modifications;
  }

  String modificationsView(int base) {
    StringBuffer xml = new StringBuffer("<modifications>");
    List<Modification> modificationsL = modifications.getModifications();
    if (!modificationsL.isEmpty()) {
      for (Modification modification : modificationsL) {
        if (modification instanceof WordDistanceMatch) {
          xml.append("<li>" + wordDistanceMatch((WordDistanceMatch) modification) + "</li>");
        } else if (modification instanceof Addition) {
          xml.append(additionView((Addition) modification, base));
        } else if (modification instanceof Omission) {
          xml.append(removalView((Omission) modification));
        } else if (modification instanceof Transposition) {
          xml.append("<li>" + transpositionView((Transposition) modification) + "</li>");
        } else if (modification instanceof Replacement) {
          xml.append("<li>" + replacementView((Replacement) modification) + "</li>");
        }
      }
    } else {}
    xml.append("</modifications>");
    return xml.toString();
  }

  private String replacementView(Replacement modification) {
    // TODO Auto-generated method stub
    return null;
  }

  private String transpositionView(Transposition modification) {
    // TODO Auto-generated method stub
    return null;
  }

  private String removalView(Omission modification) {
    return "<omission position=\"" + modification.getPosition() + "\">" + modification.getOmittedWords() + "</omission>";
  }

  private String additionView(Addition modification, int base) {
    return "<addition position=\"" + modification.getPosition() + "\">" + modification.getAddedWords() + "</addition>";
  }

  private String wordDistanceMatch(WordDistanceMatch modification) {
    // TODO Auto-generated method stub
    return null;
  }

  public Modifications getModifications() {
    return modifications;
  }

}
