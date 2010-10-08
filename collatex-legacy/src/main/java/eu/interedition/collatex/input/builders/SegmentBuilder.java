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

package eu.interedition.collatex.input.builders;

import eu.interedition.collatex.input.Phrase;
import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.Witness;
import eu.interedition.collatex.input.WitnessSegmentPhrases;

public class SegmentBuilder {

  public static WitnessSegmentPhrases build(final String witnessId, final String phrasePlain) {
    final WitnessBuilder builder = new WitnessBuilder();
    final Witness witness = builder.build(witnessId, phrasePlain);
    final Segment segment = witness.getFirstSegment();
    final Phrase phrase = new Phrase(segment, segment.getElementOnWordPosition(1), segment.getElementOnWordPosition(segment.wordSize()), null);
    final WitnessSegmentPhrases p = new WitnessSegmentPhrases(witnessId, phrase);
    return p;
  }
}
