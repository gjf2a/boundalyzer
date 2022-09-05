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

public class AnyChar extends NonRecursive {
    public AnyChar() {
        super("<any>");
    }
    
    public Tree makeTree(ResultTable results, int inputStart) {
        if (results.getInput().legal(inputStart)) {
            return new Leaf(name(), results.getInput(), inputStart, 1);
        } else {
            return makeExhausted(results.getInput(), inputStart, 0);
        }
    }
    
    public int numRows() {return 1;}
    
    public int numSymbols(int row) {
        return 1;
    }
    
    public SymbolInfo symbolAt(int row, int rowPosition) {
        return new SymbolInfo(name(), true);
    }
}
