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

package eu.interedition.collatex.input;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

import eu.interedition.collatex.input.builders.WitnessBuilder;
import eu.interedition.collatex.input.visitors.JSONObjectVisitor;
@Deprecated
public class WitnessSet {
  private final List<Witness> _witnesses;

  public WitnessSet(Witness... witnesses) {
    this(Arrays.asList(witnesses));
  }

  public WitnessSet(List<Witness> witnesses) {
    this._witnesses = witnesses;
  }

  public static WitnessSet createWitnessSet(String[] witnessStrings) {
    WitnessBuilder builder = new WitnessBuilder();
    int i = 1;
    List<Witness> witnesses = Lists.newArrayList();
    for (String witnessString : witnessStrings) {
      Witness witness = builder.build("witness" + i++, witnessString);
      witnesses.add(witness);
    }
    WitnessSet set = new WitnessSet(witnesses);
    return set;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (Witness witness : _witnesses) {
      builder.append(witness.getFirstSegment().id + ": " + witness.toString() + "\n");
    }
    return builder.toString();
  }

  public List<Witness> getWitnesses() {
    return _witnesses;
  }

  public void accept(JSONObjectVisitor visitor) {
    visitor.visitWitnessSet(this);
    for (Witness witness : _witnesses) {
      witness.accept(visitor);
    }
  }
}
