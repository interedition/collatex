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

package eu.interedition.collatex2.implementation;

import eu.interedition.collatex2.interfaces.IAlignment;
import eu.interedition.collatex2.interfaces.IAlignmentTable;
import eu.interedition.collatex2.interfaces.IWitness;
import eu.interedition.collatex2.legacy.alignment.SequenceDetection;
import eu.interedition.collatex2.legacy.tokencontainers.AlignmentTableCreator3;

public class PairwiseAlignmentHelper {
  public static IAlignment align(CollateXEngine engine, IWitness a, IWitness b) {
    IAlignmentTable table = engine.align(a);
    IAlignment alignment_without_seq = AlignmentTableCreator3.createAlignmentUsingIndex(table, b);
    final IAlignment alignment = SequenceDetection.improveAlignment(alignment_without_seq);
    return alignment;
  }
}
