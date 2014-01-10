package com.xinqihd.sns.gameserver.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * It is a very simple and thread-safe Map implementation that is used
 * to wrap simple key-value pairs normally less 10 elements. 
 * 
 * @author wangqi
 *
 * @param <K>
 * @param <V>
 */
public class ArrayMap<K, V> implements Map<K, V> {
	
	private static final UnsupportedOperationException unsupportEx = new UnsupportedOperationException("ArrayMap is readonly.");
	private final K[] keys;
	private final V[] values;
	
	/**
	 * Create the ArrayMap from keys and values array. 
	 * Note: the length of the two array must be same.
	 * 
	 * @param keys
	 * @param values
	 */
	private ArrayMap(final K[] keys, final V[] values) {
		this.keys = keys;
		this.values = values;
	}
	
	/**
	 * Factory method to create an ArrayMap.
	 * Note: the length of the two array must be same.
	 * 
	 * @param keys
	 * @param values
	 * @return
	 */
	public static final <K,V> ArrayMap<K,V> createMap(final K[] keys, final V[] values) {
		if ( keys == null || values == null || keys.length != values.length || keys.length == 0) {
			throw new IllegalArgumentException("ArrayMap's keys and values are illegal. keys:"+keys+", values: " + values +
					", keys.length:"+keys.length+", values.length:"+values.length);
		}
		ArrayMap<K,V> arrayMap = new ArrayMap<K,V>(keys, values);
		return arrayMap;
	}

	@Override
	public final int size() {
		return keys.length;
	}

	@Override
	public final boolean isEmpty() {
		return false;
	}

	/**
	 * Note: it will consume O(N) time to do this operation.
	 * 
	 */
	@Override
	public final boolean containsKey(Object key) {
		for ( Object k : keys ) {
			if ( k == key || k.equals(key) ) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Note: it will consume O(N) time to do this operation.
	 * 
	 */
	@Override
	public final boolean containsValue(Object value) {
		for ( Object v : values ) {
			if ( v == value || v.equals(value) ) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Note: it will consume O(N) time to do this operation.
	 * 
	 */
	@Override
	public final V get(Object key) {
		for ( int i=0; i<keys.length; i++ ) {
			K k = keys[i];
			if ( k == key || k.equals(key) ) {
				return values[i];
			}
		}
		return null;
	}

	/**
	 * It will throw an UnsupportedOperationException because this
	 * map is read-only.
	 */
	@Override
	public final V put(K key, V value) {
		unsupportEx.fillInStackTrace();
		throw unsupportEx;
	}

	/**
	 * It will throw an UnsupportedOperationException because this
	 * map is read-only.
	 */
	@Override
	public final V remove(Object key) {
		unsupportEx.fillInStackTrace();
		throw unsupportEx;
	}

	/**
	 * It will throw an UnsupportedOperationException because this
	 * map is read-only.
	 */
	@Override
	public final void putAll(Map<? extends K, ? extends V> m) {
		unsupportEx.fillInStackTrace();
		throw unsupportEx;
	}

	/**
	 * It will throw an UnsupportedOperationException because this
	 * map is read-only.
	 */
	@Override
	public final void clear() {
		unsupportEx.fillInStackTrace();
		throw unsupportEx;		
	}

	@Override
	public final Set<K> keySet() {
		KeySet<K> set = new KeySet<K>(this.keys);
		return set;
	}

	@Override
	public final Collection<V> values() {
		ArrayCollection<V> list = new ArrayCollection<V>(values);
		return list;
	}

	@Override
	public final Set<java.util.Map.Entry<K, V>> entrySet() {
		ArrayMap.Entry<K,V>[] entries = new ArrayMap.Entry[keys.length];
		for ( int i=0; i<keys.length; i++ ) {
			entries[i] = new ArrayMap.Entry<K,V>(keys[i], values[i]);
		}
		KeySet<java.util.Map.Entry<K, V>> set = new KeySet<java.util.Map.Entry<K, V>>(entries);
		return set;
	}
	
	private static final class Entry<K,V> implements java.util.Map.Entry<K, V> {
		
		private K key;
		private V value;
		
		public Entry(K k, V v) {
			this.key = k;
			this.value = v;
		}

		@Override
		public K getKey() {
			return key;
		}

		@Override
		public V getValue() {
			return value;
		}

		@Override
		public V setValue(V value) {
			return this.value = value;
		}

	}
	
	private static final class KeySet<K> implements Set<K> {
		
		private final K[] keys;
		
		public KeySet(K[] keys) {
			this.keys = keys;
		}

		@Override
		public int size() {
			return this.keys.length;
		}

		@Override
		public boolean isEmpty() {
			return keys.length == 0;
		}

		/* (non-Javadoc)
		 * @see java.util.Set#contains(java.lang.Object)
		 */
		@Override
		public boolean contains(Object o) {
			for ( K k : keys ) {
				if ( k == o || k.equals(o) ) {
					return true;
				}
			}
			return false;
		}

		@Override
		public Iterator<K> iterator() {
			return new ArrayIterator<K>(keys);
		}

		/* (non-Javadoc)
		 * @see java.util.Set#toArray()
		 */
		@Override
		public Object[] toArray() {
			return keys;
		}

		@Override
		public <T> T[] toArray(T[] a) {
      // Estimate size of array; be prepared to see more or fewer elements
      int size = size();
      T[] r = a.length >= size ? a :
                (T[])java.lang.reflect.Array
                .newInstance(a.getClass().getComponentType(), size);
      Iterator<K> it = iterator();

      for (int i = 0; i < r.length; i++) {
          if (! it.hasNext()) { // fewer elements than expected
              if (a != r)
                  return Arrays.copyOf(r, i);
              r[i] = null; // null-terminate
              return r;
          }
          r[i] = (T)it.next();
      }
      return it.hasNext() ? finishToArray(r, it) : r;
		}

		@Override
		public boolean add(K e) {
			unsupportEx.fillInStackTrace();
			throw unsupportEx;	
		}

		@Override
		public boolean remove(Object o) {
			unsupportEx.fillInStackTrace();
			throw unsupportEx;	
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			for (Iterator<?> iterator = c.iterator(); iterator.hasNext();) {
				Object object = iterator.next();
				for ( Object k : this.keys ) {
					if ( !(k == object || k.equals(object)) ) {
						return false;
					}
				}
			}
			return true;
		}

		@Override
		public boolean addAll(Collection<? extends K> c) {
			unsupportEx.fillInStackTrace();
			throw unsupportEx;	
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			unsupportEx.fillInStackTrace();
			throw unsupportEx;	
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			unsupportEx.fillInStackTrace();
			throw unsupportEx;	
		}

		@Override
		public void clear() {
			unsupportEx.fillInStackTrace();
			throw unsupportEx;	
		}
	}
	
	private static final class ArrayCollection<V> implements Collection<V> {
		
		private final V[] values;
		
		public ArrayCollection(V[] values) {
			this.values = values;
		}

		/* (non-Javadoc)
		 * @see java.util.Collection#size()
		 */
		@Override
		public int size() {
			return this.values.length;
		}

		/* (non-Javadoc)
		 * @see java.util.Collection#isEmpty()
		 */
		@Override
		public boolean isEmpty() {
			return this.values.length == 0;
		}

		/* (non-Javadoc)
		 * @see java.util.Collection#contains(java.lang.Object)
		 */
		@Override
		public boolean contains(Object o) {
			for ( V value : values ) {
				if ( value == o || value.equals(o) ) {
					return true;
				}
			}
			return false;
		}

		/* (non-Javadoc)
		 * @see java.util.Collection#iterator()
		 */
		@Override
		public Iterator<V> iterator() {
			ArrayIterator<V> iter = new ArrayIterator<V>(values);
			return iter;
		}

		@Override
		public Object[] toArray() {
			return values;
		}

		@Override
		public <T> T[] toArray(T[] a) {
      // Estimate size of array; be prepared to see more or fewer elements
      int size = size();
      T[] r = a.length >= size ? a :
                (T[])java.lang.reflect.Array
                .newInstance(a.getClass().getComponentType(), size);
      Iterator<V> it = iterator();

      for (int i = 0; i < r.length; i++) {
          if (! it.hasNext()) { // fewer elements than expected
              if (a != r)
                  return Arrays.copyOf(r, i);
              r[i] = null; // null-terminate
              return r;
          }
          r[i] = (T)it.next();
      }
      return it.hasNext() ? finishToArray(r, it) : r;
		}
		
		/* (non-Javadoc)
		 * @see java.util.Collection#containsAll(java.util.Collection)
		 */
		@Override
		public boolean containsAll(Collection<?> c) {
			for (Iterator iterator = c.iterator(); iterator.hasNext();) {
				Object object = (Object) iterator.next();
				for ( V value : values ) {
					if ( !(value == object || value.equals(object)) ) {
						return false;
					}
				}
			}
			return true;
		}

		/* (non-Javadoc)
		 * @see java.util.Collection#add(java.lang.Object)
		 */
		@Override
		public boolean add(V e) {
			unsupportEx.fillInStackTrace();
			throw unsupportEx;	
		}

		/* (non-Javadoc)
		 * @see java.util.Collection#remove(java.lang.Object)
		 */
		@Override
		public boolean remove(Object o) {
			unsupportEx.fillInStackTrace();
			throw unsupportEx;	
		}

		/* (non-Javadoc)
		 * @see java.util.Collection#addAll(java.util.Collection)
		 */
		@Override
		public boolean addAll(Collection<? extends V> c) {
			unsupportEx.fillInStackTrace();
			throw unsupportEx;	
		}

		/* (non-Javadoc)
		 * @see java.util.Collection#removeAll(java.util.Collection)
		 */
		@Override
		public boolean removeAll(Collection<?> c) {
			unsupportEx.fillInStackTrace();
			throw unsupportEx;	
		}

		/* (non-Javadoc)
		 * @see java.util.Collection#retainAll(java.util.Collection)
		 */
		@Override
		public boolean retainAll(Collection<?> c) {
			unsupportEx.fillInStackTrace();
			throw unsupportEx;	
		}

		/* (non-Javadoc)
		 * @see java.util.Collection#clear()
		 */
		@Override
		public void clear() {
			unsupportEx.fillInStackTrace();
			throw unsupportEx;	
		}
		
	}

	private static final class ArrayIterator<T> implements Iterator<T> {
		
		private T[] elements = null;
		private int index = 0;
		
		public ArrayIterator(T[] elements) {
			this.elements = elements;
		}

		@Override
		public boolean hasNext() {
			return index < elements.length;
		}

		@Override
		public T next() {
			return elements[index++];
		}

		@Override
		public void remove() {
			unsupportEx.fillInStackTrace();
			throw unsupportEx;	
		}
	}
	
	private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
  private static final <T> T[] finishToArray(T[] r, Iterator<?> it) {
    int i = r.length;
    while (it.hasNext()) {
        int cap = r.length;
        if (i == cap) {
            int newCap = cap + (cap >> 1) + 1;
            // overflow-conscious code
            if (newCap - MAX_ARRAY_SIZE > 0)
                newCap = hugeCapacity(cap + 1);
            r = Arrays.copyOf(r, newCap);
        }
        r[i++] = (T)it.next();
    }
    // trim if overallocated
    return (i == r.length) ? r : Arrays.copyOf(r, i);
  }
  
  private static int hugeCapacity(int minCapacity) {
    if (minCapacity < 0) // overflow
        throw new OutOfMemoryError
            ("Required array size too large");
    return (minCapacity > MAX_ARRAY_SIZE) ?
        Integer.MAX_VALUE :
        MAX_ARRAY_SIZE;
  }
}
