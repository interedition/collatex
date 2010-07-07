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

  @SuppressWarnings("null")
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
