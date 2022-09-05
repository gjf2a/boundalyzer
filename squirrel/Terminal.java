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

public class Terminal extends NonRecursive {
    private String errorMsg;
    
    public Terminal(String terminal) {
        super(terminal);
        errorMsg = "Token does not match \"" + replaceSpecialChars(name()) + "\"";
    }
    
    public Tree makeTree(ResultTable results, int inputStart) {
        Input in = results.getInput();
        for (int i = 0; i < name().length(); ++i) {
            int at = i + inputStart;
            if (at >= in.length()) {
                return makeExhausted(in, inputStart, i);
            } else if (in.charAt(at) != name().charAt(i)) {
                return new Error(name(), in, inputStart, i, errorMsg);
            }
        }
        
        return new Leaf(name(), in, inputStart, name().length());  
    }
    
    public int numRows() {return 1;}
    
    public int numSymbols(int row) {
        return 1;
    }
    
    public SymbolInfo symbolAt(int row, int rowPosition) {
        return new SymbolInfo(name(), true);
    }
    
    public String toString() {return name();}
    
    static private String replaceSpecialChars(String s) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            switch (c) {
                case '\n': result.append("\\n"); break;
                case '\t': result.append("\\t"); break;
                case '\0': result.append("\\0"); break;
                default: result.append(c);
            }
        }
        return result.toString();
    }
}

