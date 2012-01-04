package com.opera.core.systems;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opera.core.systems.common.lang.OperaStrings;


public class OperaStringTest {

  @Test
  public void testRegularStringWithoutNumber() {
    assertEquals(false, OperaStrings.isNumeric("sample"));
  }


  @Test
  public void testRegularStringWithNumberOnly() {
    assertEquals(true, OperaStrings.isNumeric("9"));
  }

  @Test
  public void testRegularStringWithDecimalNumberOnly() {
    assertEquals(true, OperaStrings.isNumeric("9.00"));
  }

  @Test
  public void testEmptyString() {
    assertEquals(false, OperaStrings.isNumeric(""));
  }


  @Test
  public void testNullString() {
    assertEquals(false, OperaStrings.isNumeric(null));
  }


}
