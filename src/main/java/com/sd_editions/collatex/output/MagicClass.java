//package com.sd_editions.collatex.output;
//
//import com.sd_editions.collatex.permutations.CollateCore;
//import com.sd_editions.collatex.permutations.Match;
//import com.sd_editions.collatex.permutations.MatchNonMatch;
//import com.sd_editions.collatex.permutations.NonMatch;
//import com.sd_editions.collatex.permutations.Witness;
//import com.sd_editions.collatex.permutations.Word;
//
//public class MagicClass {
//
//  private final Witness[] witnesses;
//
//  public MagicClass(Witness... witnesses) {
//    this.witnesses = witnesses;
//  }
//
//  public MagicTable createAppAlignmentTable() {
//    // vul hier de alignment table met words witness1
//    // maak van de alignment table de superbase
//    // dan compare de witness 2 met de superbase
//    // voeg de matches en de additions toe aan de aligntable
//    // maak van de alignment table een super base
//    // compare de volgende witness etc
//
//    CollateCore core = new CollateCore();
//    Witness w1 = witnesses[0];
//    Witness w2 = witnesses[1];
//    MatchNonMatch matchNonMatch = core.compareWitnesses(w1, w2);
//    MagicTable table = new MagicTable();
//
//    for (Match match : matchNonMatch.getMatches()) {
//      Word matchedWord = match.getBaseWord();
//      table.setMatch(matchedWord);
//    }
//
//    for (NonMatch nonMatch : matchNonMatch.getNonMatches()) {
//      table.setNonMatch(nonMatch);
//    }
//
//    return table;
//  }
//
//}
