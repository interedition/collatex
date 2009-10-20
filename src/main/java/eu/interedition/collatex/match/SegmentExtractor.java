package eu.interedition.collatex.match;

import java.util.HashMap;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sd_editions.collatex.Block.Util;

import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.Word;

public class SegmentExtractor {

  private static HashMap<String, Witness> witnessHash = Maps.newHashMap();
  private static List<String> possibleWordsInSegments;
  private static List<String> wordsInSegments;

  public static List<WordSegment> extractSegments(Witness... witnesses) {
    possibleWordsInSegments = Lists.newArrayList();
    wordsInSegments = Lists.newArrayList();

    for (Witness witness : witnesses) {
      Util.p(witness);
      witnessHash.put(witness.id, witness);
    }

    WordPairCollection wordpairs = new WordPairCollection(witnessHash);

    int witness_index = 0;
    for (Witness witness1 : witnesses) {
      int witnessSize1 = witness1.size();
      for (int position1 = 1; position1 < witnessSize1; position1++) {
        Word baseWord0 = witness1.getWordOnPosition(position1);
        Word baseWord1 = witness1.getWordOnPosition(position1 + 1);
        String normalized0 = baseWord0.normalized;
        String normalized1 = baseWord1.normalized;
        if (wordsNotInSegments(baseWord0, baseWord1)) {
          boolean matchingPairFound = false;
          addPairOccurancesInWitness(wordpairs, witness1, witnessSize1, position1, baseWord0, baseWord1, normalized0, normalized1);

          // Check if this pair appears again in the other witnesses
          for (int i = witness_index + 1; i < witnesses.length; i++) {
            Witness witness2 = witnesses[i];
            int witnessSize2 = witness2.size();
            for (int position2 = 1; position2 < witnessSize2; position2++) {
              Word word0 = witness2.getWordOnPosition(position2);
              Word word1 = witness2.getWordOnPosition(position2 + 1);
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

  private static void addPairOccurancesInWitness(WordPairCollection wordpairs, Witness witness, int witnessSize, int position, Word baseWord0, Word baseWord1, String normalized0, String normalized1) {
    addWordPair(wordpairs, baseWord0, baseWord1);
    for (int position1 = position + 2; position1 < witnessSize; position1++) {
      if (pairFound(normalized0, normalized1, witness, position1)) {
        Word word0 = witness.getWordOnPosition(position1);
        Word word1 = witness.getWordOnPosition(position1 + 1);
        if (wordsNotInSegments(word0, word1)) {
          addWordPair(wordpairs, word0, word1);
        }
      }
    }
  }

  private static void addWordPair(WordPairCollection wordpairs, Word baseWord0, Word baseWord1) {
    possibleWordsInSegments.add(wordIdentifier(baseWord0));
    possibleWordsInSegments.add(wordIdentifier(baseWord1));
    wordpairs.addWordPair(baseWord0, baseWord1);
  }

  private static boolean wordsNotInSegments(Word baseWord0, Word baseWord1) {
    return !(wordsInSegments.contains(wordIdentifier(baseWord0)) || wordsInSegments.contains(wordIdentifier(baseWord1)));
  }

  private static boolean pairFound(String normalized0, String normalized1, Witness witness2, int position2) {
    return (witness2.getWordOnPosition(position2).normalized.equals(normalized0)) && //
        (witness2.getWordOnPosition(position2 + 1).normalized.equals(normalized1));
  }

  public static String wordIdentifier(Word word) {
    return word.getWitnessId() + "." + word.position;
  }

}
