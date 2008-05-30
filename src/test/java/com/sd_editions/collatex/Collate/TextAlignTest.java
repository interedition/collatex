package com.sd_editions.collatex.Collate;

import java.util.ArrayList;

import junit.framework.TestCase;

import com.google.common.collect.Lists;

public class TextAlignTest extends TestCase {

  public ArrayList<String> testAlign(TextAlign ta) {
    ArrayList arrL = Lists.newArrayList();
    try {
      arrL = ta.getContentofBlock(ta.getTxtOrigBase());
    } catch (Exception e) {
      // TODO: handle exception
    }
    ta.setBase(arrL);
    ta.base2Slot();
    try {
      arrL = ta.getContentofBlock(ta.getTxtOrigWit(1));
    } catch (Exception e) {
      // TODO: handle exception
      System.out.println(e);
    }
    ta.setWit(arrL);
    ta.wit2Slot();
    return arrL;
  }

  public void testAlignment_ident() {
    TextAlign ta = new TextAlign();
    ArrayList arrL = Lists.newArrayList();
    // Basis
    ta.addNewBase(ta.createBlockStruct("str", "The black cat"));
    // Witnesses
    ta.addNewWit(ta.createBlockStruct("str", "The black cat"));
    arrL = testAlign(ta);
    arrL = ta.collateBase2Wit();
    ta.addAlignInfoRow(arrL);

    assertEquals(arrL.toString(), "[1, , The, , black, , cat, ]");
  }

  public void testAlignment_ident2() {
    TextAlign ta = new TextAlign();
    ArrayList arrL = Lists.newArrayList();
    // Basis
    ta.addNewBase(ta.createBlockStruct("str", "The black cat"));
    // Witnesses
    ta.addNewWit(ta.createBlockStruct("str", "The, black cat"));
    arrL = testAlign(ta);
    arrL = ta.collateBase2Wit();
    ta.addAlignInfoRow(arrL);

    assertEquals(arrL.toString(), "[1, , The, , black, , cat, ]");
  }

  public void testAlignment_ident3() {
    TextAlign ta = new TextAlign();
    ArrayList arrL = Lists.newArrayList();
    // Basis
    ta.addNewBase(ta.createBlockStruct("str", "the white and black cat"));
    // Witnesses
    ta.addNewWit(ta.createBlockStruct("str", "The black cat"));
    arrL = testAlign(ta);
    arrL = ta.collateBase2Wit();
    ta.addAlignInfoRow(arrL);

    assertEquals(arrL.toString(), "[1, , The, , , , , , black, , cat, ]");
  }

  public void testAlignment_case_insensitive() {
    TextAlign ta = new TextAlign();
    ArrayList arrL = Lists.newArrayList();
    // Basis
    ta.addNewBase(ta.createBlockStruct("str", "The black cat"));
    // Witnesses
    ta.addNewWit(ta.createBlockStruct("str", "the black cat"));
    arrL = testAlign(ta);
    arrL = ta.collateBase2Wit();
    ta.addAlignInfoRow(arrL);

    assertEquals(arrL.toString(), "[1, , the, , black, , cat, ]");
  }

  public void testAlignment_omm() {
    TextAlign ta = new TextAlign();
    ArrayList arrL = Lists.newArrayList();
    // Basis
    ta.addNewBase(ta.createBlockStruct("str", "the black cat"));
    // Witnesses
    ta.addNewWit(ta.createBlockStruct("str", "The cat"));
    arrL = testAlign(ta);
    arrL = ta.collateBase2Wit();
    ta.addAlignInfoRow(arrL);
    //TODO
    assertEquals(arrL.toString(), "[1, , The, , , , , cat, ]");
  }

  public void testAlignment_omm2() {
    TextAlign ta = new TextAlign();
    ArrayList arrL = Lists.newArrayList();
    // Basis
    ta.addNewBase(ta.createBlockStruct("str", "The black red cat"));
    // Witnesses
    ta.addNewWit(ta.createBlockStruct("str", "The cat"));
    arrL = testAlign(ta);
    arrL = ta.collateBase2Wit();
    ta.addAlignInfoRow(arrL);

    assertEquals(arrL.toString(), "[1, , The, , , , , , cat, ]");
  }

  public void testAlignment_add() {
    TextAlign ta = new TextAlign();
    ArrayList arrL = Lists.newArrayList();
    // Basis
    ta.addNewBase(ta.createBlockStruct("str", "The cat"));
    // Witnesses
    ta.addNewWit(ta.createBlockStruct("str", "The black cat"));
    arrL = testAlign(ta);
    arrL = ta.collateBase2Wit();
    ta.addAlignInfoRow(arrL);

    assertEquals(arrL.toString(), "[1, , The, black, cat, ]");
  }

  public void testAlignment_add2() {
    TextAlign ta = new TextAlign();
    ArrayList arrL = Lists.newArrayList();
    // Basis
    ta.addNewBase(ta.createBlockStruct("str", "The black cat sat on the mat"));
    // Witnesses
    ta.addNewWit(ta.createBlockStruct("str", "Tom Johns and the black cat sat on the red mat"));
    arrL = testAlign(ta);
    arrL = ta.collateBase2Wit();
    ta.addAlignInfoRow(arrL);

    assertEquals(arrL.toString(), "[1, Tom Johns and, the, , black, , cat, , sat, , on, , the, red, mat, ]");
  }

  public void testAlignment_add3() {
    TextAlign ta = new TextAlign();
    ArrayList arrL = Lists.newArrayList();
    // Basis
    ta.addNewBase(ta.createBlockStruct("str", "The black cat"));
    // Witnesses
    ta.addNewWit(ta.createBlockStruct("str", "the angry big black cat sat on the red mat"));
    arrL = testAlign(ta);
    arrL = ta.collateBase2Wit();
    ta.addAlignInfoRow(arrL);

    assertEquals(arrL.toString(), "[1, , the, angry big, black, , cat, sat on the red mat]");
  }

  public void testAlignment_variant() {
    TextAlign ta = new TextAlign();
    ArrayList arrL = Lists.newArrayList();
    // Basis
    ta.addNewBase(ta.createBlockStruct("str", "the cat sat on the mat"));
    // Witnesses
    ta.addNewWit(ta.createBlockStruct("str", "the black sat on the mat"));
    arrL = testAlign(ta);
    arrL = ta.collateBase2Wit();
    ta.addAlignInfoRow(arrL);

    assertEquals(arrL.toString(), "[1, , the, , black /cat, , sat, , on, , the, , mat, ]");
  }

  public void testAlignment_varinat2() {
    TextAlign ta = new TextAlign();
    ArrayList arrL = Lists.newArrayList();
    // Basis
    ta.addNewBase(ta.createBlockStruct("str", "a yellow cat"));
    // Witnesses
    ta.addNewWit(ta.createBlockStruct("str", "A red cat"));
    arrL = testAlign(ta);
    arrL = ta.collateBase2Wit();
    ta.addAlignInfoRow(arrL);

    assertEquals(arrL.toString(), "[1, , A, , red /yellow, , cat, ]");
  }

  public void testAlignment_new() {
    TextAlign ta = new TextAlign();
    ArrayList arrL = Lists.newArrayList();
    // Basis
    ta.addNewBase(ta.createBlockStruct("str", "the white and black cat"));
    // Witnesses
    ta.addNewWit(ta.createBlockStruct("str", "the black and white cat"));
    arrL = testAlign(ta);
    arrL = ta.collateBase2Wit();
    ta.addAlignInfoRow(arrL);
    assertEquals(arrL.toString(), "[1, , the, black and, white, , , , , , cat, ]");
    assertEquals(12, arrL.size());
  }

  public void testAlignment_new2() {
    TextAlign ta = new TextAlign();
    ArrayList arrL = Lists.newArrayList();
    // Basis
    ta.addNewBase(ta.createBlockStruct("str", "the white and black cat sat on mat"));
    // Witnesses
    ta.addNewWit(ta.createBlockStruct("str", "the black and white cat sat on the mat"));
    arrL = testAlign(ta);
    arrL = ta.collateBase2Wit();
    ta.addAlignInfoRow(arrL);
    assertEquals(arrL.toString(), "[1, , the, black and, white, , , , , , cat, , sat, , on, the, mat, ]");
  }

  public void testAlignment_new3() {
    TextAlign ta = new TextAlign();
    ArrayList arrL = Lists.newArrayList();
    // Basis
    ta.addNewBase(ta.createBlockStruct("str", "the white and black cat sat on the green mat"));
    // Witnesses
    ta.addNewWit(ta.createBlockStruct("str", "the black and white cat sat on the red mat"));
    arrL = testAlign(ta);
    arrL = ta.collateBase2Wit();
    ta.addAlignInfoRow(arrL);
    assertEquals(arrL.toString(), "[1, , the, black and, white, , , , , , cat, , sat, , on, , the, , red /green, , mat, ]");
  }
}
