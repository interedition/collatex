package com.sd_editions.collatex.permutations;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.google.common.base.Join;
import com.google.common.collect.Maps;

public class MultiMatchMap {
  private final HashMap<String, MultiMatch> mmm;

  public MultiMatchMap() {
    this.mmm = Maps.newHashMap();
  }

  public String getNormalizedMatchSentence() {
    return Join.join(" ", keySet());
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
