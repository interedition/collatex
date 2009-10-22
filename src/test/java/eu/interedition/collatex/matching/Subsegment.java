package eu.interedition.collatex.matching;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class Subsegment {
  private final String title;
  private boolean open = true;
  private final Map<String, List<Integer>> map = Maps.newHashMap();

  public Subsegment(String _title) {
    this.title = _title;
  }

  public String getTitle() {
    return title;
  }

  public boolean isOpen() {
    return open;
  }

  public void add(String witnessId, List<Integer> positions) {
    map.put(witnessId, positions);
  }

  public Set<Entry<String, List<Integer>>> entrySet() {
    return map.entrySet();
  }

  public int size() {
    return map.size();
  }

  public List<Integer> get(String witnessId) {
    return map.get(witnessId);
  }

  @Override
  public String toString() {
    return getTitle() + " " + map.toString();
  }

  public void close() {
    open = false;
  }

  public Set<String> getWitnessIds() {
    return map.keySet();
  }

  public List<SegmentPosition> getSegmentPositions() {
    List<SegmentPosition> list = Lists.newArrayList();
    for (Entry<String, List<Integer>> entry : map.entrySet()) {
      String witnessId = entry.getKey();
      for (Integer position : entry.getValue()) {
        list.add(new SegmentPosition(witnessId, position));
      }
    }
    return list;
  }
}
