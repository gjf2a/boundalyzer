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

public class StackFrame {
    private StackFrame caller;
    private int inputStart, row, rowPosition, charsMatched, depth;
    private String symbol;
    private ArrayList<Tree> children;
    private boolean finished;
    private Grammar grammar;
    private Input input;
    
    private static final boolean debug = false;
    
    public StackFrame(Grammar g, Input input, int inputStart) {
        this(g, null, g.startSymbol(), input, inputStart);
    }
    
    public StackFrame(StackFrame from, String symbol) {
        this(from.grammar, from, symbol, from.input, from.unmatchedStart());
    }
    
    /*[callingFrame(), matcher(), leftSymbol() isFinished(), hasError(), inputStart(), numChildTrees() := 
       from, g.ruleFor(symbol), symbol, false, false, inputStart, 0]*/
    private StackFrame(Grammar g, StackFrame from, String symbol, Input input, int inputStart) {
        grammar = g;
        caller = from;
        this.symbol = symbol;
        this.inputStart = inputStart;
        this.row = -1;
        nextRow();
        finished = false;
        this.input = input;
        
        depth = (from == null) ? 0 : from.depth + 1;
    }
    
    public int depth() {return depth;}
    
    public String currentLine() {return input.toLineEnd(unmatchedStart());}
    
    public int numChildTrees() {return children.size();}
    
    public Tree childTree(int i) {return children.get(i);}
    
    public boolean isFinished() {return finished;}
    
    public StackFrame callingFrame() {return caller;}
    
    public Recognizer matcher() {
        if (!grammar.hasRuleFor(symbol)) {
            throw new IllegalStateException("No rule in grammar for " + symbol);
        }
        return grammar.ruleFor(symbol);
    }
    
    public String leftSymbol() {return symbol;}
    
    public int inputStart() {return inputStart;}
    
    public ArrayList<String> symbolChain() {
        ArrayList<StackFrame> reversedChain = new ArrayList<StackFrame>();
        for (StackFrame sf = this; sf != null; sf = sf.callingFrame()) {
            reversedChain.add(sf);
        }
        
        ArrayList<String> chain = new ArrayList<String>();
        for (int i = reversedChain.size() - 1; i >= 0; i--) {
            chain.add(reversedChain.get(i).leftSymbol());
        }
        return chain;
    }
    
    /*[Parse already succeeded at rightSymbol() and unmatchedStart() => 
        return := cached parse tree
      | true => return := null]*/
    public Tree rightTree(ResultTable results) {
        return results.getTree(rightSymbol(), unmatchedStart());
    }
    
    public String rightSymbol() {
        return rightSymbolInfo().toString();
    }
    
    public SymbolInfo rightSymbolInfo() {
        return matcher().symbolAt(row, rowPosition);
    }
    
    public int unmatchedStart() {return inputStart + charsMatched;}
    
    /*[!matcher().isTerminal() => throws := IllegalStateException
      | matcher() is matched at leftSymbol() and unmatchedStart() =>
        throws := IllegalStateException
      | true => isFinished(), results.getTree(leftSymbol(), unmatchedStart()) := true, error]*/
    public void resolveTerminal(ResultTable results) {
        if (!matcher().isTerminal()) {throw new IllegalStateException("can't call with a nonterminal");}
        if (debug) {System.out.println("resolveTerminal(): leftSymbol(): " + leftSymbol());}
        
        Tree pt = ((NonRecursive)matcher()).makeTree(results, inputStart);
        results.addTree(leftSymbol(), inputStart, pt);
        finished = true;
        if (debug) {printTreeSuccess(results);}
    }           

    // current() = results.getTree(rightSymbol(), unmatchedStart())
    // error(tree) = tree is an error and symbol is not optional and not an 
    //               exclusive lookahead 
    // parsedTreeFor(symbol) = empty leaf if current() is an error; tree stored in table otherwise
    /*[current() == null or isFinished() => throws := IllegalStateException
      | error(current()) and hasMoreRows() => numChildTrees(), rightSymbol(), unmatchedChars(), numChildTrees() := 0, first symbol from next row, inputStart(), 0
      | error(current()) and no rows remain => isFinished(), results.getTree(rightSymbol(), unmatchedStart()) := true, error tree
      | unmatched symbols remain on right-hand side => rightSymbol(), unmatchedStart(), results.getTree(rightSymbol(), unmatchedStart()), getChildTree(numChildTrees()), numChildTrees() := next symbol, unmatchedStart() + (number of chars matched) only if not lookahead, parse tree for rightSymbol(), parsedTreeFor(rightSymbol()), numChildTrees() + 1
      | true => isFinished(), results.getTree(rightSymbol(), unmatchedStart()), results.getTree(leftSymbol(), unmatchedStart()) := true, parsedTreeFor(rightSymbol()), parse tree built from childTrees(i)
    ]*/
    public void nextSymbol(ResultTable results) {
        if (isFinished()) {throw new IllegalStateException("isFinished(): No more advancing possible");}
        Tree parsed = rightTree(results);
        if (parsed == null) {throw new IllegalStateException("No rightTree(): No more advancing possible");}
        if (debug) {System.out.println("nextSymbol(): leftSymbol(): " + leftSymbol());}
        
        if (rightSymbolInfo().isExclusiveLookahead()) {
            parsed = parsed.isError() 
                ? makeEmpty(results) 
                : new Error(rightSymbolInfo().getOriginal(), results.getInput(), unmatchedStart(), 0, "Unwelcome match to " + rightSymbolInfo().getOriginal());
        }
        
        if (parsed.isError() && rightSymbolInfo().isOptional()) {
            parsed = makeEmpty(results);
        }
        
        if (!rightSymbolInfo().isLookahead()) {
            charsMatched += parsed.length();
        }
        
        children.add(parsed);
        
        if (parsed.isError()) {
            if (row + 1 < matcher().numRows()) {nextRow(results);} 
            else {finished = true;}
        } else {
            rowPosition += 1;
            finished = (rowPosition == matcher().numSymbols(row));
        }
        
        if (isFinished()) {
            results.addTree(leftSymbol(), inputStart, makeNode(results));
            if (debug) {printTreeSuccess(results);}
        }
    }
    
    private Tree makeNode(ResultTable results) {
        RecursiveNonterminal rn = (RecursiveNonterminal)matcher();        
        ArrayList<Tree> altChildren = new ArrayList<Tree>();
        int altMatched = 0;
        for (int i = 0; i < children.size(); ++i) {
            Tree child = children.get(i);
            if (results.errorStoredFor(child.name(), child.start())) {
                Tree childError = results.bestErrorFor(child.name(), child.start());
                altChildren.add(childError);
                results.reportError(rn.makeFinalTree(altChildren, results, inputStart, altMatched + childError.length()));
                altChildren.set(i, child);
            } else {
                altChildren.add(child);
            }
            altMatched += child.length();
        }
        return rn.makeFinalTree(children, results, inputStart, charsMatched);
    }
    
    private Leaf makeEmpty(ResultTable results) {
        return new Leaf(rightSymbol(), results.getInput(), unmatchedStart(), 0);
    }
    
    private void printTreeSuccess(ResultTable results) {
        Tree t = results.getTree(leftSymbol(), inputStart);
        System.out.print(leftSymbol() + " at (" + inputStart + ", " + t.end() + ") ends in ");
        System.out.println(t.isError() ? "error" : "success");
    }
    
    private void nextRow(ResultTable results) {
        results.reportError(makeNode(results));
        nextRow();
    }
    
    private void nextRow() {
        row += 1;
        rowPosition = charsMatched = 0;
        children = new ArrayList<Tree>();
    }
}
