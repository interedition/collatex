package com.sd_editions.collatex.output;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.sd_editions.collatex.permutations.CollateCore;
import com.sd_editions.collatex.permutations.Match;
import com.sd_editions.collatex.permutations.MatchNonMatch;
import com.sd_editions.collatex.permutations.NonMatch;
import com.sd_editions.collatex.permutations.Superbase;
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
    this.witnesses = Arrays.asList(_witnesses);
  }

  public AlignmentTable2 createAlignmentTable() {
    // fill alignment table with words from the first
    // witness

    AlignmentTable2 table;
    table = new AlignmentTable2();
    Witness w1 = witnesses.get(0);
    table.addFirstWitness(w1);
    // make the superbase from the alignment table
    Superbase superbase = table.createSuperbase();

    // do the first comparison
    Witness w2 = witnesses.get(1);

    CollateCore core = new CollateCore();
    MatchNonMatch compresult = core.compareWitnesses(superbase, w2);
    System.out.println(compresult.getMatches());
    addExtraWitnessToAlignmentTable(table, compresult, superbase, w2);

    // do the second comparison
    Witness w3 = witnesses.get(2);

    compresult = core.compareWitnesses(superbase, w3);
    System.out.println(compresult.getMatches());
    addExtraWitnessToAlignmentTable(table, compresult, superbase, w3);

    // eerst addition verwerken
    // daarna matches 
    // noteer in metadata column match, near match, variants
    return table;
  }

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
    // TODO: i need a method for the replacements
    List<NonMatch> replacements = Lists.newArrayList();
    for (NonMatch nonMatch : compresult.getNonMatches()) {
      if (nonMatch.isReplacement()) {
        replacements.add(nonMatch);
      }
    }
    // TODO: hou rekening met additions aan het begin!

    // Note: Damn Ik will bij een Gap eigenlijk weten
    // welke match er voor en er na komt,
    // zodat ik weet na of voor of tussen welke columns
    // ik de variants moet plaatsen
    for (NonMatch replacement : replacements) {
      // Note: wacht variants de eerste versie 
      // zal gewoon op genomen zijn in de superbase!
      // TODO: hou rekening met langere additions!
      Word wordInOriginal = replacement.getBase().getFirstWord();
      Word wordInWitness = replacement.getWitness().getFirstWord();
      Column column = superbase.getColumnFor(wordInOriginal);
      table.addVariant(column, witness, wordInWitness);
    }
  }
  // NOTE: THIS WAS THE OLD METHOD MEANT FOR ADDITIONS!
  //  private void addExtraWitnessToAlignmentTable(AlignmentTable2 table, MatchNonMatch compresult) {
  //    List<NonMatch> nonMatches = compresult.getNonMatches();
  //    // I need a method that only returns additions
  //    List<NonMatch> additions = Lists.newArrayList();
  //    for (NonMatch nonMatch : nonMatches) {
  //      if (nonMatch.isAddition()) {
  //        additions.add(nonMatch);
  //      }
  //    }
  //    // TODO: the position stuff is probably wrong!
  //    // I need to know between which matches this addition occurs!
  //    // It should be possible to store that information
  //    // when the detection occurs
  //    // there should also be a methods that gives me the 
  //    // individual words of a phrase
  //    // And I should remember which column each of the 
  //    // words of the superbase belongs to
  //    for (NonMatch addition : additions) {
  //      Gap witness = addition.getWitness();
  //      //      for (Word word : witness.g) {
  //      //        
  //      //      }
  //    }
  //    // TODO Auto-generated method stub
  //
  //  }
}
