package eu.interedition.collatex.alignment.multiple_witness;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import eu.interedition.collatex.alignment.Alignment;
import eu.interedition.collatex.alignment.Gap;
import eu.interedition.collatex.alignment.Match;
import eu.interedition.collatex.alignment.multiple_witness.visitors.IAlignmentTableVisitor;
import eu.interedition.collatex.collation.CollateCore;
import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.Word;
import eu.interedition.collatex.parallel_segmentation.TeiParallelSegmentationTable;

// Note: for the TEI XML output it is easier to
// have a Column be a list<phrase>
// However, for building the alignment table it
// is easier to have a Column be a list<word>

// NOTE: instead of comparing each of the witnesses with
// each other.. the solution chosen here is based on a
// superbase. So that every witness is compared against
// the super base which is constructed after each compare

public class AlignmentTable2 {
  private final List<Column> columns;
  private final List<Witness> witnesses;

  public AlignmentTable2() {
    this.columns = Lists.newArrayList();
    this.witnesses = Lists.newArrayList();
  }

  public void add(Column column) {
    columns.add(column);
  }

  public void addVariantBefore(Column column, List<Word> witnessWords) {
    int indexOf = columns.indexOf(column);
    if (indexOf == -1) {
      throw new RuntimeException("Unexpected error: Column not found!");
    }

    for (Word word : witnessWords) {
      Column extraColumn = new Column(word);
      columns.add(indexOf, extraColumn);
      indexOf++;
    }
  }

  public void addVariantAtTheEnd(List<Word> witnessWords) {
    for (Word word : witnessWords) {
      Column extraColumn = new Column(word);
      columns.add(extraColumn);
    }
  }

  public Superbase createSuperbase() {
    Superbase superbase = new Superbase();
    for (Column column : columns) {
      column.addToSuperbase(superbase);
    }
    return superbase;
  }

  public void addWitness(Witness witness) {
    if (witnesses.isEmpty()) {
      for (Word word : witness.getWords()) {
        add(new Column(word));
      }
      witnesses.add(witness);
      return;
    }

    addWitnessToInternalList(witness);

    // make the superbase from the alignment table
    Superbase superbase = createSuperbase();
    Alignment compresult = CollateCore.collate(superbase, witness);

    addMatchesToAlignmentTable(superbase, compresult);
    addReplacementsToAlignmentTable(witness, superbase, compresult);
    addAdditionsToAlignmentTable(superbase, compresult);
  }

  public List<Column> getColumns() {
    return columns;
  }

  public List<Witness> getWitnesses() {
    return witnesses;
  }

  // TODO: move this to a visitor!
  // TODO: separate in two steps: segmentation and xml rendering
  public String toXML() {
    TeiParallelSegmentationTable app = new TeiParallelSegmentationTable(this);
    return app.toXML();
  }

  @Override
  public String toString() {
    String collectedStrings = "";
    for (Witness witness : witnesses) {
      collectedStrings += witness.id + ": ";
      String delim = "";
      for (Column column : columns) {
        collectedStrings += delim + cellToString(witness, column);
        delim = "|";
      }
      collectedStrings += "\n";
    }
    return collectedStrings;
  }

  private String cellToString(Witness witness, Column column) {
    if (!column.containsWitness(witness)) {
      return " ";
    }
    return column.getWord(witness).toString();
  }

  private void addWitnessToInternalList(Witness witness) {
    // TODO: an ordered set instead of list would be nice here
    if (!witnesses.contains(witness)) {
      witnesses.add(witness);
    }
  }

  private void addAdditionsToAlignmentTable(Superbase superbase, Alignment compresult) {
    List<Gap> additions = compresult.getAdditions();
    for (Gap addition : additions) {
      List<Word> witnessWords = addition.getPhraseB().getWords();
      addVariantAtGap(superbase, addition, witnessWords);
    }
  }

  // TODO: addReplacements.. should look like addAdditions method!
  private void addReplacementsToAlignmentTable(Witness witness, Superbase superbase, Alignment compresult) {
    List<Gap> replacements = compresult.getReplacements();
    for (Gap replacement : replacements) {
      // TODO: hou rekening met langere additions!

      Iterator<Word> baseIterator = replacement.getPhraseA().getWords().iterator();
      Iterator<Word> witnessIterator = replacement.getPhraseB().getWords().iterator();
      while (baseIterator.hasNext()) {
        Word wordInOriginal = baseIterator.next();
        Column column = superbase.getColumnFor(wordInOriginal);
        if (witnessIterator.hasNext()) {
          Word wordInWitness = witnessIterator.next();
          if (column.containsWitness(witness)) { // already have something in here from the matches phase
            addVariantBefore(column, Lists.newArrayList(wordInWitness)); // FIXME but this doesn't handle longer sequences ...
          } else {
            column.addVariant(wordInWitness);
          }
        }
      }
      // still have words in the witness? add new columns after the last one from the base
      if (witnessIterator.hasNext()) {
        LinkedList<Word> remainingWitnessWords = Lists.newLinkedList(witnessIterator);
        addVariantAtGap(superbase, replacement, remainingWitnessWords);
      }
    }
  }

  private void addMatchesToAlignmentTable(Superbase superbase, Alignment compresult) {
    Set<Match> matches = compresult.getMatches();
    for (Match match : matches) {
      Column column = superbase.getColumnFor(match);
      Word witnessWord = match.getWitnessWord();
      column.addMatch(witnessWord);
    }
  }

  private void addVariantAtGap(Superbase superbase, Gap gap, List<Word> witnessWords) {
    if (gap.getPhraseA().isAtTheEnd()) {
      addVariantAtTheEnd(witnessWords);
    } else {
      Match nextMatch = gap.getNextMatch();
      Column column = superbase.getColumnFor(nextMatch);
      addVariantBefore(column, witnessWords);
    }
  }

  // TODO: add visitor who walks over the witnesses
  // Note: this is a visitor who walks over the columns!
  public void accept(IAlignmentTableVisitor visitor) {
    visitor.visitTable(this);
    for (Column column : columns) {
      column.accept(visitor);
    }
    visitor.postVisitTable(this);
  }

  // TODO: move this functionalitity to a visitor!
  public static String alignmentTableToHTML(AlignmentTable2 alignmentTable) {
    StringBuilder tableHTML = new StringBuilder("<div id=\"alignment-table\"><h4>Alignment Table:</h4>\n<table class=\"alignment\">\n");

    for (Witness witness : alignmentTable.getWitnesses()) {
      tableHTML.append("<tr>");
      tableHTML.append("<th>Witness ").append(witness.id).append(":</th>");
      for (Column column : alignmentTable.getColumns()) {
        tableHTML.append("<td>");
        if (column.containsWitness(witness)) {
          tableHTML.append(column.getWord(witness).normalized); // TODO: add escaping!
        }
        tableHTML.append("</td>");
      }
      tableHTML.append("</tr>\n");
    }
    tableHTML.append("</table>\n</div>\n\n");
    //    return alignmentTable.toString().replaceAll("\n", "<br/>") + "<br/>";
    return tableHTML.toString();
  }
}
