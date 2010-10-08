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

import java.util.Set;

import com.google.common.collect.Sets;

import eu.interedition.collatex.alignment.Match;
import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.Word;
import eu.interedition.collatex.match.worddistance.WordDistance;

public class RealMatcher {

  // NOTE: this code is specific for Segments/Words!
  // TODO generalize to IWitness etc
  public static Set<Match<Word>> findMatches(final Segment base, final Segment witness, final WordDistance distanceMeasure) {
    final Set<Match<Word>> matchSet = Sets.newLinkedHashSet();
    for (final Word baseWord : base.getWords()) {
      for (final Word witnessWord : witness.getWords()) {
        if (baseWord._normalized.equals(witnessWord._normalized)) {
          matchSet.add(new Match<Word>(baseWord, witnessWord));
        } else {
          final float editDistance = distanceMeasure.distance(baseWord._normalized, witnessWord._normalized);
          if (editDistance < 0.5) matchSet.add(new Match<Word>(baseWord, witnessWord, editDistance));
        }
      }
    }
    return matchSet;
  }
}
