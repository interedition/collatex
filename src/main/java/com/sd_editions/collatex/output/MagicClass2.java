package com.sd_editions.collatex.output;

import java.util.Arrays;
import java.util.List;

import com.sd_editions.collatex.permutations.CollateCore;
import com.sd_editions.collatex.permutations.MatchNonMatch;
import com.sd_editions.collatex.permutations.Witness;

public class MagicClass2 {
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

  public MagicClass2(Witness... _witnesses) {
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
    Witness superbase = table.createSuperBase();

    // do the first comparison
    Witness w2 = witnesses.get(1);

    CollateCore core = new CollateCore();
    MatchNonMatch compresult = core.compareWitnesses(w1, w2);
    System.out.println(compresult.getMatches());
    // eerst addition verwerken
    // daarna matches 
    // noteer in metadata column match, near match, variants
    return table;
  }
}
