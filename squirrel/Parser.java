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
import java.io.*;

public class Parser {
    private Grammar grammar;
    private StackFrame top;
    private ResultTable results;
    
    private static boolean debug = false;
    
    private Parser(Grammar g) {
        g.checkGrammar();
        this.grammar = g;
    }
    
    private void startWith(String input) {
        Input in = new Input(input);
        results = new ResultTable(in, grammar.startSymbol());
        if (input.length() < 1) {
            top = null;
        } else {
            top = new StackFrame(grammar, in, 0);
        }
    }
    
    public Parser(Grammar g, String input) {
        this(g);
        startWith(input);
    }
    
    public Parser(Grammar g, File input) throws IOException {
        this(g);
        startWith(fileToString(input));
    }
    
    public Parser(Grammar g, Object input) throws IOException {
        this(g);
        if (input instanceof String) {
            startWith((String)input);
        } else if (input instanceof File) {
            startWith(fileToString((File)input));
        } else {
            throw new IllegalArgumentException(input + " is neither a String nor a File");
        }
    }
    
    public void debugMode() {debug = true;}
    
    /*[return := (this.top() == null)]*/
    public boolean isComplete() {
        return top == null;
    }
    
    public ResultTable results() {return results;}
    
    public StackFrame top() {return top;}
    
    public Tree bestTree() {
        parse();
        return results().bestTree();
    }
    
    public void parse() {
        while (!isComplete()) {
            parseNext();
        }
    }
    
    /*[isComplete() => throws := IllegalStateException 
      | top().isFinished() => top() := previous stack frame
      | top() is a terminal symbol => top() := top().resolveTerminal() 
      | no parse tree exists at top().unmatchedStart() and symbol => 
          top() := stack frame for top().currentChildSymbol()
      | true => top() := advance to next symbol]*/
    public void parseNext() {
        if (debug && !top.isFinished()) {
            System.out.println("left: " + top.leftSymbol() + " right: " + top.rightSymbol() + " depth: " + top.depth() + " unmatchedStart: " + top.unmatchedStart());
            System.out.println(top.currentLine());
        }
        if (isComplete()) {
            throw new IllegalStateException("Nothing left to parse");
        } else if (top.isFinished()) {
            top = top.callingFrame();
        } else if (top.matcher().isTerminal()) {
            top.resolveTerminal(results);
        } else if (top.rightTree(results) == null) {
            top = new StackFrame(top, top.rightSymbol());
        } else {
            top.nextSymbol(results);
        }
    }
    
    public static String fileToString(File input) throws IOException {
        Scanner in = new Scanner(input);
        StringBuilder str = new StringBuilder();
        while (in.hasNextLine()) {
            str.append(in.nextLine());
            str.append('\n');
        }
        return str.toString();
    }
}
