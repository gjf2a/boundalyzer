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


// Uses the Andersson algorithm
// _Persistent_ balanced binary trees; changes to the tree are not visible to 
// other references.  All trees share common contents.
//
// Set operations (union, intersection) are also included.
//
// This is useful for keeping maps of variables in scope.
// As we process source code in a linear ordering, upon entering
// curly braces the new variables introduced will later go out of
// scope.  This class will let us "revert" by holding on to a 
// reference prior to reaching the curly braces.

// What would be ideal would be to create SharedSet and SharedMap interfaces,
// which TreeSharedSet and TreeSharedMap would implement.  This would be more
// consonant with the way that the Collections library works.

package util;
import java.util.*;

public class SharedTreeMap<K extends Comparable<? super K>,V> implements SharedMap<K,V>, SharedSet<K>, Iterable<K> {
    private K key;
    private V value;
    private SharedTreeMap<K,V> left, right;
    private int level, size;
    
    public SharedTreeMap() {
        this(null, null, null, null, 0);
    }
    
    public boolean isEmpty() {return key == null;}
    
    public boolean equals(Object other) {
        if (other instanceof SharedMap) {
            SharedMap<K,V> that = (SharedMap<K,V>)other;
            if (this.size() != that.size()) {return false;}
            
            for (K key: this) {
                if (!that.contains(key) || !that.get(key).equals(this.get(key))) {
                    return false;
                }
            }
            return true;
            
        } else if (other instanceof SharedSet) {
            SharedSet<K> that = (SharedSet<K>)other;
            if (this.size() != that.size()) {return false;}
            for (K key: this) {if (!that.contains(key)) {return false;}}
            return true;
            
        } else {
            return false;
        }
    }
    
    public SharedSet<K> keySet() {return this;}
    
    public K getKey() {return key;}
    public V getValue() {return value;}
    
    public V get(K target) {
        if (isEmpty()) {return null;}
        
        int compare = target.compareTo(getKey());
        if (compare == 0) {
            return getValue();
        } else if (compare > 0) {
            return right.get(target);
        } else {
            return left.get(target);
        }
    }
    
    public boolean contains(K target) {
        if (isEmpty()) {return false;}
        int compare = target.compareTo(getKey());
        if (compare == 0) {
            return true;
        } else if (compare > 0) {
            return right.contains(target);
        } else {
            return left.contains(target);
        }
    }
    
    public int size() {return size;}
    
    // Pre: 0 <= n < size()
    // Post: Returns nth-ranked key
    public K nth(int n) {
        if (n == left.size()) {
            return getKey();
        } else if (n > left.size()) {
            return right.nth(n - left.size() - 1);
        } else {
            return left.nth(n);
        }
    }
    
    public SharedTreeMap<K,V> with(K target, V val) {
        if (this.isEmpty()) {
            return new SharedTreeMap<K,V>(target, val, this, this, 1);
        }
        
        SharedTreeMap<K,V> result;
        int compare = target.compareTo(this.getKey());
        if (compare == 0) {
            result = new SharedTreeMap<K,V>(target, val, left, right, size);
        } else if (compare > 0) {
            result = new SharedTreeMap<K,V>(this, left, right.with(target, val));
        } else {
            result = new SharedTreeMap<K,V>(this, left.with(target, val), right);
        }
        return result.rebalanced();
    }
    
    public SharedSet<K> with(K target) {
        return this.with(target, null);
    }
        
    public SharedSet<K> with(SharedSet<K> other) {
        if (this.size() < other.size()) {
            return other.with(this);
        } else {
            SharedSet<K> result = this;
            for (K t: other) {
                result = result.with(t);
            }
            return result;
        }
    }
    
    public SharedMap<K,V> withReplacedKey(K oldKey, K newKey) {
    	V value = this.get(oldKey);
    	return this.without(oldKey).with(newKey, value);
    }
    
    public SharedTreeMap<K,V> without(K target) {
        if (isEmpty()) {return this;}
        
        SharedTreeMap<K,V> result = null;
        int compare = target.compareTo(getKey());
        if (compare == 0) {
            if (left.isEmpty()) {
                result = right;
            } else {
                K key = left.getMax();
                result = new SharedTreeMap<K,V>(key, left.get(key), left.withoutMax(), right, size - 1);
            }
        } else {
            if (compare > 0) {
                result = new SharedTreeMap<K,V>(this, left, right.without(target));
            } else {
                result = new SharedTreeMap<K,V>(this, left.without(target), right);
            }
        }
        return result.rebalanced();
    }
    
    public SharedTreeMap<K,V> union(SharedTreeMap<K,V> other) {
        SharedTreeMap<K,V> onion = this;
        for (K k: other) {onion = onion.with(k, other.get(k));}
        return onion;
    }
    
    public SharedTreeMap<K,V> intersection(SharedTreeMap<K,V> other) {
        SharedTreeMap<K,V> inter = new SharedTreeMap<K,V>();
        for (K k: this) {
            if (other.contains(k)) {inter = inter.with(k, other.get(k));}
        }
        return inter;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        appendSelf(sb, 0);
        return sb.toString();
    }
    
    public String inOrder() {
        StringBuilder sb = new StringBuilder();
        buildInOrder(sb);
        return sb.toString();
    }
    
    public Iterator<K> iterator() {return new SelfIter(this);}
    
    public K getMin() {
        return left.isEmpty() ? getKey() : left.getMin();
    }
    
    public K getMax() {
        return right.isEmpty() ? getKey() : right.getMax();
    }
    
    // Private methods
    
    private SharedTreeMap(K k, V v, SharedTreeMap<K,V> left, SharedTreeMap<K,V> right, int sz) {
        key = k;
        value = v;
        this.left = left;
        this.right = right;
        if (left == null) {
            level = 0;
        } else {
            level = computeLevel();
        }
        size = sz;
    }
    
    private SharedTreeMap(SharedTreeMap<K,V> src) {
        this(src, src.left, src.right);
    }
    
    private SharedTreeMap(SharedTreeMap<K,V> src, SharedTreeMap<K,V> left, SharedTreeMap<K,V> right) {
        this(src.key, src.value, left, right, left.size + right.size + 1);
        level = src.level;
    }
    
    private SharedTreeMap<K,V> rebalanced() {
        SharedTreeMap<K,V> result = this;
        result = result.gapsFixed();
        result = result.enforceLeftRule();
        result = result.enforceRightRule();
        return result;
    }

    private int computeLevel() {
        return isEmpty() ? 0 : left.level + 1;
    }
    
    private SharedTreeMap<K,V> enforceLeftRule() {
        if (!isEmpty() && level != computeLevel()) {
            if (left.level == right.level) {
                return new SharedTreeMap<K,V>(this);
            } else {
                SharedTreeMap<K,V> newThis = new SharedTreeMap<K,V>(this, this.left.right, this.right);
                newThis.level = newThis.computeLevel(); // Shouldn't matter...
                SharedTreeMap<K,V> newParent = new SharedTreeMap<K,V>(this.left, this.left.left, newThis.enforceLeftRule());
                return newParent;
            }
        } else {
            return this;
        }
    }
    
    private SharedTreeMap<K,V> enforceRightRule() {
        if (!isEmpty() && !right.isEmpty() && !right.right.isEmpty() && right.right.level == level) {
            SharedTreeMap<K,V> newThis = new SharedTreeMap<K,V>(this, this.left, this.right.left.enforceRightRule());
            SharedTreeMap<K,V> newParent = new SharedTreeMap<K,V>(this.right, newThis, this.right.right);
            newParent.level = newParent.computeLevel(); // Shouldn't matter...
            return newParent;
        } else {
            return this;
        }
    }
    
    private SharedTreeMap<K,V> gapsFixed() {
        if (!isEmpty() && (level > left.level + 1 || level > right.level + 1)) {
            SharedTreeMap<K,V> newThis = new SharedTreeMap<K,V>(this);
            newThis.level = this.level - 1;
            if (newThis.right.level > newThis.level) {
                SharedTreeMap<K,V> newRight = new SharedTreeMap<K,V>(right);
                newRight.level = newThis.level;
                newThis.right = newRight; // Recent attempt at a bug-fix
            }
            return newThis;
        } else {
            return this;
        }
    }
    
    private SharedTreeMap<K,V> withoutMax() {
        if (right.isEmpty()) {
            return left.isEmpty() ? right : left;
        } else {
            return new SharedTreeMap<K,V>(this, left, right.withoutMax()).rebalanced();
        }
    }
    
    protected void appendSelfKey(StringBuilder sb, int numTabs) {
        appendTabs(numTabs, sb);
        
        if (isEmpty()) {
            sb.append("[sentinel]\n");
        } else {
            sb.append("[" + getKey() + "](" + level + ")\n");
            left.appendSelfKey(sb, numTabs + 1);
            right.appendSelfKey(sb, numTabs + 1);
        }
    }
    
    private void appendSelf(StringBuilder sb, int numTabs) {
        appendTabs(numTabs, sb);
        
        if (isEmpty()) {
            sb.append("[sentinel]\n");
        } else {
            sb.append("[" + getKey() + "," + getValue() + "](" + level + ")\n");
            left.appendSelf(sb, numTabs + 1);
            right.appendSelf(sb, numTabs + 1);
        }
    }
    
    private void appendTabs(int numTabs, StringBuilder sb) {
        for (int i = 0; i < numTabs; ++i) {sb.append('\t');}
    }
    
    private void buildInOrder(StringBuilder sb) {
        if (!isEmpty()) {
            left.buildInOrder(sb);
            sb.append("[");
            sb.append(getKey().toString());
            sb.append(",");
            sb.append(getValue().toString());
            sb.append("] ");
            right.buildInOrder(sb);
        }
    }
    
    private class SelfIter implements Iterator<K> {
        private LinkedList<SharedTreeMap<K,V>> next;
        private SharedTreeMap<K,V> where;
        
        public SelfIter(SharedTreeMap<K,V> start) {
            next = new LinkedList<SharedTreeMap<K,V>>();
            where = findNext(start);
        }
        
        private SharedTreeMap<K,V> findNext(SharedTreeMap<K,V> start) {
            if (start.isEmpty() || start.left.isEmpty()) {
                return start;
            } else {
                next.addLast(start);
                return findNext(start.left);
            }
        }
        
        public boolean hasNext() {return !where.isEmpty();}
        
        public K next() {
            K result = where.getKey();
            if (where.right.isEmpty()) {
                where = (next.isEmpty()) ? where.right : next.removeLast();
            } else {
                where = findNext(where.right);
            }
            return result;
        }
        
        public void remove() {throw new UnsupportedOperationException();}
    }
    
    public static void main(String[] args) {
        SharedTreeMap<String,String> root = new SharedTreeMap<String,String>();
        for (int i = 0; i < args.length; i+=2) {
            root = root.with(args[i], args[i+1]);
        }
        
        System.out.println("Tree:");
        System.out.println(root);
        
        System.out.println("Iterator test: In ascending order:");
        for (String node: root) {
            System.out.print("(" + node + " " + root.get(node) + ")");
        }
        System.out.println();
        
        System.out.println("nth test: In ascending order:");
        for (int i = 0; i < root.size(); ++i) {
            System.out.println("root.nth(" + i + "): " + root.nth(i));
        }
        System.out.println();
        
        for (int i = 0; i < args.length; i+=2) {
            root = root.without(args[i]);
            System.out.println("size: " + root.size());
            System.out.println("Tree:");
            System.out.println(root);
        }
    }
}
