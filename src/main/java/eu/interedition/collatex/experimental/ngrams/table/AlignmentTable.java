package eu.interedition.collatex.experimental.ngrams.table;

import java.util.List;

import com.sd_editions.collatex.match.views.AppElement;
import com.sd_editions.collatex.match.views.Element;
import com.sd_editions.collatex.match.views.TextElement;

import eu.interedition.collatex.experimental.ngrams.NGram;
import eu.interedition.collatex.experimental.ngrams.alignment.Alignment;
import eu.interedition.collatex.experimental.ngrams.alignment.ModificationVisitor;
import eu.interedition.collatex.visualization.Modifications;

public class AlignmentTable {

  private final Element[] cells;

  // NOTE: rename to tree? XML is a tree after all!
  // NOTE: rename to elements? A tree has elements after all!
  public AlignmentTable(final Modifications modifications) {
    cells = new Element[100]; // TODO: take longest witness?

    // NOTE: move this to Modifications?
    //    final Set<Match<Word>> matches = modifications.getMatches();
    //    cells = new Element[100]; // TODO: take longest witness?
    //    // NOTE: move this to ModificationVisitor?
    //    for (final Match<Word> match : matches) {
    //      final Word matchedWord = match.getBaseWord();
    //      cells[matchedWord.position * 2 - 1] = new TextElement(matchedWord);
    //    }
    //    // Note: move this to Modifications?
    //    final ModificationVisitor modificationVisitor = new ModificationVisitor(this);
    //    modifications.accept(modificationVisitor);
  }

  public AlignmentTable(final Alignment alignment) {
    final List<NGram> matches = alignment.getMatches();
    cells = new Element[100]; // TODO: take longest witness?
    // Note: move this to ModificationVisitor?
    for (final NGram match : matches) {
      cells[match.getFirstToken().getPosition() * 2 - 1] = new TextElement(match);
    }
    // Note: move this to Modifications?
    final ModificationVisitor modificationVisitor = new ModificationVisitor(this);
    alignment.accept(modificationVisitor);
  }

  // TODO: use a StringBuilder or Buffer instead of +=
  // TODO: should we store the whitespace in a Word?
  public String toXML() {
    String result = "<xml>";
    String whitespace = "";
    for (final Element element : cells) {
      if (element != null) {
        result += whitespace + element.toXML();
        whitespace = " ";
      }
    }
    result += "</xml>";
    return result;
  }

  public void setApp(final int i, final AppElement app) {
    cells[i] = app;
  }
}
