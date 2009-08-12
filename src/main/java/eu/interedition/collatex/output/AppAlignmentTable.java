package eu.interedition.collatex.output;

import java.util.List;

import com.google.common.collect.Lists;
import com.sd_editions.collatex.match.views.Element;

import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.Word;
import eu.interedition.collatex.superbase.AlignmentTable2;
import eu.interedition.collatex.superbase.Column;

public class AppAlignmentTable {

  private final AlignmentTable2 alignmentTable;
  private final List<Element> cells;

  public AppAlignmentTable(AlignmentTable2 _alignmentTable) {
    this.alignmentTable = _alignmentTable;
    this.cells = Lists.newArrayList();

    List<Column> columns = alignmentTable.getColumns();
    AppElementTEI mergedColumn = new AppElementTEI(null, null);
    cells.add(mergedColumn);
    // Column previousColumn = null; // Note: in the next step we have to compare two columns with each other
    for (Column column : columns) {
      List<Witness> witnesses = alignmentTable.getWitnesses();
      for (Witness witness : witnesses) {
        Word word = column.getWord(witness);
        mergedColumn.addWord(witness, word);
      }
    }
  }

  //  public AppAlignmentTable(Collation _matchNonMatch) {
  //    // FIXME we should decide somewhere _which_ of the permutations we chose. 
  //    this.matchNonMatch = _matchNonMatch;
  //    cells = new Element[100]; // FIXME use maximum number of words per witness -- but need witness here
  //
  //    for (Match match : matchNonMatch.getMatches()) {
  //      Word matchedWord = match.getBaseWord();
  //      cells[matchedWord.position * 2 + 1] = new TextElement(matchedWord);
  //    }
  //
  //    for (Gap nonMatch : matchNonMatch.getGaps()) {
  //      // FIXME somehow propagate Bram's witness id here after merge
  //      cells[nonMatch.getPhraseA().getStartPosition() * 2] = new AppElementTEI(nonMatch.getPhraseA(), nonMatch.getPhraseB());
  //    }
  //
  //  }

  public String toXML() {

    StringBuilder result = new StringBuilder(); // FIXME initialize length
    result.append("<collation>");

    for (Element cell : cells) {
      if (cell != null) {
        result.append(cell.toXML());
        // result.append(' '); // FIXME can we just introduce whitespace here!? 
      }
    }
    //    result.deleteCharAt(result.length() - 1); // Note: this line here is beacause of the always added ' '

    result.append("</collation>");

    return result.toString();
  }

}
