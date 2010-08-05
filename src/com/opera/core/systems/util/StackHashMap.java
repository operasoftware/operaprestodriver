package com.opera.core.systems.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * A thread safe stack hash map for use in window manager
 * The backing map is a {@link LinkedHashMap} that provides
 * predictable iteration order. All the operations that require
 * thread safety are protected by the lock of synchronized map.
 * 
 * @author Deniz Turkoglu
 *
 * @param <K>
 * @param <V>
 */
public class StackHashMap<K,V> implements Map<K,V> {
	
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
			if(!list.contains(key))
				list.addLast(key); //if not add last
			//if we already have the key, just update the value
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
		return map.keySet();
	}

	public Collection<V> values() {
		return map.values();
	}

	public Set<Map.Entry<K, V>> entrySet() {
		return map.entrySet();
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
	
	public V peek() {
		synchronized (map) {
			K k = peekKey();
			return (k == null) ? null : map.get(k);
		}
	}
	
	public K peekKey() {
		return (list.isEmpty()) ? null : list.getFirst();
	}
	
	public List<K> asStack() {
		return Collections.unmodifiableList(list);
	}
	
	public Map<K,V> asMap() {
		return Collections.unmodifiableMap(map);
	}
	
	public V pushIfAbsent(K k, V v) {
		synchronized (map) {
			if(!list.contains(k)) {
				return map.put(k, v);				
			} else {
				list.remove(k);
			}
			list.addFirst(k);
			return null;
		}
	}

}
