/*
Copyright 2008-2011 Opera Software ASA

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
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;


/**
 * A thread safe stack hash map for use in window manager
 * The backing map is a {@link LinkedHashMap} that provides
 * predictable iteration order. All the operations that require
 * thread safety are protected by the lock of synchronized map.
 *
 * @author Deniz Turkoglu <denizt@opera.com>
 *
 * @param <K>
 * @param <V>
 */
public class StackHashMap<K,V> implements ConcurrentMap<K,V> {

	private final Map<K, V> map;
	private final LinkedList<K> list;

	public StackHashMap() {
		map = Collections.synchronizedMap(new LinkedHashMap<K,V>());
		list = new LinkedList<K>();
	}

	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}

	public V get(Object key) {
		return map.get(key);
	}

	public V put(K key, V value) {
		synchronized(map) {
			//check if we already have the key
			if(!map.containsKey(key))
				list.addLast(key); //if not add last
			//if we already have the key, just update the value
			else
				 value = (value == null) ? map.get(key) : value; //we already know this key and don't want to nullify it
			return map.put(key, value);
		}
	}

	public void putAll(Map<? extends K, ? extends V> t) {
		synchronized (map) {

			Iterator<? extends Entry<? extends K, ? extends V>> itr = t.entrySet().iterator();
			while (itr.hasNext()) {
				Entry<? extends K, ? extends V> entry = itr.next();
				put(entry.getKey(), entry.getValue());
			}
		}
	}

	public Set<K> keySet() {
		throw new UnsupportedOperationException();
	}

	public Collection<V> values() {
		return map.values();
	}

	public Set<Map.Entry<K, V>> entrySet() {
		throw new UnsupportedOperationException();
	}

	public int size() {
		return map.size();
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	public V remove(Object key) {
		synchronized (map) {
			list.remove(key);
			return map.remove(key);
		}
	}

	public void clear() {
		synchronized (map) {
			list.clear();
			map.clear();
		}
	}

	/**
	 * Removes the first value from the map
	 * @return the value that was removed
	 */
	public V pop() {
		synchronized (map) {
			return map.remove(list.removeFirst());
		}
	}

	public V push(K k,V v) {
		synchronized (map) {
			list.addFirst(k);
			return map.put(k, v);
		}
	}

	/**
	 * @return the first value in the backing map
	 */
	public V peek() {
		synchronized (map) {
			K k = peekKey();
			return (k == null) ? null : map.get(k);
		}
	}

	/**
	 * @return the first key in the backing linked list
	 */
	public K peekKey() {
		return (list.isEmpty()) ? null : list.getFirst();
	}

	/**
	 * @return an unmodifiable copy of the backing linkedlist(used as a stack)
	 */
	public List<K> asStack() {
		return Collections.unmodifiableList(list);
	}

	/**
	 * @return an unmodifiable copy of the backing map
	 */
	public Map<K,V> asMap() {
		return Collections.unmodifiableMap(map);
	}

	/**
	 * Puts a key to top of the map if absent
	 * if the key is present in stack it is removed
	 * @param k
	 * @param v
	 * @return the value if it is not contained, null otherwise
	 */
	public V putIfAbsent(K k, V v) {
		synchronized (map) {
			if(!containsKey(k)) {
				list.addFirst(k);
				return map.put(k, v);
			} else {
				list.remove(k);
			}
			list.addFirst(k);
			return null;
		}
	}



	public boolean remove(Object key, Object value) {
		throw new UnsupportedOperationException();
	}

	public boolean replace(K key, V oldValue, V newValue) {
		throw new UnsupportedOperationException();
	}

	public V replace(K key, V value) {
		throw new UnsupportedOperationException();
	}

}
