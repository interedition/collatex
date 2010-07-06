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

package com.sd_editions.collatex.Collate;

import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.TestCase;

import com.sd_editions.collatex.Block.BlockStructure;
import com.sd_editions.collatex.Block.BlockStructureCascadeException;
import com.sd_editions.collatex.InputPlugin.StringInputPlugin;

public class TestIndexTable extends TestCase {

  public void testIndexTableUnsorted1() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    BlockStructure base = new StringInputPlugin("a red cat or black cat sat").readFile();
    IndexTable iTabbase = new IndexTable(base);
    assertEquals("{a=[1], red=[2], cat=[3, 6], or=[4], black=[5], sat=[7]}", iTabbase.getIndexTable().toString());
  }

  public void testIndexTableSorted1() throws FileNotFoundException, IOException, BlockStructureCascadeException {
    BlockStructure base = new StringInputPlugin("a red cat or black cat sat").readFile();
    IndexTable iTabbase = new IndexTable(base);
    iTabbase.makeSortedMap();
    assertEquals("{a=[1], black=[5], cat=[3, 6], or=[4], red=[2], sat=[7]}", iTabbase.getIndexTableSorted().toString());
  }
}