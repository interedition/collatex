package com.sd_editions.collatex.Collate;

import org.apache.commons.lang.StringUtils;

import junit.framework.TestCase;

public class LevensteinTest extends TestCase {
//  public void testLevenstein() {
//    System.out.println(StringUtils.getLevenshteinDistance("a", "and"));
//    System.out.println(StringUtils.getLevenshteinDistance("a", "cat"));
//    System.out.println(StringUtils.getLevenshteinDistance("a", "dog"));
//    System.out.println(StringUtils.getLevenshteinDistance("a", "or"));
//    System.out.println("---------");
//    System.out.println(StringUtils.getLevenshteinDistance("and", "cat"));
//    System.out.println(StringUtils.getLevenshteinDistance("and", "dog"));
//    System.out.println(StringUtils.getLevenshteinDistance("and", "or"));
//    System.out.println("---------");
//    System.out.println(StringUtils.getLevenshteinDistance("cat", "dog"));
//    System.out.println(StringUtils.getLevenshteinDistance("cat", "or"));
//    System.out.println("---------");
//    System.out.println(StringUtils.getLevenshteinDistance("dog", "or"));
//  }
  
public void testLevenstein() {
System.out.println(StringUtils.getLevenshteinDistance("a", "and"));
System.out.println(StringUtils.getLevenshteinDistance("a", "cat"));
System.out.println(StringUtils.getLevenshteinDistance("a", "dog"));
System.out.println(StringUtils.getLevenshteinDistance("a", "or"));
System.out.println("---------");
System.out.println(StringUtils.getLevenshteinDistance("and", "cat"));
System.out.println(StringUtils.getLevenshteinDistance("and", "dog"));
System.out.println(StringUtils.getLevenshteinDistance("and", "or"));
System.out.println("---------");
System.out.println(StringUtils.getLevenshteinDistance("cat", "dog"));
System.out.println(StringUtils.getLevenshteinDistance("cat", "or"));
System.out.println("---------");
System.out.println(StringUtils.getLevenshteinDistance("dog", "or"));
}

}
