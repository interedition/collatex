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

package eu.interedition.collatex.experimental.ngrams.alignment;

import java.util.List;

import eu.interedition.collatex.experimental.ngrams.NGram;
import eu.interedition.collatex2.interfaces.IWitness;

public class Alignment {

  private final List<NGram> matches;
  private final List<Gap> gaps;

  public Alignment(final List<NGram> matches, final List<Gap> gaps) {
    this.matches = matches;
    this.gaps = gaps;
  }

  public List<NGram> getMatches() {
    return matches;
  }

  public List<Gap> getGaps() {
    return gaps;
  }

  public static Alignment create(final IWitness a, final IWitness b) {
    final WitnessSet set = new WitnessSet(a, b);
    return set.align();
  }

  public void accept(final ModificationVisitor modificationVisitor) {
    for (final Gap gap : gaps) {
      final Modification modification = gap.getModification();
      modification.accept(modificationVisitor);
    }
  }
}
