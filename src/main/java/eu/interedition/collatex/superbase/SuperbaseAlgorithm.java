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

  // NOTE: aargh je kunt pas betalen hoe breed de columns zijn
  // aan het eind
  // net zo goed als dat je pas aan het eind weet
  // of het een gewone tekst is of een app (hoogte)
  // of anders moeten we phrases gaan opsplitsen
  // dan is lastig want dan moet je dingen gaan
  // bijhouden 

  public SuperbaseAlgorithm(Witness... _witnesses) {
    this(Arrays.asList(_witnesses));
  }

  public SuperbaseAlgorithm(List<Witness> _witnesses) {
    this.witnesses = _witnesses;
  }

  public AlignmentTable2 createAlignmentTable() {
    // fill alignment table with words from the first
    // witness

    AlignmentTable2 table;
    table = new AlignmentTable2();
    if (witnesses.size() > 0) {
      Witness w1 = witnesses.get(0);
      table.addFirstWitness(w1);
      // make the superbase from the alignment table
      Superbase superbase = table.createSuperbase();

      // do the first comparison
      Witness w2 = witnesses.get(1);

      CollateCore core = new CollateCore();
      MatchNonMatch compresult = core.compareWitnesses(superbase, w2);
      addExtraWitnessToAlignmentTable(table, compresult, superbase, w2);

      if (witnesses.size() > 2) {
        // do the second comparison
        Witness w3 = witnesses.get(2);

        compresult = core.compareWitnesses(superbase, w3);
        addExtraWitnessToAlignmentTable(table, compresult, superbase, w3);

        if (witnesses.size() > 3) {
          // do the third comparison
          Witness w4 = witnesses.get(3);

          compresult = core.compareWitnesses(superbase, w4);
          addExtraWitnessToAlignmentTable(table, compresult, superbase, w4);
        }
      }
    }
    // TODO: noteer in metadata column match, near match, variants
    return table;
  }

  // NOTE: maybe the modification classes should be
  // NOTE: updated to make them like a wrapper
  // NOTE: around the nonmatch class
  private void addExtraWitnessToAlignmentTable(AlignmentTable2 table, MatchNonMatch compresult, Superbase superbase, Witness witness) {
    Set<Match> matches = compresult.getMatches();
    for (Match match : matches) {
      Word baseWord = match.getBaseWord();
      Column column = superbase.getColumnFor(baseWord);
      Word witnessWord = match.getWitnessWord();
      // Note: go through table here or direct through column?
      // columnFor.addMatch(witness, witnessWord);
      // if you go directly the table is not notified about
      // the fact that a witness is added
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
      if (addition.getBase().isAtTheEnd()) {
        // NOTE: right now only the first word is taken
        // TODO: should work with the whole phrase
        Word firstWord = addition.getWitness().getFirstWord();
        table.addMatchAtTheEnd(witness, firstWord);
      } else {
        Word nextWord = addition.getBase().getNextWord();
        Column column = superbase.getColumnFor(nextWord);
        // NOTE: right now only the first word is taken
        // TODO: should work with the whole phrase
        Word firstWord = addition.getWitness().getFirstWord();
        table.addMatchBefore(column, witness, firstWord);
      }
    }
  }

}
