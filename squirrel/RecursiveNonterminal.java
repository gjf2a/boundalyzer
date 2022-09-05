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
import java.util.*;

abstract public class RecursiveNonterminal extends Recognizer {
    private static boolean debug = false;
    
    private SymbolInfo[][] symbols;
    private Grammar grammar;
    private boolean verified, leftAssoc;
    
    /*[name(), isTerminal(), memberOf(), numRows(), numSymbols(row), symbolAt(row, symbol) := 
       name, false, memberOf, symbols.length, symbols[row].length, new SymbolInfo(symbols[row][symbol])]*/
    public RecursiveNonterminal(String name, String[][] symbols, Grammar memberOf) {
        super(name);
        leftAssoc = usableLeftRecursion(symbols);
        if (debug) {
            if (leftAssoc) {System.out.println(name() + " has usable left recursion");}
            else {System.out.println(name() + " does not have usable left recursion");}
        }
        String[][] syms = leftAssoc ? makeRightRecursive(symbols) : symbols;
        this.symbols = convertMatrix(syms);
        grammar = memberOf;
        verified = false;
    }
    
    public Grammar memberOf() {return grammar;}
        
    public int numRows() {
        return symbols.length;
    }
    
    public int numSymbols(int row) {
        return symbols[row].length;
    }
    
    public SymbolInfo symbolAt(int row, int position) {
        return symbols[row][position];
    }
    
    abstract public Tree makeFinalTree(ArrayList<Tree> symbolTrees, ResultTable results, int inputStart, int charsMatched);
    
    public boolean isTerminal() {return false;}
    
    /*[symbols has two rows; symbols[1] has one element; symbols[0] ends with that element and starts with name() => return := true
      | return := false]*/
    private boolean usableLeftRecursion(String[][] symbols) {
        if (symbols.length == 2 && symbols[1].length == 1) {
            if (debug) {System.out.println("Got this far...");}
            String solo = symbols[1][0];
            int rowSize = symbols[0].length;
            if (symbols[0][rowSize - 1].equals(solo) && 
                symbols[0][0].equals(name())) {
                if (debug) {System.out.println("About to validate the row");}
                for (int i = 1; i < rowSize; ++i) {
                    if (symbols[0][i].equals(name())) {
                        if (debug) {System.out.println("Failure at position " + i);}
                        return false;
                    }
                }
                return true;
            }
        } 
        
        return false;
    }
    
    /*[usableLeftRecursion => return := symbols with 1st and last elements of row 1 swapped]*/
    private String[][] makeRightRecursive(String[][] symbols) {
        String[][] result = new String[2][];
        result[0] = makeRightRow(symbols[0]);
        result[1] = symbols[1];
        return result;
    }
    
    /*[return := transform of symbolRow from 
             <nonterm> ::= <nonterm> s1 ... sn sym
             into
             <nonterm> ::= sym s1 ... sn <nonterm>]*/
    private String[] makeRightRow(String[] symbolRow) {
        String[] result = new String[symbolRow.length];
        result[0] = symbolRow[symbolRow.length - 1];
        result[result.length - 1] = symbolRow[0];
        
        for (int i = 1; i < symbolRow.length - 1; ++i) {
            result[i] = symbolRow[i];
        }
        return result;
    }
    
    /*[this is not internally consistent => throws := exception 
      | this has terminals not present in the grammar => memberOf() := memberOf() ++ absent terminals
      | true => I]*/
    public void verify() {
        if (verified) {return;}
        verified = true;
        
        for (int row = 0; row < numRows(); ++row) {
            for (int sym = 0; sym < symbols[row].length; ++sym) {
                SymbolInfo symbol = symbols[row][sym];
                String symbolStr = symbol.toString();
                
                if (!grammar.hasRuleFor(symbolStr)) {
                    if (symbol.isTerminal()) {
                        grammar.addTerminal(symbolStr);
                    } else {
                        throw new IllegalStateException("Symbol " + symbol + " is referenced but not defined");
                    }
                }
                grammar.ruleFor(symbolStr).verify();
            }
        }
        
        if (leftRecursive()) {
            throw new IllegalStateException("Disallowed left recursion found in rule " + name());
        }
    }
    
    public boolean leftRecursive() {
        Set<RecursiveNonterminal> leftCorners = new LinkedHashSet<RecursiveNonterminal>();
        addLeftCornersTo(leftCorners);
        return leftCorners.contains(this);
    }

    private void addLeftCornersTo(Set<RecursiveNonterminal> leftCorners) {
        for (int row = 0; row < numRows(); ++row) {
            Recognizer r = grammar.ruleFor(symbols[row][0].toString());
            if (r instanceof RecursiveNonterminal) {
                RecursiveNonterminal rec = (RecursiveNonterminal)r;
                if (!leftCorners.contains(rec)) {
                    leftCorners.add(rec);
                    rec.addLeftCornersTo(leftCorners);
                }
            }
        }
    }
    
    protected boolean isLeftAssociative() {return leftAssoc;}
    
    private static final String ruleSym = " ::= ";
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name());
        sb.append(ruleSym);
        appendRow(sb, 0);
        for (int row = 1; row < numRows(); ++row) {
            for (int i = 0; i < name().length() + ruleSym.length(); ++i) {
                sb.append(' ');
            }
            appendRow(sb, row);
        }
        return sb.toString();
    }
    
    private void appendRow(StringBuilder sb, int row) {
        for (int symbol = 0; symbol < symbols[row].length; ++symbol) {
            sb.append(symbols[row][symbol]);
            sb.append(' ');
        }
        sb.append('\n');
    }
    
    private SymbolInfo[][] convertMatrix(String[][] symbols) {
        SymbolInfo[][] result = new SymbolInfo[symbols.length][];
        for (int i = 0; i < symbols.length; ++i) {
            result[i] = new SymbolInfo[symbols[i].length];
            for (int j = 0; j < symbols[i].length; ++j) {
                result[i][j] = new SymbolInfo(symbols[i][j]);
            }
        }
        return result;
    }
}
