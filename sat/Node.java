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

public class Node {
    private int toBeChanged;
    private Set<String> current;
    private ArrayList<String> candidates;
    
    public Node(ArrayList<String> candidates) {
        this(0, new TreeSet<String>(), candidates);
    }
    
    private Node(Node parent) {
        this(parent.toBeChanged + 1, parent.current, parent.candidates);
    }
    
    private Node(Node parent, Set<String> changed) {
        this(parent.toBeChanged + 1, changed, parent.candidates);
    }
    
    private Node(int toB, Set<String> current, ArrayList<String> candidates) {
        toBeChanged = toB;
        this.current = current;
        this.candidates = candidates;
    }
    
    // Pre: None
    // Post: Returns true if every variable has been assigned a truth value
    public boolean allAssigned() {
        return toBeChanged >= candidates.size();
    }
    
    // Pre: !allAssigned()
    // Post: Returns the var that will be assigned by trueSuccessor() or
    //       falseSuccessor()
    public String nextVarToAssign() {
        return candidates.get(toBeChanged);
    }
    
    // Pre: !allAssigned()
    public Node successor(boolean nextValue) {
        if (nextValue) {
            Set<String> next = new TreeSet<String>(current);
            next.add(candidates.get(toBeChanged));
            return new Node(this, next);
        } else {
            return new Node(this);
        }
        
    }
    
    // Pre: allAssigned()
    // Post: Returns all variables that are assigned "true"; there is a 
    //       closed-world hypothesis implicit here
    public Set<String> allTrueVars() {
        if (allAssigned()) {return current;}
        else {throw new IllegalStateException("Not all vars assigned!");}
    }
    
    public void printDebug() {
        System.out.print("Candidates already considered:");
        for (int i = 0; i < toBeChanged; ++i) {
            System.out.print(" " + candidates.get(i));
        }
        System.out.println();
        
        System.out.print("Candidates yet to be considered:");
        for (int i = toBeChanged; i < candidates.size(); ++i) {
            System.out.print(" " + candidates.get(i));
        }
        System.out.println();
    }
}
