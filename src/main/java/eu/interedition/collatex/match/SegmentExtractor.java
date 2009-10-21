package eu.interedition.collatex.match;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.Word;

public class SegmentExtractor {

  private static HashMap<String, Segment> witnessHash = Maps.newHashMap();

  public static Set<WordSegment> extractSegmentSet(Segment... witnesses) {
    Set<WordSegment> segmentSet = Sets.newHashSet();
    HashMap<String, WordSegment> segmentHash = Maps.newHashMap();

    int witness_index = 0;
    for (Segment witness1 : witnesses) {
      witnessHash.put(witness1.id, witness1);
      int witnessSize1 = witness1.size();
      for (int position1 = 1; position1 < witnessSize1; position1++) {
        List<Word> pair1 = Lists.newArrayList(witness1.getWordOnPosition(position1), witness1.getWordOnPosition(position1 + 1));
        String normalized0 = pair1.get(0).normalized;
        String normalized1 = pair1.get(1).normalized;

        // check the other witnesses for the same pair
        for (int j = witness_index + 1; j < witnesses.length; j++) {
          Segment witness2 = witnesses[j];
          int witnessSize2 = witness2.size();
          for (int position2 = 1; position2 < witnessSize2; position2++) {
            if (pairFound(normalized0, normalized1, witness2, position2)) {
              String segmentTitle = normalized0 + " " + normalized1;
              WordSegment ws;
              if (segmentHash.containsKey(segmentTitle)) {
                ws = segmentHash.get(segmentTitle);
              } else {
                ws = new WordSegment(segmentTitle);
                ws.addWitness(witness1.id, pair1);
              }
              ws.addWitness(witness2.id, Lists.newArrayList(witness2.getWordOnPosition(position2), witness2.getWordOnPosition(position2 + 1)));
              segmentHash.put(segmentTitle, ws);
              segmentSet.add(ws);
              position1++;
            }
          }
        }
      }
      witness_index++;
    }

    // Now see which segments can be extended:
    // for each WordSegment, the next word for all witnesses in the Segment should match
    for (WordSegment wordSegment : segmentSet) {
      wordSegment.grow(witnessHash);
    }

    return segmentSet;
  }

  private static boolean pairFound(String normalized0, String normalized1, Segment witness2, int position2) {
    return (witness2.getWordOnPosition(position2).normalized.equals(normalized0)) && //
        (witness2.getWordOnPosition(position2 + 1).normalized.equals(normalized1));
  }
}
