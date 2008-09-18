package com.sd_editions.collatex.spike2;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

public class MatchSequences {
  public static List<Modification> getModificationsInMatchSequences(Witness base, Witness witness, List<MatchSequence> sequences) {
    List<Modification> results = Lists.newArrayList();
    for (MatchSequence sequence : sequences) {
      List<Match> matches = sequence.getMatches();
      if (matches.size() > 1) {
        Iterator<Match> i = matches.iterator();
        Match previous = i.next();
        while (i.hasNext()) {
          Match next = i.next();
          int baseStartPosition = previous.getBaseWord().position;
          int baseEndPosition = next.getBaseWord().position;
          int witnessStartPosition = previous.getWitnessWord().position;
          int witnessEndPosition = next.getWitnessWord().position;
          int diffBase = baseEndPosition - baseStartPosition;
          int diffWitness = witnessEndPosition - witnessStartPosition;
          if (diffBase != 0 || diffWitness != 0) {
            Gap gapBase = new Gap(base, diffBase, baseStartPosition + 1, baseEndPosition - 1);
            Gap gapWitness = new Gap(witness, diffWitness, witnessStartPosition + 1, witnessEndPosition - 1);
            MisMatch misMatch = new MisMatch(gapBase, gapWitness);
            System.out.println(misMatch.isAddition());
            System.out.println(misMatch.isOmission());
            System.out.println(misMatch.isReplacement());
            Modification modification = misMatch.analyse();
            results.add(modification);
          }
          previous = next;
        }
      }
    }
    return results;
  }

}
