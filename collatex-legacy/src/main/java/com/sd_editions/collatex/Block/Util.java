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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.lang.WordUtils;

import com.google.common.base.Joiner;
import com.sd_editions.collatex.InputPlugin.StringInputPlugin;

public class Util {
  private static final int WRAP_AT = 120;

  public static BlockStructure string2BlockStructure(final String string) {
    BlockStructure result = null;
    try {
      result = new StringInputPlugin(string).readFile();
      // TODO work away those exceptions.. they are not relevant for Strings
    } catch (final FileNotFoundException e) {
      throw new RuntimeException(e);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    } catch (final BlockStructureCascadeException e) {
      throw new RuntimeException(e);
    }
    return result;
  }

  public static void p(final String text) {
    pp(text, '"' + text + '"');
  }

  public static void p(final int i) {
    System.out.println(i);
  }

  public static void p(final float l) {
    System.out.println(l);
  }

  public static void p(final Object o) {
    pp(o, "<" + o + ">");
  }

  private static void pp(final Object o, final String string) {
    if (o == null) {
      System.out.println(o);
    } else {
      System.out.println(WordUtils.wrap(string, WRAP_AT, "\n  ", false));
    }
  }

  public static void p(final Collection<?> c) {
    pp(c, "[" + Joiner.on(",").join(c) + "]");
  }

  public static void p(final String label, final Object o) {
    System.out.print(label + ": ");
    p(o);
  }

  public static void p(final String label, final int o) {
    System.out.print(label + ": ");
    p(o);
  }

  public static void p(final String label, final float o) {
    System.out.print(label + ": ");
    p(o);
  }

  public static void remark(final String string) {
    System.out.println(string);
  }

  public static void newline() {
    System.out.println();
  }

}
