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

package eu.interedition.collatex.visualization;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.sd_editions.collatex.permutations.collate.Transposition;

import eu.interedition.collatex.alignment.Match;
import eu.interedition.collatex.experimental.ngrams.alignment.Modification;
import eu.interedition.collatex.experimental.ngrams.alignment.ModificationVisitor;
import eu.interedition.collatex.input.Word;

//TODO remove this class!
//TODO Alignment class in experimental package is its replacement!
public class Modifications {
  private final List<Transposition> transpositions;
  private final List<Modification> modifications;
  private final Set<Match<Word>> matches;

  public Modifications(final List<Modification> _modifications, final List<Transposition> _transpositions, final Set<Match<Word>> _matches) {
    this.modifications = _modifications;
    this.transpositions = _transpositions;
    this.matches = _matches;
  }

  public List<Transposition> getTranspositions() {
    return transpositions;
  }

  public List<Modification> getModifications() {
    final List<Modification> addedUp = Lists.newArrayList();
    addedUp.addAll(modifications);
    addedUp.addAll(transpositions);
    return addedUp;
  }

  public int size() {
    return getModifications().size();
  }

  public Modification get(final int i) {
    return getModifications().get(i);
  }

  public Match getMatchAtBasePosition(final int basePosition) {
    for (final Match match : matches) {
      if (match.getBaseWord().getBeginPosition() == basePosition) return match;
    }
    return null;
  }

  // TODO rename WitnessPosition is meant here!
  public Match getMatchAtSegmentPosition(final int witnessPosition) {
    for (final Match match : matches) {
      if (match.getWitnessWord().getBeginPosition() == witnessPosition) return match;
    }
    return null;
  }

  public Set<Match<Word>> getMatches() {
    return matches;
  }

  public void accept(final ModificationVisitor modificationVisitor) {
    for (final Modification modification : modifications) {
      modification.accept(modificationVisitor);
    }

  }

}
