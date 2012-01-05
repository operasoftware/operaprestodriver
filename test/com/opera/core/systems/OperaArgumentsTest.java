/*
Copyright 2011-2012 Opera Software ASA

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.opera.core.systems;

import com.opera.core.systems.arguments.OperaArgument;
import com.opera.core.systems.arguments.OperaCoreArguments;
import com.opera.core.systems.arguments.OperaDesktopArguments;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class OperaArgumentsTest {

  private static OperaArguments arguments;

  @Test
  public void testConstructingOperaArguments() {
    arguments = new OperaArguments();
    assertNotNull(arguments);
  }

  @Test
  public void testAddArgumentWithKey() {
    arguments.add("foo");
    assertNotNull(arguments.getArguments());
    assertEquals(1, arguments.size());
  }

  @Test
  public void testAddArgumentWithKeyAndValue() {
    arguments.add("foo", "bar");
    assertEquals("foo", arguments.get(1).getArgument());
    assertEquals("bar", arguments.get(1).getValue());
  }

  @Test
  public void testAddArgumentWithObject() {
    OperaArgument argument = new OperaArgument("hooba", "flooba");
    arguments.add(argument);
    assertEquals("hooba", arguments.get(2).getArgument());
    assertEquals("flooba", arguments.get(2).getValue());
  }
  
  @Test
  public void testSize() {
    assertEquals(3, arguments.size());
  }

  @Test
  public void testGettingArgumentsAsObjectList() {
    assertTrue(arguments.iterator().hasNext());
  }

  @Test
  public void testGettingArgumentsAsStringList() {
    assertEquals(5, arguments.getArgumentsAsStringList().size());
    assertEquals("-hooba", arguments.getArgumentsAsStringList().get(3));
  }

  @Test
  public void testParsingString() {
    OperaArguments parsed = OperaArguments.parse("-foo -bar baz -bah=abc");
    assertEquals("foo", parsed.get(0).getArgument());
    assertEquals("bar", parsed.get(1).getArgument());
    assertEquals("baz", parsed.get(1).getValue());
    assertEquals("bah", parsed.get(2).getArgument());
  }

  @Test
  public void testParsingStringWithArgumentSign() {
    OperaArguments parsed = OperaArguments.parse("--foo --bar baz --bah=abc");
    assertEquals("foo", parsed.get(0).getArgument());
    assertEquals("bar", parsed.get(1).getArgument());
    assertEquals("baz", parsed.get(1).getValue());
    assertEquals("bah", parsed.get(2).getArgument());
  }

  @Test
  public void testParsingComplexString() {
    OperaArguments parsed = OperaArguments.parse(
        "-foo bar -baz=bah -path /path/to/somewhere -anotherpath=C:\\another\\path -a==abc /b hei --c \"hei\"");
    assertEquals("foo", parsed.get(0).getArgument());
    assertEquals("bar", parsed.get(0).getValue());
    assertEquals("baz", parsed.get(1).getArgument());
    assertEquals("bah", parsed.get(1).getValue());
    assertEquals("path", parsed.get(2).getArgument());
    assertEquals("/path/to/somewhere", parsed.get(2).getValue());
    assertEquals("anotherpath", parsed.get(3).getArgument());
    assertEquals("C:\\another\\path", parsed.get(3).getValue());
    assertEquals("a", parsed.get(4).getArgument());
    assertEquals("=abc", parsed.get(4).getValue());
    assertEquals("b", parsed.get(5).getArgument());
    assertEquals("hei", parsed.get(5).getValue());
    assertEquals("c", parsed.get(6).getArgument());
    assertEquals("hei", parsed.get(6).getValue());
  }

  @Test
  public void testParsingJustOneArgument() {
    OperaArguments parsed = OperaArguments.parse("-foo");
    assertEquals("foo", parsed.get(0).getArgument());
    assertNull(parsed.get(0).getValue());
    assertEquals(1, parsed.size());
  }

  @Test
  public void testMerge() {
    OperaArguments extraArguments = new OperaArguments();
    extraArguments.add("abc", "def");
    extraArguments.add("ghi");
    arguments.merge(extraArguments);
    assertEquals("foo", arguments.get(0).getArgument());
    assertEquals("abc", arguments.get(3).getArgument());
    assertEquals("def", arguments.get(3).getValue());
    assertEquals("ghi", arguments.get(4).getArgument());
    assertNull(arguments.get(4).getValue());
  }

  // Extensions follow.

  @Test
  @Ignore("Currently disabled due to bug in core-gogi")
  public void testConstructingOperaCoreArguments() {
    OperaArguments arguments = new OperaCoreArguments();
    assertNotNull(arguments);
    assertEquals("autotestmode", arguments.get(0).getArgument());
  }

  @Test
  @Ignore(
      "Currently disabled due to bug in OperaCoreArguments, which OperaDesktopArguments extends")
  public void testConstructingOperaDesktopArguments() {
    OperaArguments arguments = new OperaDesktopArguments();
    assertNotNull(arguments);
    assertEquals("autotestmode", arguments.get(0).getArgument());
  }

}