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

public class ResultTable {
    private Map<String,Map<Integer,Tree>> table, bestErrors;
    private Input input;
    private String startSymbol;
    
    private final static boolean debug = false;
    
    /*[getInput(), treeStoredFor(nonterm, inputStart) := in, false]*/
    public ResultTable(Input in, String startSymbol) {
        input = in;
        table = new LinkedHashMap<String,Map<Integer,Tree>>();
        bestErrors = new LinkedHashMap<String,Map<Integer,Tree>>();
        this.startSymbol = startSymbol;
    }
    
    /*[treeStoredFor(nonterm, inputStart) => return := treeFor(nonterm, inputStart)
      | true => return := null]*/
    public Tree getTree(String nonterm, int inputStart) {
        ensureNonTermRow(nonterm);
        if (inputStart > maxInputStart()) {
            return null;
        }
        return table.get(nonterm).get(inputStart);
    }
    
    public void reportError(Tree error) {
        if (!error.isError()) {
            throw new IllegalArgumentException(error + " is not an error");
        }
        
        if (!bestErrors.containsKey(error.name())) {
            bestErrors.put(error.name(), new LinkedHashMap<Integer,Tree>());
        }
        
        Map<Integer,Tree> bestMap = bestErrors.get(error.name());
        if (!bestMap.containsKey(error.start()) || bestMap.get(error.start()).end() < error.end()) {
            bestMap.put(error.start(), error);
            if (debug) {
                System.out.println(error.name() + ": Reporting best error: " + error.errorMessage());
            }
        }
    }
    
    public boolean treeStoredFor(String nonterm, int inputStart) {
        return table.containsKey(nonterm) && table.get(nonterm).containsKey(inputStart);
    }
    
    public boolean errorStoredFor(String nonterm, int inputStart) {
        return bestErrors.containsKey(nonterm) && bestErrors.get(nonterm).containsKey(inputStart);
    }
    
    public Tree bestErrorFor(String nonterm, int inputStart) {
        return bestErrors.get(nonterm).get(inputStart);
    }
    
    public int maxInputStart() {return input.length();}
    
    private void ensureNonTermRow(String nonterm) {
        if (table.get(nonterm) == null) {
            table.put(nonterm, new LinkedHashMap<Integer,Tree>());
            //table.put(nonterm, new Tree[maxInputStart() + 1]);
        }
    }
    
    /*[treeStoredFor(symbol, inputStart) => throws := exception
    | true => bestError(), treeStoredFor(symbol, inputStart), getTree(symbol, inputStart) := tree if tree.isError() and tree.matchesMoreThan(getBestError()), true, tree]*/ 
    public void addTree(String symbol, int inputStart, Tree tree) {
        ensureNonTermRow(symbol);
        
        if (treeStoredFor(symbol, inputStart)) {
            System.out.println("Failing to add \"" + tree + "\"");
            throw new IllegalStateException("Precondition violated for ResultTable.addTree(): symbol is " + symbol + "; tree.start() = " + tree.start());
        }
        
        if (tree.isError()) {reportError(tree);}
        table.get(symbol).put(inputStart, tree);
    }
    
    /*[parse error => return := parse error at maximum distance from start
      | true => return := tree matching entire input]*/
    public Tree bestTree() {
        if (getInput().length() < 1) {
            return new Error(startSymbol, input, 0, 0, "Empty input");
        } else {
            Tree result = getTree(startSymbol, 0);
            if (result.length() < getInput().length() && !result.isError()) {
                if (debug) {
                    System.out.println("result: " + result.textTree());
                    System.out.println("result.length(): " + result.length() + " getInput().length(): " + getInput().length());
                    System.out.println("last char of result: '" + result.toString().charAt(result.toString().length() - 1) + "'");
                    System.out.println("last char of getInput(): '" + getInput().charAt(getInput().length() - 1) + "'");
                }
                return bestErrorFor(startSymbol, 0);
            } else {
                return result;
            }
        }
    }
    
    public Input getInput() {return input;}
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String nonterm: table.keySet()) {
            sb.append("Nonterminal ");
            sb.append(nonterm);
            sb.append(":");
            for (int i: table.get(nonterm).keySet()) {
                sb.append(i);
                sb.append(": ");
                sb.append(table.get(nonterm).get(i).toString());
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
