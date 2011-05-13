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

package eu.interedition.collatex2.interfaces;

import eu.interedition.collatex2.implementation.vg_alignment.IAlignment2;
import eu.interedition.collatex2.implementation.vg_analysis.IAnalysis;


public interface IAligner {
  //maybe move this method to the variant graph builder?
  IVariantGraph getResult();

  //maybe move this method to the variant graph builder?
  IAligner add(IWitness... witnesses);

  //Move this method to somewhere else!
  IAnalysis analyze(IWitness witness);
  
  IAlignment2 align(IWitness witness);
  
}
