// Copyright 2009 by Gabriel J. Ferrer
//
// This program is part of the Boundalyzer project.
// 
// Boundalyzer is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// Boundalyzer is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with Boundalyzer.  If not, see <http://www.gnu.org/licenses/>.

package sat;
import java.util.*;

public class Searcher {
    private VarInfo vars;
    private LinkedList<Node> deque;
    private boolean done, found;
    private Expr expr;
    
    private final static boolean debug = false;
    
    public Searcher(Expr ex) {
        expr = ex;
        done = found = false;
        vars = ex.allVars();
        if (debug) {System.out.println(vars);System.out.print("candidates:");}
        ArrayList<String> candidates = new ArrayList<String>();
        for (String v: vars.allNames()) {
            if (debug) {System.out.print(" \"" + v + "\"");}
            candidates.add(v);
        }
        if (debug) {System.out.println();}
        deque = new LinkedList<Node>();
        deque.addFirst(new Node(candidates));
    }
    
    public void expandNext() {
        if (debug) {System.out.println("Entering expandNext()");}
        if (deque.size() == 0) {
            done = true;
        } else {
            Node next = deque.removeLast();
            if (debug) {next.printDebug();}
            if (next.allAssigned()) {
                found = found || expr.isTrue(next.allTrueVars());
            } else {
                if (debug) {System.out.println("next.nextVarToAssign(): " + next.nextVarToAssign());}
                if (vars.is(next.nextVarToAssign())) {
                    if (debug) {System.out.println("is");}
                    deque.addLast(next.successor(true));
                }
                if (vars.isNot(next.nextVarToAssign())) {
                    if (debug) {System.out.println("is not");}
                    deque.addLast(next.successor(false));
                }
            }
        }
    }
    
    // Post: Returns true if every possible combination of values has been
    //       considered in the search
    public boolean complete() {return done;}
    
    // Post: Returns true if a satisfying assignment has been found
    public boolean satisfied() {if (debug) {System.out.println("satisfied? " + found);} return found;}
    
    public static void main(String[] args) {
        
        // Start with a VERY simple test case
        Searcher one = new Searcher(new Var("x"));
        while (!one.complete()) {one.expandNext();}
        System.out.println(one.satisfied() ? "satisfied" : "unsatisfied");
    }
}
