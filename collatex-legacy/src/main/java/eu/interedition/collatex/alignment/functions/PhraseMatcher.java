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

package eu.interedition.collatex.alignment.functions;

import com.sd_editions.collatex.match.SubsegmentExtractor;

import eu.interedition.collatex.alignment.Alignment;
import eu.interedition.collatex.input.Phrase;
import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.WitnessSegmentPhrases;

// HIGHLY UNSTABLE: DO NOT USE!
public class PhraseMatcher {

  public static Alignment<Phrase> align(final Witness a, final Witness b) {
    final Segment firstSegment = a.getFirstSegment();
    final Segment firstSegment2 = b.getFirstSegment();
    return align(firstSegment, firstSegment2);
  }

  private static Alignment<Phrase> align(final Segment firstSegment, final Segment firstSegment2) {
    final SubsegmentExtractor subsegmentextractor = new SubsegmentExtractor(firstSegment, firstSegment2);
    subsegmentextractor.go();
    System.out.println(subsegmentextractor.getSubsegments().size());
    //    Map<String, List<Phrase>> phrasesPerSegment = subsegmentextractor.getPhrasesPerSegment();
    //    System.out.println(phrasesPerSegment.keySet());
    //    throw new RuntimeException();
    return null;

    //  
    //    System.out.println("!!" + phrasesPerSegment.keySet());
    //   
    //    return null;
  }

  public static Alignment<Phrase> align(final WitnessSegmentPhrases ph1, final WitnessSegmentPhrases ph2) {
    // The matching has to be done here!

    // TODO Auto-generated method stub
    return null;
  }
}
