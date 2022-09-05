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

abstract public class Recognizer {
    private String name;
    
    /*[name() := name]*/
    public Recognizer(String name) {
        this.name = name;
    }
    
    public String name() {return name;}
    
    abstract public boolean isTerminal();
    
    abstract public int numRows();
    
    abstract public int numSymbols(int row);
    
    abstract public SymbolInfo symbolAt(int row, int rowPosition);
    
    /*[this is not internally consistent => throws := exception 
      | true => I]*/
    abstract public void verify();
}
