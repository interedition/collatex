package com.sd_editions.collatex.permutations;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.google.common.base.Join;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class MultiMatchMap {
  private final LinkedHashMap<String, MultiMatch> mmm;

  public MultiMatchMap(List<Witness> witnesses) {
    this.mmm = Maps.newLinkedHashMap();
    // initialize with the first 2 witnesses
    Witness base = witnesses.get(0);
    Witness witness = witnesses.get(1);
    for (Word baseword : base.getWords()) {
      String normalized = baseword.normalized;
      if (this.containsKey(normalized)) {
        this.get(normalized).addMatchingWord(baseword);
      }
      for (Word witnessword : witness.getWords()) {
        if (normalized.equals(witnessword.normalized)) {
          MultiMatch mm;
          if (this.containsKey(normalized)) {
            mm = this.get(normalized);
            mm.addMatchingWord(witnessword);
          } else {
            mm = new MultiMatch(baseword, witnessword);
          }
          this.put(normalized, mm);
        }
      }
    }
    // go over the rest of the witnesses, comparing the normalizedwords from the multimatches
    for (int i = 2; i < witnesses.size(); i++) {
      witness = witnesses.get(i);
      Set<String> keySet = Sets.newLinkedHashSet(this.keySet());
      for (String normalized : keySet) {
        boolean normalizedHasMatchInThisWitness = false;
        for (Word witnessword : witness.getWords()) {
          if (normalized.equals(witnessword.normalized)) {
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
    return Join.join(" ", mmm.keySet());
  }

  public List<Set<Match>> permutations() {
    // TODO Auto-generated method stub
    return null;
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
