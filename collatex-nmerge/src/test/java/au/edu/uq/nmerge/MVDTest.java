/*
 * NMerge is Copyright 2009-2011 Desmond Schmidt
 *
 * This file is part of NMerge. NMerge is a Java library for merging
 * multiple versions into multi-version documents (MVDs), and for
 * reading, searching and comparing them.
 *
 * NMerge is free software: you can redistribute it and/or modify
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

package au.edu.uq.nmerge;

import au.edu.uq.nmerge.mvd.Chunk;
import au.edu.uq.nmerge.mvd.ChunkState;
import au.edu.uq.nmerge.mvd.MVD;
import org.junit.Test;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class MVDTest extends AbstractTest {

  @Test
  public void simple() throws Exception {
    final MVD mvd = new MVD("Test");
    final short version1 = (short) mvd.newVersion("test1", "test1", Short.MIN_VALUE, false);
    mvd.update(version1, "Hello funny World!".getBytes());

    final short version2 = (short) mvd.newVersion("test2", "test2", Short.MIN_VALUE, false);
    mvd.update(version2, "Hello World! How funny!".getBytes());

    for (Chunk chunk : mvd.compare(version2, version1, ChunkState.added)) {
      LOG.debug(chunk.toString());
    }

  }
}
