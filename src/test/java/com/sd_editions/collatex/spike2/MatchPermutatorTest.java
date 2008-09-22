package com.sd_editions.collatex.spike2;

import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import com.google.common.base.Join;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class MatchPermutatorTest extends TestCase {

  private MatchPermutator permutator;
  private Set<Match> testSet;
  private Match match_1_2;
  private Match match_1_3;
  private Match match_2_1;
  private Match match_2_3;
  private Match match_3_4;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    testSet = Sets.newHashSet();
    match_1_2 = new Match(new Word("een", 1), new Word("een", 2));
    match_1_3 = new Match(new Word("een", 1), new Word("tween", 3));
    match_2_1 = new Match(new Word("twee", 2), new Word("twee", 1));
    match_2_3 = new Match(new Word("twee", 2), new Word("tween", 3));
    match_3_4 = new Match(new Word("drie", 3), new Word("drie", 4));
    testSet.add(match_1_2);
    testSet.add(match_1_3);
    testSet.add(match_2_1);
    testSet.add(match_2_3);
    testSet.add(match_3_4);
    permutator = new MatchPermutator(testSet);
  }

  public void testGroup1() {
    List<MatchGroup> group = permutator.group(testSet);
    String groupString = matchArrayListToString(group);
    assertTrue(groupString.contains("[(1->2),(1->3)]"));
    assertTrue(groupString.contains("[(1->3),(2->3)]"));
    assertTrue(groupString.contains("[(2->1),(2->3)]"));
    assertTrue(groupString.contains("[(3->4)]"));
    assertEquals(4, group.size());
  }

  public void testDegroup() {
    //      grouped_tuples = [[[1,2]],[[2,1]],[[3,4]]]
    List<MatchGroup> groupedMatches = Lists.newArrayList(new MatchGroup(match_1_2), new MatchGroup(match_2_1), new MatchGroup(match_3_4)); //      expected = [[1,2],[2,1],[3,4]]
    Set<Match> expected = Sets.newHashSet(match_1_2, match_2_1, match_3_4);
    //      assert_equal(expected, permutator.degroup(grouped_tuples))
    assertEquals(expected, permutator.degroup(groupedMatches));
  }

  //def test_fix_cell1
  public void testFixCell1() {
    //      grouped_tuples = [[[1,2],[1,3]],[[2,1],[2,3]],[[3,4]]]
    List<MatchGroup> groupedMatches = Lists.newArrayList(new MatchGroup(match_1_2, match_1_3), new MatchGroup(match_2_1, match_2_3), new MatchGroup(match_3_4));
    //      expected=[[[1,3]],[[2,1]],[[3,4]]]
    List<MatchGroup> expected = Lists.newArrayList(new MatchGroup(match_1_3), new MatchGroup(match_2_1), new MatchGroup(match_3_4));
    //      assert_equal(expected, permutator.fix_cell(grouped_tuples, 0, [1,3]))
    List<MatchGroup> fixCell = permutator.fixCell(groupedMatches, 0, match_1_3);
    assertEquals(expected.size(), fixCell.size());
    assertEquals(matchArrayListToString(expected), matchArrayListToString(fixCell));
    //end
  }

  String matchArrayListToString(List<MatchGroup> group) {
    List<String> matchArrayStrings = Lists.newArrayList();
    for (MatchGroup matchA : group) {
      List<String> matchStrings = Lists.newArrayList();
      for (Match match : matchA) {
        matchStrings.add(match.toString());
      }
      matchArrayStrings.add("[" + Join.join(",", Lists.sortedCopy(matchStrings)) + "]");
    }
    return Join.join(", ", Lists.sortedCopy(matchArrayStrings));
  }
}
