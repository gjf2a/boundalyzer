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

package blackbox2;
import java.util.*;

public class UndoStack<T> {
    private LinkedList<T> data;
    private int limit;
    
    public UndoStack() {
        clear();
        noLimit();
    }
    
    public boolean hasLimit() {
        return limit >= 0;
    }
    
    public void setLimit(int n) {limit = n;}
    
    public void noLimit() {limit = -1;}
    
    public int limit() {return limit;}
    
    public void push(T t) {
        data.add(t);
        if (hasLimit() && data.size() > limit) {
            data.removeFirst();
        }
    }
    
    public T top() {return data.get(data.size() - 1);}
    
    public T pop() {
        T result = top();
        data.removeLast();
        return result;
    }
    
    public boolean empty() {return data.size() == 0;}
    
    public void clear() {
        data = new LinkedList<T>();
    }
    
    public static void main(String[] args) {
        UndoStack<String> test = new UndoStack<String>();
        
        for (String s: args) {test.push(s);}
        while (!test.empty()) {System.out.println(test.pop());}
    }
}
