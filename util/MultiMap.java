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
import java.util.Set;

public interface MultiMap<K,V> {
    // Pre: None
    // Post: contains(key); allFor(key).contains(value); keySet.contains(key)
    //       !isEmpty(); size() = old size() + 1
    public void put(K key, V value);
    
    // Pre: None
    // Post: !allFor(key).contains(value); 
    //       contains(key) => size() == old size() - 1
    //       allFor(key).size() == 1 => !contains(key); !keySet.contains(key)
    //       contains(key) && size() == 1 => isEmpty()
    public void remove(K key, V value);
    
    // Pre: None
    // Post: Returns true if allFor(key).size() > 0
    public boolean containsKey(K key);
    
    // Pre: None
    // Post: Returns all values with key 
    public Set<V> allFor(K key);
    
    // Pre: None
    // Post: Returns all keys for which allFor(key).size() > 0
    public Set<K> keySet();
    
    // Pre: None
    // Post: Returns true if size() == 0; returns false otherwise
    public boolean isEmpty();
    
    // Pre: None
    // Post: Returns # of values added
    public int size();
}
