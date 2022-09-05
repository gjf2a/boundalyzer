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

public class Grammar implements HasGrammar {
    private String startSymbol;
    private Map<String,Recognizer> grammar;
    
    /*[startSymbol() := null]*/
    public Grammar() {
        grammar = new LinkedHashMap<String,Recognizer>();
        grammar.put("<any>", new AnyChar());
        startSymbol = null;
    }
    
    public Grammar getGrammar() {return this;}
    
    /*[this contains no illicit left recursion or unreferenced nonterminals
       => I
      | true => throws := IllegalStateException]*/
    public void checkGrammar() {
        grammar.get(startSymbol).verify();
    }
    
    public String startSymbol() {return startSymbol;}
    
    /*[symbols().contains(nonterm) => startSymbol() := nonterm
      | true => throws := IllegalArgumentException]*/
    public void setStartSymbol(String nonterm) {
        if (!grammar.containsKey(nonterm)) {
            throw new IllegalArgumentException("Symbol " + nonterm + " is not present");
        }
        startSymbol = nonterm;
    }
    
    public Set<String> symbols() {return grammar.keySet();}
    
    public boolean hasRuleFor(String symbol) {
        return grammar.containsKey(symbol);
    }
    
    public Recognizer ruleFor(String symbol) {
        return grammar.get(symbol);
    }
    
    public void importStandard() {
        importFrom(new Standard());
    }
    
    /*[symbols(), ruleFor(other.symbols().keySet()) := 
      symbols() ++ all from other.symbols(), rule for each of other.symbols()]*/
    public void importFrom(Grammar other) {
        other.checkGrammar();
        for (String s: other.symbols()) {
            grammar.put(s, other.grammar.get(s));
        }
    }
    
    /*[symbols(), ruleFor(terminal) := symbols() ++ terminal, leaf maker for terminal]*/
    public void addTerminal(String terminal) {
        grammar.put(terminal, new Terminal(terminal));
    }
    
    /*[leftHand and all nonterminals in each rightHand are enclosed in angle 
       brackets => symbols(), ruleFor(leftHand) := symbols() ++ leftHand, 
                   tree builder from rightHand
      | true => throws := IllegalStateException]*/
    public void addRule(String leftHand, String... rightHand) {
        addToGrammar(leftHand, new TreeBuilder(leftHand, strToArray(rightHand), this));
    }
    
    /*[angleBracketed(leftHand) and angleBracketed(each rightHand nonterminal)
       => symbols(), ruleFor(leftHand) := symbols() ++ leftHand, 
          token builder from rightHand
      | true => throws := IllegalStateException]*/
    public void addToken(String leftHand, String... rightHand) {
        addToGrammar(leftHand, new TokenMatcher(leftHand, strToArray(rightHand), this));
    }
    
    /*[angleBracketed(leftHand) => symbols(), ruleFor(leftHand) := 
                                   symbols() ++ leftHand, 
                                   token builder for each char in rightHand
      | true => throws := IllegalStateException]*/
    public void addCharClass(String leftHand, char... rightHand) {
        addCharMatcher(new LiteralCharMatcher(leftHand, rightHand));
    }
    
    /*[angleBracketed(leftHand) => symbols(), ruleFor(leftHand) := 
                                   symbols() ++ leftHand, 
                                   token builder for each char from start to end
      | true => throws := IllegalStateException]*/
    public void addCharRange(String leftHand, char start, char end) {
        addCharMatcher(new RangeCharMatcher(leftHand, start, end));
    }
    
    private void addCharMatcher(Recognizer cm) {
        addToGrammar(cm.name(), cm);
    }
    
    /*[symbols().contains(leftHand) => throws := IllegalStateException
      | true => startSymbol(), symbols(), ruleFor(leftHand) := leftHand if it was null, symbols() ++ leftHand, r]*/
    private void addToGrammar(String leftHand, Recognizer r) {
        if (grammar.containsKey(leftHand)) {
            throw new IllegalArgumentException(leftHand + " has been added multiple times to the grammar");
        }
        if (startSymbol == null) {
            startSymbol = leftHand;
        }
        
        grammar.put(leftHand, r);
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String s: grammar.keySet()) {
            sb.append(grammar.get(s));
            sb.append('\n');
        }
        return sb.toString();
    }
    
    private String[][] strToArray(String[] rightHand) {
        String[][] rightHandArrays = new String[rightHand.length][];
        for (int i = 0; i < rightHand.length; ++i) {
            Scanner s = new Scanner(rightHand[i]);
            ArrayList<String> items = new ArrayList<String>();
            while (s.hasNext()) {items.add(s.next());}
            rightHandArrays[i] = new String[items.size()];
            for (int j = 0; j < rightHandArrays[i].length; ++j) {
                rightHandArrays[i][j] = items.get(j);
            }
        }
        return rightHandArrays;
    }
}
