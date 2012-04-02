/*
Copyright 2008-2012 Opera Software ASA

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

package com.opera.core.systems.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A set that stores strings and compares them case-insensitively.
 *
 * @author Stuart Knightley <stuartk@opera.com>
 */
public class CaseInsensitiveStringSet implements Set<String> {

  HashSet<String> set = new HashSet<String>();

  public boolean add(String e) {
    return set.add(e.toLowerCase());
  }

  public boolean addAll(Collection<? extends String> c) {
    return false;
  }

  public void clear() {
    set.clear();
  }

  /**
   * Returns true if this set contains the specified string, compared case-insensitively.
   */
  public boolean contains(Object o) {
    if (o instanceof String) {
      return set.contains(((String) o).toLowerCase());
    } else {
      return set.contains(o);
    }
  }

  public boolean containsAll(Collection<?> c) {
    return false;
  }

  public boolean isEmpty() {
    return set.isEmpty();
  }

  public Iterator<String> iterator() {
    return set.iterator();
  }

  public boolean remove(Object o) {
    if (o instanceof String) {
      return set.remove(((String) o).toLowerCase());
    } else {
      return set.remove(o);
    }
  }

  public boolean removeAll(Collection<?> c) {
    return false;
  }

  public boolean retainAll(Collection<?> c) {
    return false;
  }

  public int size() {
    return set.size();
  }

  public Object[] toArray() {
    return set.toArray();
  }

  public <T> T[] toArray(T[] a) {
    return set.toArray(a);
  }

}