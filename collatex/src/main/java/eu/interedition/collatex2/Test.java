package eu.interedition.collatex2;

import eu.interedition.collatex2.implementation.CollateXEngine;

public class Test {
public static void main(String[] args) {
  CollateXEngine engine = new CollateXEngine();
  engine.createWitness("A", "test");
}
}
