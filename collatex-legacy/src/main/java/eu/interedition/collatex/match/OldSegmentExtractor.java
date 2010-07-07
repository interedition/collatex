/**
 * CollateX - a Java library for collating textual sources,
 * for example, to produce an apparatus.
 *
 * Copyright (C) 2010 ESF COST Action "Interedition".
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.collatex.match;

import java.util.HashMap;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sd_editions.collatex.Block.Util;

import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.Word;

public class OldSegmentExtractor {

  private static HashMap<String, Segment> witnessHash = Maps.newHashMap();
  private static List<String> possibleWordsInSegments;
  private static List<String> wordsInSegments;

  public static List<WordSegment> extractSegments(final Segment... witnesses) {
    possibleWordsInSegments = Lists.newArrayList();
    wordsInSegments = Lists.newArrayList();

    for (final Segment witness : witnesses) {
      Util.p(witness);
      witnessHash.put(witness.id, witness);
    }

    final WordPairCollection wordpairs = new WordPairCollection(witnessHash);

    int witness_index = 0;
    for (final Segment witness1 : witnesses) {
      final int witnessSize1 = witness1.wordSize();
      for (int position1 = 1; position1 < witnessSize1; position1++) {
        final Word baseWord0 = witness1.getElementOnWordPosition(position1);
        final Word baseWord1 = witness1.getElementOnWordPosition(position1 + 1);
        final String normalized0 = baseWord0._normalized;
        final String normalized1 = baseWord1._normalized;
        if (wordsNotInSegments(baseWord0, baseWord1)) {
          boolean matchingPairFound = false;
          addPairOccurancesInWitness(wordpairs, witness1, witnessSize1, position1, baseWord0, baseWord1, normalized0, normalized1);

          // Check if this pair appears again in the other witnesses
          for (int i = witness_index + 1; i < witnesses.length; i++) {
            final Segment witness2 = witnesses[i];
            final int witnessSize2 = witness2.wordSize();
            for (int position2 = 1; position2 < witnessSize2; position2++) {
              final Word word0 = witness2.getElementOnWordPosition(position2);
              final Word word1 = witness2.getElementOnWordPosition(position2 + 1);
              if (wordsNotInSegments(word0, word1) && pairFound(normalized0, normalized1, witness2, position2)) {
                matchingPairFound = true;
                addPairOccurancesInWitness(wordpairs, witness2, witnessSize2, position2, word0, word1, normalized0, normalized1);
              }
            }
            if (matchingPairFound) wordsInSegments.addAll(possibleWordsInSegments);
          }
        }
      }
      witness_index++;
    }

    return wordpairs.getWordSegments(wordsInSegments);
  }

  private static void addPairOccurancesInWitness(final WordPairCollection wordpairs, final Segment witness, final int witnessSize, final int position, final Word baseWord0, final Word baseWord1,
      final String normalized0, final String normalized1) {
    addWordPair(wordpairs, baseWord0, baseWord1);
    for (int position1 = position + 2; position1 < witnessSize; position1++) {
      if (pairFound(normalized0, normalized1, witness, position1)) {
        final Word word0 = witness.getElementOnWordPosition(position1);
        final Word word1 = witness.getElementOnWordPosition(position1 + 1);
        if (wordsNotInSegments(word0, word1)) {
          addWordPair(wordpairs, word0, word1);
        }
      }
    }
  }

  private static void addWordPair(final WordPairCollection wordpairs, final Word baseWord0, final Word baseWord1) {
    possibleWordsInSegments.add(wordIdentifier(baseWord0));
    possibleWordsInSegments.add(wordIdentifier(baseWord1));
    wordpairs.addWordPair(baseWord0, baseWord1);
  }

  private static boolean wordsNotInSegments(final Word baseWord0, final Word baseWord1) {
    return !(wordsInSegments.contains(wordIdentifier(baseWord0)) || wordsInSegments.contains(wordIdentifier(baseWord1)));
  }

  private static boolean pairFound(final String normalized0, final String normalized1, final Segment witness2, final int position2) {
    return (witness2.getElementOnWordPosition(position2)._normalized.equals(normalized0)) && //
        (witness2.getElementOnWordPosition(position2 + 1)._normalized.equals(normalized1));
  }

  public static String wordIdentifier(final Word word) {
    return word.getWitnessId() + "." + word.position;
  }

}
