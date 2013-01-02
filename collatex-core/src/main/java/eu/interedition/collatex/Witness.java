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

package eu.interedition.collatex;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Ordering;

import java.util.Comparator;

/**
 * IWitness
 * 
 * Representation of a single textual witness
 *
 */
public interface Witness {

  String getSigil();

  final Comparator<Witness> SIGIL_COMPARATOR = new Comparator<Witness>() {
    @Override
    public int compare(Witness o1, Witness o2) {
      return o1.getSigil().compareTo(o2.getSigil());
    }
  };

  final Function<VariantGraph.Edge, String> TO_SIGILS = new Function<VariantGraph.Edge, String>() {
    @Override
    public String apply(VariantGraph.Edge input) {
      return Joiner.on(", ").join(Ordering.from(SIGIL_COMPARATOR).sortedCopy(input.witnesses()));
    }
  };
}
