package eu.interedition.collatex.superbase;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Set;

import org.junit.Test;

import eu.interedition.collatex.collation.CollateCore;
import eu.interedition.collatex.collation.Match;
import eu.interedition.collatex.collation.gaps.Gap;
import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.WitnessBuilder;
import eu.interedition.collatex.input.Word;
import eu.interedition.collatex.matching.Collation;

public class AlignmentTable2Test {
  @Test
  public void testCreateSuperBase() {
    WitnessBuilder builder = new WitnessBuilder();
    Witness a = builder.build("A", "the first witness");
    AlignmentTable2 alignmentTable = new AlignmentTable2();
    alignmentTable.addFirstWitness(a);
    Witness superbase = alignmentTable.createSuperbase();
    assertEquals("the first witness", superbase.toString());
  }

  @Test
  public void testCreateSuperBaseWithVariation() {
    WitnessBuilder builder = new WitnessBuilder();
    Witness a = builder.build("A", "the first witness");
    Witness b = builder.build("B", "the second witness");
    AlignmentTable2 alignmentTable = new AlignmentTable2();
    alignmentTable.addFirstWitness(a);
    alignmentTable.addWitness(b);
    Witness superbase = alignmentTable.createSuperbase();
    assertEquals("the first second witness", superbase.toString());
  }

  @Test
  public void testStringOutputOneWitness() {
    WitnessBuilder builder = new WitnessBuilder();
    Witness w1 = builder.build("A", "the black cat");
    AlignmentTable2 table = new AlignmentTable2();
    table.addFirstWitness(w1);
    String expected = "A: the|black|cat\n";
    assertEquals(expected, table.toString());
  }

  @Test
  public void testStringOutputTwoWitnesses() {
    WitnessBuilder builder = new WitnessBuilder();
    Witness w1 = builder.build("A", "the black cat");
    Witness w2 = builder.build("B", "the black cat");
    AlignmentTable2 table = new AlignmentTable2();
    table.addFirstWitness(w1);
    // TODO: word contains id also, which refers to Witness
    Column c1 = table.getColumns().get(0);
    Column c2 = table.getColumns().get(1);
    Column c3 = table.getColumns().get(2);
    table.addMatch(w2, w2.getWordOnPosition(1), c1);
    table.addMatch(w2, w2.getWordOnPosition(2), c2);
    table.addMatch(w2, w2.getWordOnPosition(3), c3);
    String expected = "A: the|black|cat\n";
    expected += "B: the|black|cat\n";
    assertEquals(expected, table.toString());
  }

  @Test
  public void testStringOutputEmptyCells() {
    WitnessBuilder builder = new WitnessBuilder();
    Witness w1 = builder.build("A", "the black cat");
    Witness w2 = builder.build("B", "the");
    AlignmentTable2 table = new AlignmentTable2();
    table.addFirstWitness(w1);
    Column column = table.getColumns().get(0);
    // TODO: word contains id also, which refers to Witness
    table.addMatch(w2, w2.getWordOnPosition(1), column);
    String expected = "A: the|black|cat\n";
    expected += "B: the| | \n";
    assertEquals(expected, table.toString());
  }

  private static void addWitnessToAlignmentTable(AlignmentTable2 table, Witness witness) {
    // make the superbase from the alignment table
    Superbase superbase = table.createSuperbase();
    CollateCore core = new CollateCore();
    Collation compresult = core.compareWitnesses(superbase, witness);

    Set<Match> matches = compresult.getMatches();
    for (Match match : matches) {
      Word baseWord = match.getBaseWord();
      Column column = superbase.getColumnFor(baseWord);
      Word witnessWord = match.getWitnessWord();
      table.addMatch(witness, witnessWord, column);
    }

    List<Gap> replacements = compresult.getReplacements();
    for (Gap replacement : replacements) {
      // TODO: hou rekening met langere additions!
      Word wordInOriginal = replacement.getBase().getFirstWord();
      Word wordInWitness = replacement.getWitness().getFirstWord(); // if witness is longer -> extra columns
      Column column = superbase.getColumnFor(wordInOriginal);
      table.addVariant(column, witness, wordInWitness);
    }

    List<Gap> additions = compresult.getAdditions();
    for (Gap addition : additions) {
      // NOTE: right now only the first word is taken
      // TODO: should work with the whole phrase 
      Word firstWord = addition.getWitness().getFirstWord();

      if (addition.getBase().isAtTheEnd()) {
        table.addVariantAtTheEnd(witness, addition.getWitness().getWords());
      } else {
        Word nextWord = addition.getBase().getNextWord();
        Column column = superbase.getColumnFor(nextWord);
        table.addVariantBefore(column, witness, addition.getBase().getWords());
      }
    }
  }
}
