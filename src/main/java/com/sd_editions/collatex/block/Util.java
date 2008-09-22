package com.sd_editions.collatex.Block;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.sd_editions.collatex.InputPlugin.StringInputPlugin;

public class Util {
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
    System.out.println('"' + text + '"');
  }

  public static void p(int i) {
    System.out.println(i);
  }

  public static void p(float l) {
    System.out.println(l);
  }

  public static void p(Object o) {
    System.out.println("<" + o + ">");
  }

}
