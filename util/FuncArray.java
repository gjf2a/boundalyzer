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

import java.util.Iterator;

public class FuncArray<T> implements Iterable<T> {
    private T[] array;
    private Cell cellList;
    private int size, numCells;
    
    public FuncArray() {
        this(0);
    }
    
    private FuncArray(int size) {
        array = (T[])new Object[size];
        cellList = null;
        this.size = size;
        numCells = 0;
    }
    
    // Pre: 0 <= where <= size
    private FuncArray(FuncArray<T> prev, int where, T what) {
        array = prev.array;
        cellList = new Cell(prev.cellList, where, what);
        size = prev.size;
        if (where == size) {
            size += 1;
        }
        numCells = prev.numCells + 1;
    }
    
    private FuncArray<T> makeWith(int where, T what) {
        FuncArray<T> result = new FuncArray<T>(this, where, what);
        return (result.numCells > 2*result.size) ? result.refreshed() : result;
    }
    
    private void updateIfNull(int i, T t) {
        if (array[i] == null) {array[i] = t;}
    }
    
    public FuncArray<T> refreshed() {
        if (numCells == 0) {return this;}
            
        FuncArray<T> result = new FuncArray<T>(size);
        for (Cell c = cellList; c != null; c = c.next) {
            result.updateIfNull(c.i, c.t);
        }
        
        for (int i = 0; i < array.length; ++i) {
            result.updateIfNull(i, array[i]);
        }
        return result;
    }
    
    public Iterator<T> iterator() {return new FIter();}
    
    public int size() {
        return size;
    }
    
    public boolean equals(Object other) {
        if (other instanceof FuncArray) {
            FuncArray<T> that = (FuncArray<T>)other;
            if (that.size() != this.size()) {return false;}
            
            for (int i = 0; i < size(); ++i) {
                if (!this.get(i).equals(that.get(i))) {
                    return false;
                }
            }
            return true;
            
        } else {
            return false;
        }
    }
    
    // Pre: 0 <= i < size
    // Post: None
    private void check(int i) {
        if (i < 0 || i > size) {
            throw new ArrayIndexOutOfBoundsException(i + " is out of bounds");
        }
    }
    
    public T get(int i) {
        check(i);
        for (Cell c = cellList; c != null; c = c.next) {
            if (c.i == i) {return c.t;}
        }
        return array[i];
    }
    
    public FuncArray<T> add(T t) {
        return makeWith(size, t);
    }
    
    public FuncArray<T> set(int i, T t) {
        check(i);
        return makeWith(i, t);
    }
    
    public FuncArray<T> swap(int i, int j) {
        FuncArray<T> iCopied = set(j, get(i));
        return iCopied.set(i, get(j));
    }
    
    public FuncArray<T> insert(int i, T t) {
        if (i == size) {
            return add(t);
        } else {
            check(i);
            FuncArray<T> result = new FuncArray<T>(size + 1);
            result.array[i] = t;
            
            for (Cell c = cellList; c != null; c = c.next) {
                int dest = c.i < i ? c.i : c.i + 1;
                result.updateIfNull(dest, c.t);
            }
            
            for (int ind = 0; ind < i && ind < array.length; ++ind) {
                result.updateIfNull(ind, array[ind]);
            }
            
            for (int ind = i; ind < array.length; ++ind) {
                result.updateIfNull(ind + 1, array[ind]);
            }
            return result;
        }
    }
    
    public FuncArray<T> remove(int i) {
        check(i);
        FuncArray<T> result = new FuncArray<T>(size - 1);
        for (Cell c = cellList; c != null; c = c.next) {
            if (c.i < i) {
                result.updateIfNull(c.i, c.t);
            } else if (c.i > i) {
                result.updateIfNull(c.i - 1, c.t);
            }
        }
        
        for (int ind = 0; ind < i && ind < array.length; ++ind) {
            result.updateIfNull(ind, array[ind]);
        }
        
        for (int ind = i + 1; ind < array.length; ++ind) {
            result.updateIfNull(ind - 1, array[ind]);
        }
        
        return result;
    }
    
    private class Cell {
        public T t;
        public int i;
        public Cell next;
        
        public Cell(Cell prev, int where, T what) {
            next = prev;
            i = where;
            t = what;
        }
    }
    
    private class FIter implements Iterator<T> {
        private int i;
        
        public FIter() {this(0);}
        
        public FIter(int where) {i = where;}
     
        public boolean hasNext() {
            return i < size();
        }
        
        public T next() {
            return get(i++);
        }
        
        public void remove() {throw new UnsupportedOperationException();}
    }
    
    public static void main(String[] args) {
        FuncArray<String> test = new FuncArray<String>();
        for (String s: args) {test = test.add(s);}
        
        System.out.println("Command-line arguments:");
        for (String s: test) {System.out.print(s + " ");}
        System.out.println();
        
        if (args.length > 0) {
            System.out.println("First element removed:");
            FuncArray<String> removed = test.remove(0);
            for (String s: removed) {System.out.print(s + " ");}
            System.out.println();
            
            System.out.println("First element put back, second:");
            removed = removed.insert(1, args[0]);
            for (String s: removed) {System.out.print(s + " ");}
            System.out.println();
        }
    }
}
