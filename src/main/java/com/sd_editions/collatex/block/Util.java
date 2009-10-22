package com.sd_editions.collatex.Block;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.lang.WordUtils;

import com.google.common.base.Join;
import com.sd_editions.collatex.InputPlugin.StringInputPlugin;

public class Util {
  private static final int WRAP_AT = 120;

  public static BlockStructure string2BlockStructure(String string) {
    BlockStructure result = null;
    try {
      result = new StringInputPlugin(string).readFile();
      // TODO: work away those exceptions.. they are not relevant for Strings
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (BlockStructureCascadeException e) {
      throw new RuntimeException(e);
    }
    return result;
  }

  public static void p(String text) {
    pp(text, '"' + text + '"');
  }

  public static void p(int i) {
    System.out.println(i);
  }

  public static void p(float l) {
    System.out.println(l);
  }

  public static void p(Object o) {
    pp(o, "<" + o + ">");
  }

  private static void pp(Object o, String string) {
    if (o == null) {
      System.out.println(o);
    } else {
      System.out.println(WordUtils.wrap(string, WRAP_AT, "\n  ", false));
    }
  }

  public static void p(Collection c) {
    pp(c, "[" + Join.join(",", c) + "]");
  }

  public static void p(String label, Object o) {
    System.out.print(label + ": ");
    p(o);
  }

  public static void p(String label, int o) {
    System.out.print(label + ": ");
    p(o);
  }

  public static void p(String label, float o) {
    System.out.print(label + ": ");
    p(o);
  }

  public static void remark(String string) {
    System.out.println(string);
  }

  public static void newline() {
    System.out.println();
  }

}
