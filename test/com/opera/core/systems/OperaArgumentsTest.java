package com.opera.core.systems;

import com.opera.core.systems.arguments.OperaArgument;
import com.opera.core.systems.arguments.OperaCoreArguments;
import com.opera.core.systems.arguments.OperaDesktopArguments;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

public class OperaArgumentsTest {

  private static OperaArguments arguments;

   @Test
  public void testConstructingOperaArguments() {
    arguments = new OperaArguments();
    Assert.assertNotNull(arguments);
  }

  @Test
  public void testAddArgumentWithKey() {
    arguments.add("foo");
    Assert.assertNotNull(arguments.getArguments());
    Assert.assertEquals(1, arguments.getArguments().size());
  }

  @Test
  public void testAddArgumentWithKeyAndValue() {
    arguments.add("foo", "bar");
    Assert.assertEquals("foo", arguments.getArguments().get(1).getArgument());
    Assert.assertEquals("bar", arguments.getArguments().get(1).getValue());
  }

  @Test
  public void testAddArgumentWithOperaArgument() {
    OperaArgument argument = new OperaArgument("hooba", "flooba");
    arguments.add(argument);
    Assert.assertEquals("hooba", arguments.getArguments().get(2).getArgument());
    Assert.assertEquals("flooba", arguments.getArguments().get(2).getValue());
  }

  @Test
  public void testGettingArgumentsAsObjectList() {
    Assert.assertEquals(3, arguments.getArguments().size());
  }

  @Test
  public void testGettingArgumentsAsStringList() {
    Assert.assertEquals(5, arguments.getArgumentsAsStringList().size());
    Assert.assertEquals("hooba", arguments.getArgumentsAsStringList().get(3));
  }

  @Test
  public void testParsingString() {
    OperaArguments parsed = OperaArguments.parse("-foo -bar baz -bah=abc");
    Assert.assertEquals("foo", parsed.getArguments().get(0).getArgument());
    Assert.assertEquals("bar", parsed.getArguments().get(1).getArgument());
    Assert.assertEquals("baz", parsed.getArguments().get(1).getValue());
    Assert.assertEquals("bah", parsed.getArguments().get(2).getArgument());
  }

  @Test
  public void testParsingStringWithArgumentSign() {
    OperaArguments parsed = OperaArguments.parse("--foo --bar baz --bah=abc");
    Assert.assertEquals("foo", parsed.getArguments().get(0).getArgument());
    Assert.assertEquals("bar", parsed.getArguments().get(1).getArgument());
    Assert.assertEquals("baz", parsed.getArguments().get(1).getValue());
    Assert.assertEquals("bah", parsed.getArguments().get(2).getArgument());
  }

  @Test
  public void testMerge() {
    OperaArguments extraArguments = new OperaArguments();
    extraArguments.add("abc", "def");
    extraArguments.add("ghi");
    arguments.merge(extraArguments);
    Assert.assertEquals("foo", arguments.getArguments().get(0).getArgument());
    Assert.assertEquals("abc", arguments.getArguments().get(3).getArgument());
    Assert.assertEquals("def", arguments.getArguments().get(3).getValue());
    Assert.assertEquals("ghi", arguments.getArguments().get(4).getArgument());
    Assert.assertNull(arguments.getArguments().get(4).getValue());
  }


  // Extensions follow.


  @Test
  public void testConstructingOperaCoreArguments() {
    OperaArguments arguments = new OperaCoreArguments();
    Assert.assertNotNull(arguments);
    Assert.assertEquals("autotestmode", arguments.getArguments().get(0).getArgument());
  }

  @Test
  public void testConstructingOperaDesktopArguments() {
    OperaArguments arguments = new OperaDesktopArguments();
    Assert.assertNotNull(arguments);
    Assert.assertEquals("autotestmode", arguments.getArguments().get(0).getArgument());
  }

}