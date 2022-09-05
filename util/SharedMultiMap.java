// Copyright 2009 by Gabriel J. Ferrer
//
// This program is part of the Boundalyzer project.
// 
// Boundalyzer is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// Boundalyzer is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with Boundalyzer.  If not, see <http://www.gnu.org/licenses/>.

package util;

public class SharedMultiMap<K extends Comparable<? super K>,V extends Comparable<? super V>> {
    private SharedMap<K,SharedSet<V>> multi;
    
    public SharedMultiMap() {
        this(new SharedTreeMap<K,SharedSet<V>>());
    }
    
    private SharedMultiMap(SharedMap<K,SharedSet<V>> sms) {
        multi = sms;
    }
    
    public boolean equals(Object other) {
        if (other instanceof SharedMultiMap) {
            SharedMultiMap<K,V> that = (SharedMultiMap<K,V>)other;
            return this.multi.equals(that.multi);
        } else {
            return false;
        }
    }
    
    public SharedMultiMap<K,V> with(K key, V value) {
        SharedSet<V> values = multi.contains(key) ? multi.get(key) : new SharedTreeSet<V>();
        values = values.with(value);
        return new SharedMultiMap<K,V>(multi.with(key, values));
    }
    
    public SharedMultiMap<K,V> without(K key, V value) {
        if (multi.contains(key)) {
            SharedSet<V> values = multi.get(key);
            if (values.contains(value)) {
                return new SharedMultiMap<K,V>(multi.with(key, values.without(value)));
            }
        }
        
        return this;
    }
    
    public SharedMultiMap<K,V> withReplacedKey(K oldKey, K newKey) {
    	if (multi.contains(oldKey)) {
    		return new SharedMultiMap<K,V>(multi.withReplacedKey(oldKey, newKey));
    	} else {
    		return this;
    	}
    }
    
    public boolean contains(K key) {return multi.contains(key);}
    
    // Pre: None
    // Post: Returns a set containing all elements keyed by key
    public SharedSet<V> allFor(K key) {
        if (contains(key)) {
            return multi.get(key);
        } else {
            return new SharedTreeSet<V>();
        }
    }
    
    public SharedSet<K> keySet() {return multi.keySet();}
    
    public boolean isEmpty() {return multi.isEmpty();}
}
