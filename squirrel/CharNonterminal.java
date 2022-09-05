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

abstract public class CharNonterminal extends NonRecursive {
    public CharNonterminal(String name) {
        super(name);
    }
    
    public Tree makeTree(ResultTable results, int inputStart) {
        if (results.getInput().legal(inputStart)) {
            char c = results.getInput().charAt(inputStart);
            int row = rowMatching(c);
            if (row < 0) {
                return new Error(name(), results.getInput(), inputStart, 0, getErrorMsg(c));
            } else {
                return new Leaf(name(), results.getInput(), inputStart, 1);
            } 
        } else {
            return makeExhausted(results.getInput(), inputStart, 0);
        }
    }
    
    public String getErrorMsg(char mismatch) {
        return "Character '" + mismatch + "' does not match " + matchableCharList();
    }
    
    /*[return := list of matchable characters]*/
    public String matchableCharList() {
        if (numDistinctChars() == 1) {
            return Character.toString(getNthChar(0));
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < numDistinctChars() - 1; ++i) {
                sb.append("'" + getNthChar(i) + "', ");
            }
            sb.append("or '"+ getNthChar(numDistinctChars() - 1) + "'");
            return sb.toString();
        }
    }
    
    public int numRows() {return numDistinctChars();}
    
    public int numSymbols(int row) {
        return 1;
    }
    
    public SymbolInfo symbolAt(int row, int rowPosition) {
        return new SymbolInfo(Character.toString(getNthChar(row)), true);
    }
    
    abstract public int numDistinctChars();
    
    /*[0 <= n < numDistinctChars() => return := the nth char according to the ordering of this]*/
    abstract public char getNthChar(int n);
    
    public boolean recognizesChar(char c) {return rowMatching(c) >= 0;}
    
    /*[getNthChar(n) == c => return := n | true => return := -1]*/
    public int rowMatching(char c) {
        for (int i = 0; i < numDistinctChars(); ++i) {
            if (getNthChar(i) == c) {
                return i;
            }
        }
        return -1;
    }
}
