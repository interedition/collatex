package eu.interedition.collatex.alignment.multiple_witness;

import java.util.List;

import com.google.common.collect.Lists;
import com.sd_editions.collatex.match.Subsegment;

import eu.interedition.collatex.alignment.Match;
import eu.interedition.collatex.input.Phrase;
import eu.interedition.collatex.input.WitnessSegmentPhrases;

public class NewSuperbase extends WitnessSegmentPhrases {
  private final List<Column<Phrase>> _columns;

  public NewSuperbase() {
    super("superbase");
    _columns = Lists.newArrayList();
  }

  public static NewSuperbase create(final AlignmentTable2 table) {
    final NewSuperbase newS = new NewSuperbase();
    for (final Column<Phrase> column : table.getColumns()) {
      final List<Phrase> unique = column.getUniqueElements();
      for (final Phrase phrase : unique) {
        newS.addPhrase(phrase, column);
      }
    }
    return newS;
  }

  public void addPhrase(final Phrase phrase, final Column<Phrase> column) {
    // here we make a new phrase ... to set the positions, subsegment, and witnessID?!
    // we fake the length as 1
    final int position = getPhrases().size() + 1;
    final Subsegment subsegment = phrase.getSubsegment();
    final Phrase faked = new SuperbasePhrase(position, subsegment);
    getPhrases().add(faked);
    _columns.add(column);
  }

  public Column<Phrase> getColumnFor(final Match<Phrase> match) {
    return getColumnFor(match.getBaseWord());
  }

  public Column<Phrase> getColumnFor(final Phrase phraseA) {
    final int position = phraseA.getStartPosition();
    final Column<Phrase> column = _columns.get(position - 1);
    return column;
  }

  @Override
  public String toString() {
    String result = "Superbase: (";
    String delimiter = "";
    for (final Phrase p : getPhrases()) {
      result += delimiter + p.getSubsegment().getTitle();
      delimiter = ", ";
    }

    result += ")";
    return result;
  }

}
