package eu.interedition.collatex.match;

import java.util.HashMap;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.Word;

public class SegmentExtractor {

  private static HashMap<String, Witness> witnessHash = Maps.newHashMap();

  public static Set<WordSegment> extractSegmentSet(Witness... witnesses) {
    Set<WordSegment> segmentSet = Sets.newHashSet();

    for (Witness witness : witnesses) {
      witnessHash.put(witness.id, witness);
    }

    WordPairCollection wordpairs = new WordPairCollection(witnessHash);

    int witness_index = 0;
    for (Witness witness1 : witnesses) {
      int witnessSize1 = witness1.size();
      for (int position1 = 1; position1 < witnessSize1; position1++) {
        Word baseWord0 = witness1.getWordOnPosition(position1);
        Word baseWord1 = witness1.getWordOnPosition(position1 + 1);
        wordpairs.addWordPair(baseWord0, baseWord1);
        //        List<Word> pair1 = Lists.newArrayList(word0, word1);
        String normalized0 = baseWord0.normalized;
        String normalized1 = baseWord1.normalized;
        //        String segmentTitle = normalized0 + " " + normalized1;

        // Check if this pair appears again in this witness 
        //        WordSegment ws = null;
        for (int position2 = position1 + 2; position2 < witnessSize1; position2++) {
          if (pairFound(normalized0, normalized1, witness1, position2)) {
            wordpairs.addWordPair(baseWord0, baseWord1);
            //            if (ws == null) {
            //              ws = new WordSegment(segmentTitle);
            //              ws.addWitnessPair(witness1.id, pair1);
            //            }
            //            ws.addWitnessPair(witness1.id, Lists.newArrayList(witness1.getWordOnPosition(position2), witness1.getWordOnPosition(position2 + 1)));
          }
        }

        // check the other witnesses for the same pair
        for (int j = witness_index + 1; j < witnesses.length; j++) {
          Witness witness2 = witnesses[j];
          int witnessSize2 = witness2.size();
          for (int position2 = 1; position2 < witnessSize2; position2++) {
            if (pairFound(normalized0, normalized1, witness2, position2)) {
              //              if (ws == null) {
              //                ws = new WordSegment(segmentTitle);
              //                ws.addWitnessPair(witness1.id, pair1);
              //              }
              //              ws.addWitnessPair(witness2.id, Lists.newArrayList(witness2.getWordOnPosition(position2), witness2.getWordOnPosition(position2 + 1)));
            }
          }
        }
        //        if (ws != null) {
        //          ws.grow(witnessHash);
        //          segmentSet.add(ws);
        //          position1 += (ws.size() - 1);
        //        }
      }
      witness_index++;
    }

    return segmentSet;
  }

  private static boolean pairFound(String normalized0, String normalized1, Witness witness2, int position2) {
    return (witness2.getWordOnPosition(position2).normalized.equals(normalized0)) && //
        (witness2.getWordOnPosition(position2 + 1).normalized.equals(normalized1));
  }
}
