package eu.interedition.collatex.matching;

import java.util.List;
import java.util.Map;

import com.sd_editions.collatex.match.SubsegmentExtractor;

import eu.interedition.collatex.alignment.Alignment;
import eu.interedition.collatex.input.Phrase;
import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.Witness;

public class PhraseMatcher {

  public static Alignment<Phrase> align(Witness a, Witness b) {
    Segment firstSegment = a.getFirstSegment();
    Segment firstSegment2 = b.getFirstSegment();
    return align(firstSegment, firstSegment2);
  }

  private static Alignment<Phrase> align(Segment firstSegment, Segment firstSegment2) {
    SubsegmentExtractor subsegmentextractor = new SubsegmentExtractor(firstSegment, firstSegment2);
    subsegmentextractor.go();
    System.out.println(subsegmentextractor.getSubsegments().size());
    Map<String, List<Phrase>> phrasesPerSegment = subsegmentextractor.getPhrasesPerSegment();
    System.out.println(phrasesPerSegment.keySet());
    throw new RuntimeException();

    //  
    //    System.out.println("!!" + phrasesPerSegment.keySet());
    //   
    //    return null;
  }
}
