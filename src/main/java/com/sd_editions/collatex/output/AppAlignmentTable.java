package com.sd_editions.collatex.output;

import com.sd_editions.collatex.match.views.Element;
import com.sd_editions.collatex.match.views.TextElement;
import com.sd_editions.collatex.permutations.MultiMatch;
import com.sd_editions.collatex.permutations.MultiMatchNonMatch;
import com.sd_editions.collatex.views.AppElementTEI;

import eu.interedition.collatex.collation.Collation;
import eu.interedition.collatex.collation.alignment.Match;
import eu.interedition.collatex.collation.gaps.Gap;
import eu.interedition.collatex.input.Word;

public class AppAlignmentTable {

  private Collation matchNonMatch;
  private final Element[] cells;
  private MultiMatchNonMatch multiMatchNonMatch;

  public AppAlignmentTable(Collation _matchNonMatch) {
    // FIXME we should decide somewhere _which_ of the permutations we chose. 
    this.matchNonMatch = _matchNonMatch;
    cells = new Element[100]; // FIXME use maximum number of words per witness -- but need witness here

    for (Match match : matchNonMatch.getMatches()) {
      Word matchedWord = match.getBaseWord();
      cells[matchedWord.position * 2 + 1] = new TextElement(matchedWord);
    }

    for (Gap nonMatch : matchNonMatch.getGaps()) {
      // FIXME somehow propagate Bram's witness id here after merge
      cells[nonMatch.getBase().getStartPosition() * 2] = new AppElementTEI(nonMatch.getBase(), nonMatch.getWitness());
    }

  }

  public AppAlignmentTable(MultiMatchNonMatch _multiMatchNonMatch) {
    // FIXME we should decide somewhere _which_ of the permutations we chose. 
    this.multiMatchNonMatch = _multiMatchNonMatch;
    cells = new Element[100]; // FIXME use maximum number of words per witness -- but need witness here

    for (MultiMatch mmatch : multiMatchNonMatch.getMatches()) {
      Word matchedWord = mmatch.getWords().get(0);
      cells[matchedWord.position * 2 + 1] = new TextElement(matchedWord);
    }

    for (Gap nonMatch : multiMatchNonMatch.getNonMatches()) {
      cells[nonMatch.getBase().getStartPosition() * 2] = new AppElementTEI(nonMatch.getBase(), nonMatch.getWitness());
    }

  }

  public String toXML() {

    StringBuilder result = new StringBuilder(); // FIXME initialize length
    result.append("<collation>");

    for (Element cell : cells) {
      if (cell != null) {
        result.append(cell.toXML());
        result.append(' '); // FIXME can we just introduce whitespace here!? 
      }
    }
    result.deleteCharAt(result.length() - 1);

    result.append("</collation>");

    return result.toString();
  }

}
