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

package eu.interedition.collatex2.output;

import java.util.List;
import java.util.Set;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import eu.interedition.collatex2.input.Phrase;
import eu.interedition.collatex2.interfaces.INormalizedToken;
import eu.interedition.collatex2.interfaces.IPhrase;

public class ApparatusEntry {

  private final List<String> sigla;
  private final Multimap<String, INormalizedToken> sigilToTokens;

  public ApparatusEntry(final List<String> sigli) {
    this.sigla = sigli;
    this.sigilToTokens = LinkedHashMultimap.create();
  }

  public void addToken(final String sigil, final INormalizedToken token) {
    sigilToTokens.put(sigil, token);
  }

  public boolean containsWitness(final String sigil) {
    return sigilToTokens.containsKey(sigil);
  }

  public IPhrase getPhrase(final String witnessId) {
    return new Phrase(Lists.newArrayList(sigilToTokens.get(witnessId)));
  }

  public List<String> getSigla() {
    return sigla;
  }

  public Set<String> getEmptyCells() {
    final Set<String> emptySigli = Sets.newLinkedHashSet(sigla);
    emptySigli.removeAll(sigilToTokens.keySet());
    return emptySigli;
  }

  public boolean hasEmptyCells() {
    return sigla.size() != sigilToTokens.keySet().size();
  }
}
