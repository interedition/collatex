package com.sd_editions.collatex.output;

import com.sd_editions.collatex.match.views.Element;
import com.sd_editions.collatex.match.views.TextElement;
import com.sd_editions.collatex.permutations.Match;
import com.sd_editions.collatex.permutations.MatchUnmatch;
import com.sd_editions.collatex.permutations.MisMatch;
import com.sd_editions.collatex.permutations.MultiMatch;
import com.sd_editions.collatex.permutations.MultiMatchUnmatch;
import com.sd_editions.collatex.permutations.Word;
import com.sd_editions.collatex.views.AppElementTEI;

public class AppAlignmentTable {

  private MatchUnmatch matchUnmatch;
  private final Element[] cells;
  private MultiMatchUnmatch multiMatchUnmatch;

  public AppAlignmentTable(MatchUnmatch _matchUnmatch) {
    // FIXME we should decide somewhere _which_ of the permutations we chose. 
    this.matchUnmatch = _matchUnmatch;
    cells = new Element[100]; // FIXME use maximum number of words per witness -- but need witness here

    for (Match match : matchUnmatch.getPermutation()) {
      Word matchedWord = match.getBaseWord();
      cells[matchedWord.position * 2 + 1] = new TextElement(matchedWord);
    }

    for (MisMatch unmatch : matchUnmatch.getUnmatches()) {
      // FIXME somehow propagate Bram's witness id here after merge
      cells[unmatch.getBase().getStartPosition() * 2] = new AppElementTEI(unmatch.getBase(), unmatch.getWitness());
    }

  }

  public AppAlignmentTable(MultiMatchUnmatch _multiMatchUnmatch) {
    // FIXME we should decide somewhere _which_ of the permutations we chose. 
    this.multiMatchUnmatch = _multiMatchUnmatch;
    cells = new Element[100]; // FIXME use maximum number of words per witness -- but need witness here

    for (MultiMatch mmatch : multiMatchUnmatch.getMatches()) {
      Word matchedWord = mmatch.getWords().get(0);
      cells[matchedWord.position * 2 + 1] = new TextElement(matchedWord);
    }

    for (MisMatch unmatch : multiMatchUnmatch.getUnmatches()) {
      cells[unmatch.getBase().getStartPosition() * 2] = new AppElementTEI(unmatch.getBase(), unmatch.getWitness());
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
