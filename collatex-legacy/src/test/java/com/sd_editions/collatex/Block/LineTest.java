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

package com.sd_editions.collatex.Block;

import java.util.List;

import junit.framework.TestCase;

public class LineTest extends TestCase {
  public void testGetIndex() throws BlockStructureCascadeException {
    Line line = new Line(1);
    BlockStructure structure = new BlockStructure();
    structure.setRootBlock(line);
    Word word1 = new Word("first");
    Word word2 = new Word("second");
    Word word3 = new Word("third");
    structure.setChildBlock(line, word1);
    structure.setChildBlock(line, word2);
    structure.setChildBlock(line, word3);
    assertEquals(word1, line.get(1));
    assertEquals(word2, line.get(2));
    assertEquals(word3, line.get(3));
  }

  public void testSize() throws BlockStructureCascadeException {
    Line line = new Line(1);
    assertEquals(0, line.size());
    BlockStructure structure = new BlockStructure();
    structure.setRootBlock(line);
    Word word1 = new Word("first");
    Word word2 = new Word("second");
    Word word3 = new Word("third");
    structure.setChildBlock(line, word1);
    assertEquals(1, line.size());
    structure.setChildBlock(line, word2);
    assertEquals(2, line.size());
    structure.setChildBlock(line, word3);
    assertEquals(3, line.size());
  }

  public void testGetPhrase() throws BlockStructureCascadeException {
    Line line = new Line(1);
    BlockStructure structure = new BlockStructure();
    structure.setRootBlock(line);
    Word word1 = new Word("first");
    Word word2 = new Word("second");
    Word word3 = new Word("third");
    Word word4 = new Word("four");
    Word word5 = new Word("fifth");
    structure.setChildBlock(line, word1);
    structure.setChildBlock(line, word2);
    structure.setChildBlock(line, word3);
    structure.setChildBlock(line, word4);
    structure.setChildBlock(line, word5);
    List<Word> phrase = line.getPhrase(1, 1);
    assertEquals(1, phrase.size());
    assertEquals(word1, phrase.get(0));
    phrase = line.getPhrase(1, 2);
    assertEquals(2, phrase.size());
    assertEquals(word1, phrase.get(0));
    assertEquals(word2, phrase.get(1));
    phrase = line.getPhrase(2, 4);
    assertEquals(3, phrase.size());
    assertEquals(word2, phrase.get(0));
    assertEquals(word3, phrase.get(1));
    assertEquals(word4, phrase.get(2));
  }

}
