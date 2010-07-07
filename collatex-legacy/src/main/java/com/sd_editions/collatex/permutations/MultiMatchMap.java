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

package com.sd_editions.collatex.permutations;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import eu.interedition.collatex.input.Segment;
import eu.interedition.collatex.input.Word;

public class MultiMatchMap {
  private final LinkedHashMap<String, MultiMatch> mmm;

  public MultiMatchMap(Segment... witnesses) {
    this.mmm = Maps.newLinkedHashMap();
    // initialize with the first 2 witnesses
    Segment witness0 = witnesses[0];
    Segment witness1 = witnesses[1];
    for (Word word0 : witness0.getWords()) {
      String normalized = word0._normalized;
      if (this.containsKey(normalized)) {
        this.get(normalized).addMatchingWord(word0);
      }
      for (Word word1 : witness1.getWords()) {
        if (normalized.equals(word1._normalized)) {
          MultiMatch mm;
          if (this.containsKey(normalized)) {
            mm = this.get(normalized);
            mm.addMatchingWord(word1);
          } else {
            mm = new MultiMatch(word0, word1);
          }
          this.put(normalized, mm);
        }
      }
    }
    // go over the rest of the witnesses, comparing the normalizedwords from the multimatches
    for (int i = 2; i < witnesses.length; i++) {
      witness1 = witnesses[i];
      Set<String> keySet = Sets.newLinkedHashSet(this.keySet());
      for (String normalized : keySet) {
        boolean normalizedHasMatchInThisWitness = false;
        for (Word witnessword : witness1.getWords()) {
          if (normalized.equals(witnessword._normalized)) {
            MultiMatch mm = this.get(normalized);
            mm.addMatchingWord(witnessword);
            this.put(normalized, mm);
            normalizedHasMatchInThisWitness = true;
          }
        }
        if (!normalizedHasMatchInThisWitness) {
          this.remove(normalized);
        }
      }
    }
  }

  public String getNormalizedMatchSentence() {
    return Joiner.on(" ").join(mmm.keySet());
  }

  /* Delegated methods after here */
  public void clear() {
    mmm.clear();
  }

  @Override
  public Object clone() {
    return mmm.clone();
  }

  public boolean containsKey(Object key) {
    return mmm.containsKey(key);
  }

  public boolean containsValue(Object value) {
    return mmm.containsValue(value);
  }

  public Set<Entry<String, MultiMatch>> entrySet() {
    return mmm.entrySet();
  }

  @Override
  public boolean equals(Object o) {
    return mmm.equals(o);
  }

  public MultiMatch get(Object key) {
    return mmm.get(key);
  }

  @Override
  public int hashCode() {
    return mmm.hashCode();
  }

  public boolean isEmpty() {
    return mmm.isEmpty();
  }

  public Set<String> keySet() {
    return mmm.keySet();
  }

  public MultiMatch put(String key, MultiMatch value) {
    return mmm.put(key, value);
  }

  public void putAll(Map<? extends String, ? extends MultiMatch> m) {
    mmm.putAll(m);
  }

  public MultiMatch remove(Object key) {
    return mmm.remove(key);
  }

  public int size() {
    return mmm.size();
  }

  @Override
  public String toString() {
    return mmm.toString();
  }

  public Collection<MultiMatch> values() {
    return mmm.values();
  }
  /* End of delegated methods */

}
