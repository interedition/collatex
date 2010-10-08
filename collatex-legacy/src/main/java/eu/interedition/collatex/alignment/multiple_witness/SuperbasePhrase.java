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

package eu.interedition.collatex.alignment.multiple_witness;

import com.sd_editions.collatex.match.Subsegment;

import eu.interedition.collatex.input.Phrase;

public class SuperbasePhrase extends Phrase {

  public SuperbasePhrase(final int position, final Subsegment subsegment) {
    super(position, position, subsegment);
  }

  @Override
  public String toString() {
    return "SB: " + getSubsegment().getTitle().toString() + " " + getBeginPosition();
  }

}
