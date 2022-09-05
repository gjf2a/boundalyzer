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

package grammars;

import squirrel.*;

public class StringLiteralGrammar implements HasGrammar {
    private Grammar g;
    
    public StringLiteralGrammar() {
        g = new Grammar();
        g.addToken("<string>", "<quote> [<stringChars>] <quote>");
        g.addToken("<stringChars>", "<notQuote> [<stringChars>]");
        g.addCharClass("<quote>", '"');
        g.addToken("<notQuote>", "<backslashed>", "!<quote> <any>");
        
        g.addCharClass("<backslash>", '\\');
        g.addToken("<backslashed>", "<backslash> <any>");
    }
    
    public Grammar getGrammar() {return g;}
}
