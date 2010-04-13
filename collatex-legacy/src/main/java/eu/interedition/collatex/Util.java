package eu.interedition.collatex;

import java.util.Random;

public class Util {
  private static Random random = new Random();

  public static String generateRandomId() {
    return Long.toString(Math.abs(random.nextLong()), 5);
  }

}
