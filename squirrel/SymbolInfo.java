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

public class SymbolInfo {
    private String symbol, original;
    private boolean optional;
    private boolean terminal;
    private boolean inclLook;
    private boolean exclLook;
    
    private SymbolInfo() {
        optional = terminal = inclLook = exclLook = false;
    }
    
    public SymbolInfo(String symbolName) {
        this(symbolName, false);
    }
    
    /*[getOriginal(), isOptional(), isTerminal(), isInclusiveLookahead(), isExclusiveLookahead() := 
       symbolName, squareBracketed(symbolName), not angleBracketed(symbolName), prefixedBy(symbolName, &), prefixedBy(symbolName, !)]*/
    public SymbolInfo(String symbolName, boolean ignoreSpecial) {
        this();
        symbol = original = symbolName;
        if (ignoreSpecial) {return;}
        
        if (symbol.charAt(0) == '[' && symbol.charAt(symbol.length() - 1) == ']') {
            optional = true;
            symbol = symbol.substring(1, symbol.length() - 1);
        }
        
        if (symbol.charAt(0) == '&') {
            inclLook = true;
        } else if (symbol.charAt(0) == '!') {
            exclLook = true;
        }
        
        if (inclLook || exclLook) {
            symbol = symbol.substring(1, symbol.length());
        }
        
        if (symbol.length() == 0) {
            throw new IllegalArgumentException("Zero-length symbol \"" + symbolName + "\"");
        }
        
        if (symbol.charAt(0) != '<' || symbol.charAt(symbol.length() - 1) != '>') {
            terminal = true;
        }
    }
    
    public String toString() {return symbol;}
    public String getOriginal() {return original;}
    public boolean isTerminal() {return terminal;}
    public boolean isOptional() {return optional;}
    public boolean isInclusiveLookahead() {return inclLook;}
    public boolean isExclusiveLookahead() {return exclLook;}
    public boolean isLookahead() {return inclLook || exclLook;}
}
