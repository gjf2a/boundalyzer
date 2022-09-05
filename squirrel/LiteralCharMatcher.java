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

public class LiteralCharMatcher extends CharNonterminal {
    private char[] chars;
    
    public LiteralCharMatcher(String name, char[] charClass) {
        super(name);
        chars = charClass;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name());
        sb.append(" ::= ");
        sb.append(chars[0]);
        for (int i = 1; i < chars.length; ++i) {
            sb.append(" | ");
            sb.append(chars[i]);
        }
        return sb.toString();
    }

    
    public char getNthChar(int n) {return chars[n];}
    
    public int numDistinctChars() {return chars.length;}
}
