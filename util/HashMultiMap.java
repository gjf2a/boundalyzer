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

import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;

public class HashMultiMap<K,V> implements MultiMap<K,V> {
    private HashMap<K,Set<V>> map;
    private int size;
    
    public HashMultiMap() {
        map = new HashMap<K,Set<V>>();
        size = 0;
    }

    public Set<V> allFor(K key) {
        return containsKey(key) ? map.get(key) : new HashSet<V>();
    }

    public boolean containsKey(K key) {
        return map.keySet().contains(key);
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public Set<K> keySet() {
        return map.keySet();
    }

    public void put(K key, V value) {
        if (!map.containsKey(key)) {
            map.put(key, new HashSet<V>());
        }
        int sz = map.get(key).size();
        map.get(key).add(value);
        if (map.get(key).size() > sz) {
            size += 1;
        }
    }

    public void remove(K key, V value) {
        if (map.containsKey(key) && map.get(key).contains(value)) {
            map.get(key).remove(value);
            size -= 1;
            if (map.get(key).isEmpty()) {
                map.remove(key);
            }
        }
    }

    public int size() {
        return size;
    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }

}
