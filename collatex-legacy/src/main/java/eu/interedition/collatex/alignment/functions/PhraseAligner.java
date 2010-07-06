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

import eu.interedition.collatex.alignment.Alignment;
import eu.interedition.collatex.alignment.UnfixedAlignment;
import eu.interedition.collatex.input.Phrase;
import eu.interedition.collatex.input.WitnessSegmentPhrases;

public class PhraseAligner {

  // TODO warning duplicate with Matcher.align!
  public static Alignment<Phrase> align(final WitnessSegmentPhrases a, final WitnessSegmentPhrases b, final UnfixedAlignment<Phrase> u) {
    final UnfixedAlignment<Phrase> temp = u;
    //    while (temp.hasUnfixedWords()) {
    //      temp = Matcher.permutate(a, b, temp);
    //    }
    final Alignment<Phrase> alignment = Alignment.createPhraseAlignment(temp, a, b);
    return alignment;
  }
}
