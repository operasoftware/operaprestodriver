/*
Copyright 2012 Opera Software ASA

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

package com.opera.core.systems.environment;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

/**
 * @author Andreas Tolf Tolfsen <andreastt@opera.com>
 */
public abstract class DefaultTestEnvironment implements TestEnvironment {

  /**
   * Set an environment variable inside the JVM.
   *
   * http://stackoverflow.com/questions/318239/how-do-i-set-environment-variables-from-java/496849#496849
   *
   * @param key   the new environment variable's identifier
   * @param value the new environment variable's value
   */
  @SuppressWarnings("unchecked")
  public void set(String key, String value) {
    try {
      Class<?>[] classes = Collections.class.getDeclaredClasses();
      Map<String, String> env = System.getenv();
      for (Class<?> cl : classes) {
        if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
          Field field = cl.getDeclaredField("m");
          field.setAccessible(true);
          Object obj = field.get(env);
          Map<String, String> map = (Map<String, String>) obj;
          map.put(key, value);
        }
      }
    } catch (IllegalAccessException e) {
      // nothing we can do
    } catch (NoSuchFieldException e) {
      // nothing we can do
    }
  }

  /**
   * Clears an environmental variable from inside the JVM.
   *
   * @param key the environmental variable to remove
   */
  @SuppressWarnings("unchecked")
  public void unset(String key) {
    try {
      Class<?>[] classes = Collections.class.getDeclaredClasses();
      Map<String, String> env = System.getenv();
      for (Class<?> cl : classes) {
        if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
          Field field = cl.getDeclaredField("m");
          field.setAccessible(true);
          Object obj = field.get(env);
          Map<String, String> map = (Map<String, String>) obj;
          //map.put(key, value);
          map.remove(key);
        }
      }
    } catch (IllegalAccessException e) {
      // nothing we can do
    } catch (NoSuchFieldException e) {
      // nothing we can do
    }
  }

}