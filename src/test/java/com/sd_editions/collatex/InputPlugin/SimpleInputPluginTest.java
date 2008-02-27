//Make this part of the same package so we can test the protected methods
package com.sd_editions.collatex.InputPlugin;

import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.sd_editions.collatex.Block.BlockStructure;
import com.sd_editions.collatex.Block.BlockStructureCascadeException;

/**
 * Unit test for SimpleInputPlugin.
 */
public class SimpleInputPluginTest extends TestCase {
  /**
   * Create the test case
   *
   * @param testName name of the test case
   */
  public SimpleInputPluginTest(String testName) {
    super(testName);
  }

  /**
   * @return the suite of tests being tested
   */
  public static Test suite() {
    return new TestSuite(SimpleInputPluginTest.class);
  }

  public void testSimpleInputPlugin() {
    String example1 = "examples/inputfiles/example1.txt";
    BlockStructure bs = null;

    SimpleInputPlugin si = new SimpleInputPlugin(example1);
    try {
      bs = si.readFile();
    } catch (FileNotFoundException e) {
      fail("Could not load input file: " + example1 + "\n" + e);
    } catch (IOException e) {
      fail("Could not read input file: " + example1 + "\n" + e);
    } catch (BlockStructureCascadeException e) {
      fail("Could not create input structure: " + example1 + "\n" + e);
    }

    //The example1.txt file should have only 4 words
    assertEquals(4, bs.getNumberOfBlocks());
  }
}
