package com.opera.core.systems.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A set that stores strings and compares them case-insensitively.
 * @author stuartk
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
   * Returns true if this set contains the specified string, compared
   * case-insensitively.
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
