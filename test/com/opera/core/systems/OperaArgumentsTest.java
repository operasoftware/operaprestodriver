/*
Copyright 2011 Opera Software ASA

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
  public void testAddArgumentWithOperaArgument() {
    OperaArgument argument = new OperaArgument("hooba", "flooba");
    arguments.add(argument);
    assertEquals("hooba", arguments.get(2).getArgument());
    assertEquals("flooba", arguments.get(2).getValue());
  }

  @Test
  public void testGettingArgumentsAsObjectList() {
    assertEquals(3, arguments.size());
  }

  @Test
  public void testGettingArgumentsAsStringList() {
    assertEquals(5, arguments.getArgumentsAsStringList().size());
    assertEquals("hooba", arguments.getArgumentsAsStringList().get(3));
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
  @Ignore("Currently disabled due to bug in OperaCoreArguments, which this derives off")
  public void testConstructingOperaDesktopArguments() {
    OperaArguments arguments = new OperaDesktopArguments();
    assertNotNull(arguments);
    assertEquals("autotestmode", arguments.get(0).getArgument());
  }

}