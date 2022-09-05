// Copyright 2007 by Gabriel J. Ferrer
//
// This program is part of the Squirrel project.
// 
// Squirrel is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as 
// published by the Free Software Foundation, either version 3 of 
// the License, or (at your option) any later version.
//
// Squirrel is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with Squirrel.  If not, see <http://www.gnu.org/licenses/>.

package squirrel;

public class RangeCharMatcher extends CharNonterminal {
    private char low, high;
    
    /*[lo <= hi => getNthChar(0), getNthChar(hi - lo) := lo, hi
      | true => throws := IllegalArgumentException]*/
    public RangeCharMatcher(String name, char lo, char hi) {
        super(name);
        if (lo > hi) {
            throw new IllegalArgumentException(lo + " is higher than " + hi);
        }
        low = lo;
        high = hi;
    }
    
    public int numDistinctChars() {
        return high - low + 1;
    }
    
    public int rowMatching(char c) {
        int result = c - low;
        if (result < numDistinctChars() && result >= 0) {
            return result;
        } else {
            return -1;
        }
    }
    
    public char getNthChar(int n) {
        return (char)(low + n);
    }
    
    public String toString() {
        return name() + " ::= {" + low + " to " + high + "}";
    }
}
