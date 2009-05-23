package eu.interedition.collatex.superbase;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.sd_editions.collatex.permutations.CollateCore;
import com.sd_editions.collatex.permutations.Match;
import com.sd_editions.collatex.permutations.MatchNonMatch;
import com.sd_editions.collatex.permutations.NonMatch;
import com.sd_editions.collatex.permutations.Witness;
import com.sd_editions.collatex.permutations.Word;

public class SuperbaseAlgorithm {
  private final List<Witness> witnesses;

  // NOTE: instead of comparing each of the witnesses with
  // each other.. the solution chosen here is based on a
  // superbase. So that every witness is compared against
  // the super base which is constructed after each compare

  public SuperbaseAlgorithm(Witness... _witnesses) {
    this(Arrays.asList(_witnesses));
  }

  public SuperbaseAlgorithm(List<Witness> _witnesses) {
    this.witnesses = _witnesses;
  }

  public AlignmentTable2 createAlignmentTable() {
    // fill alignment table with words from the first
    // witness

    AlignmentTable2 table = new AlignmentTable2();
    if (witnesses.size() > 0) {
      Witness w1 = witnesses.get(0);
      table.addFirstWitness(w1);

      // do the first comparison
      Witness w2 = witnesses.get(1);
      addWitnessToAlignmentTable(table, w2);

      if (witnesses.size() > 2) {
        // do the second comparison
        Witness w3 = witnesses.get(2);
        addWitnessToAlignmentTable(table, w3);

        if (witnesses.size() > 3) {
          // do the third comparison
          Witness w4 = witnesses.get(3);
          addWitnessToAlignmentTable(table, w4);
        }
      }
    }
    return table;
  }

  private void addWitnessToAlignmentTable(AlignmentTable2 table, Witness witness) {
    // make the superbase from the alignment table
    Superbase superbase = table.createSuperbase();
    CollateCore core = new CollateCore();
    MatchNonMatch compresult = core.compareWitnesses(superbase, witness);

    Set<Match> matches = compresult.getMatches();
    for (Match match : matches) {
      Word baseWord = match.getBaseWord();
      Column column = superbase.getColumnFor(baseWord);
      Word witnessWord = match.getWitnessWord();
      table.addMatch(witness, witnessWord, column);
    }

    List<NonMatch> replacements = compresult.getReplacements();
    for (NonMatch replacement : replacements) {
      // TODO: hou rekening met langere additions!
      Word wordInOriginal = replacement.getBase().getFirstWord();
      Word wordInWitness = replacement.getWitness().getFirstWord();
      Column column = superbase.getColumnFor(wordInOriginal);
      table.addVariant(column, witness, wordInWitness);
    }

    List<NonMatch> additions = compresult.getAdditions();
    for (NonMatch addition : additions) {
      // NOTE: right now only the first word is taken
      // TODO: should work with the whole phrase
      Word firstWord = addition.getWitness().getFirstWord();

      if (addition.getBase().isAtTheEnd()) {
        table.addMatchAtTheEnd(witness, firstWord);
      } else {
        Word nextWord = addition.getBase().getNextWord();
        Column column = superbase.getColumnFor(nextWord);
        table.addMatchBefore(column, witness, firstWord);
      }
    }
  }
}
