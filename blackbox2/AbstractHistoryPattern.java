package blackbox2;

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

import java.util.ArrayList;

import util.*;

abstract public class AbstractHistoryPattern {
    private AbstractHistoryPattern[] children;

    public AbstractHistoryPattern(AbstractHistoryPattern[] children) {
        this.children = children;
    }
    
    public AbstractHistoryPattern() {this(new AbstractHistoryPattern[0]);}
    
    public boolean equals(Object other) {
        if (other instanceof AbstractHistoryPattern) {
            return toString().equals(other.toString());
        } else {
            return false;
        }
    }
    
    public int hashCode() {return toString().hashCode();}
    
    abstract public String toString();
    
    public boolean isError() {
        for (AbstractHistoryPattern h: children) {
            if (h.isError()) {return true;}
        }
        return false;
    }
    
    // Pre: isError()
    // Post: Returns error message
    public String getErrorMsg() {
        for (AbstractHistoryPattern h: children) {
            if (h.isError()) {return h.getErrorMsg();}
        }
        
        if (isError()) {
            return toString();
        } else {
            throw new IllegalStateException("No error!");
        }
    }
    
    // Pre: None
    // Post: Returns all symbols that do not match stimuli, responses, or macros
    public SharedSet<String> invalidSymbols(BlackBox bb) {
        SharedSet<String> result = new SharedTreeSet<String>();
        for (AbstractHistoryPattern c: children) {
            result = result.with(c.invalidSymbols(bb));
        }
        return result;
    }
    
    // Pre: None
    // Post: Returns all stimuli referenced by this StimulusHistory
    public SharedSet<String> stimuliInUse(BlackBox bb) {
        SharedSet<String> result = new SharedTreeSet<String>();
        for (AbstractHistoryPattern c: children) {
            result = result.with(c.stimuliInUse(bb));
        }
        return result;
    }
    
    // Pre: None
    // Post: Returns all responses referenced by this StimulusHistory
    public SharedSet<String> responsesInUse(BlackBox bb) {
        SharedSet<String> result = new SharedTreeSet<String>();
        for (AbstractHistoryPattern c: children) {
            result = result.with(c.responsesInUse(bb));
        }
        return result;
    }
    
    // Pre: None
    // Post: Returns true if this or any child of this uses the 
    //       "response" keyword
    public boolean checksResponse() {
        for (AbstractHistoryPattern c: children) {
            if (c.checksResponse()) {
                return true;
            }
        }
        return false;
    }
    
    // Pre: None
    // Post: Returns all macros referenced by this StimulusHistory
    public SharedSet<String> macrosInUse(BlackBox bb) {
        SharedSet<String> result = new SharedTreeSet<String>();
        for (AbstractHistoryPattern c: children) {
            result = result.with(c.macrosInUse(bb));
        }
        return result;
    }
    
    // Pre: None
    // Post: Returns all symbols (stimuli, responses, macros) referenced by
    //       this AbstractHistoryPattern
    public SharedSet<String> symbolsInUse(BlackBox bb) {
    	SharedSet<String> result = new SharedTreeSet<String>();
        for (AbstractHistoryPattern c: children) {
            result = result.with(c.symbolsInUse(bb));
        }
        return result;
    }
    
    // Pre: None
    // Post: Returns a new AbstractHistoryPattern with all instances of 
    //       oldName replaced by newName
    public AbstractHistoryPattern withRenamedSymbol(BlackBox bb, String oldName, String newName) {
    	if (symbolsInUse(bb).contains(oldName)) {
    		ArrayList<AbstractHistoryPattern> reconstructedChildren = new ArrayList<AbstractHistoryPattern>();
    		for (AbstractHistoryPattern c: children) {
    			reconstructedChildren.add(c.withRenamedSymbol(bb, oldName, newName));
    		}
    		return reconstructed(reconstructedChildren);
    	} else {
    		return this;
    	}
    }
    
    protected void checkReconstructedChildren(ArrayList<AbstractHistoryPattern> children, int numExpected) {
    	if (children.size() != numExpected) {
    		throw new IllegalArgumentException("Expected: " + numExpected + "; children.size() == " + children.size());
    	}
    }
    
    protected void rejectReconstruction() {
    	throw new IllegalStateException("This should be impossible");
    }
    
    abstract protected AbstractHistoryPattern reconstructed(ArrayList<AbstractHistoryPattern> children);

    // Pre: isError()
    // Post: Returns the first error node it can find
    public PatternError findError() {
        return new PatternError(getErrorMsg());
    }
}
