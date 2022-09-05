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

package blackbox2;

import java.util.ArrayList;

import util.*;

public class SymbolMatcher extends HistoryPattern {
    private String name;
    private SingleStimulus stimHist;
    private static boolean debug = false, debugHeuristic = false;
    
    public SymbolMatcher(String n) {
        super();
        name = n;
        stimHist = new SingleStimulus();
    }
    
    private HistoryPattern resolveName(BlackBox bb) {
    	try {
    		if (isStimulusIn(bb)) {
    			return stimHist;
    		} else if (isMacroIn(bb)) {
    			return new MacroHistory(bb);
    		} else if (bb.allResponses().contains(name)) {
    			throw new NoResponseKeywordException(name);
    		} else {
    			throw new UndefinedSymbolException(name);
    		}
    	} catch (StackOverflowError soe) {
    		throw new CircularMacroException(name);
    	}
    }
    
    private boolean isMacroIn(BlackBox bb) {
    	return bb.allMacros().contains(name);
    }
    
    private boolean isStimulusIn(BlackBox bb) {
    	return bb.allStimuli().contains(name);
    }
    
    public boolean matchesHelp(StimulusSeq seq, Memo m) {
    	try {
    		return resolveName(seq.getBox()).matchesHelp(seq, m);
    	} catch (CircularMacroException cme) {
    		throw cme;
    	} catch (RuntimeException e) {
    		if (isMacroIn(seq.getBox())) {
    			throw new ExceptionInSymbolException(e, name);
    		} else {
    			throw e;
    		}
    	}
    }
    
    protected int heuristicHelp(StimulusSeq prefix, ResponseEstimates respTable, Flags f, Memo m) {
        if (debugHeuristic) {
            int num = resolveName(prefix.getBox()).heuristic(prefix, respTable, f, m);
            System.out.println("Returning " + num + " for " + this + " on prefix: " + prefix);
            return num;
        }
        return resolveName(prefix.getBox()).heuristic(prefix, respTable, f, m);
    }
    
    public sat.Expr getSatExpr(BlackBox box) {
        return resolveName(box).getSatExpr(box);
    }
    
    public SharedSet<String> stimuliInUse(BlackBox bb) {
    	SharedSet<String> result = new SharedTreeSet<String>();
    	if (isStimulusIn(bb)) {
    		result = result.with(name);
    	} else if (isMacroIn(bb)) {
    		result = result.with(new MacroHistory(bb).stimuliInUse(bb));
    	} 
    	return result;
    }
    
    public SharedSet<String> macrosInUse(BlackBox bb) {
    	SharedSet<String> result = new SharedTreeSet<String>();
    	if (isMacroIn(bb)) {
    		result = result.with(name).with(new MacroHistory(bb).macrosInUse(bb));
    	} 
    	return result;
    }
    
    public SharedSet<String> symbolsInUse(BlackBox bb) {
    	SharedSet<String> result = new SharedTreeSet<String>();
    	result = result.with(name);
    	if (isMacroIn(bb)) {
    		result = result.with(new MacroHistory(bb).symbolsInUse(bb));
    	}
    	return result;
    }
    
    public SharedSet<String> invalidSymbols(BlackBox bb) {
        SharedSet<String> result = new SharedTreeSet<String>();
        if (!bb.allStimuli().contains(name) && !bb.allMacros().contains(name)) {
            result = result.with(name);
        }
        return result;
    }
    
    public String toString() {
        return name;
    }

    @Override
    public ResponseDependency getResponseDependencies(BlackBox box, boolean negated) {
        return this.resolveName(box).getResponseDependencies(box, negated);
    }
    
    private class SingleStimulus extends SingleMatcher {
        public SingleStimulus() {
            super(name);
        }
        
        public String toString() {return single();}
        
        protected boolean matchesAt(StimulusSeq seq, int n) {
            return seq.stimulusAt(n).equals(single());
        }
        
        protected boolean hasSingle(BlackBox box, int row) {
            if (debug) {System.out.println("box.stimulusAt(" + row + "): \"" + box.stimuliAt(row) + "\" single(): \"" + single() + "\" includes? " + box.stimuliAt(row).includes(single()));}
            return box.stimuliAt(row).includes(single());
        }
        
        public SharedSet<String> stimuliInUse(BlackBox bb) {
            SharedSet<String> result = new SharedTreeSet<String>();
            return result.with(single());
        }
        
        public SharedSet<String> symbolsInUse(BlackBox bb) {
        	return stimuliInUse(bb);
        }

        @Override
        public ResponseDependency getResponseDependencies(BlackBox box, boolean negated) {
            return new SimpleResponseDependency(this, negated);
        }
        
        public AbstractHistoryPattern withRenamedSymbol(BlackBox bb, String oldName, String newName) {
        	rejectReconstruction();
        	return this;
        }
        
        protected AbstractHistoryPattern reconstructed(ArrayList<AbstractHistoryPattern> children) {
    		rejectReconstruction();
    		return this;
    	}
    }
    
    private class MacroHistory extends IndirectMatcher {
        private BlackBox bb;
        
        public MacroHistory(BlackBox bb) {
            super(new AbstractHistoryPattern[0]);
            this.bb = bb;
        }
        
        public HistoryPattern target() {
            return bb.macroDefinition(name);
        }
        
        public SharedSet<String> stimuliInUse(BlackBox bb) {
            checkBlackBox(bb);
            return target().stimuliInUse(bb);
        }
        
        public SharedSet<String> responsesInUse(BlackBox bb) {
        	checkBlackBox(bb);
        	return target().responsesInUse(bb);
        }
        
        public SharedSet<String> macrosInUse(BlackBox bb) {
            checkBlackBox(bb);
            return target().macrosInUse(bb).with(name);
        }
        
        public SharedSet<String> symbolsInUse(BlackBox bb) {
        	checkBlackBox(bb);
        	return target().symbolsInUse(bb);
        }
        
        private void checkBlackBox(BlackBox bb) {
            if (bb != this.bb) {
                throw new IllegalArgumentException("Multiple black boxes in use!");
            }        
        }
        
        public String toString() {return name;}
        
        public AbstractHistoryPattern withRenamedSymbol(BlackBox bb, String oldName, String newName) {
        	rejectReconstruction();
        	return this;
        }
        
        protected AbstractHistoryPattern reconstructed(ArrayList<AbstractHistoryPattern> children) {
    		rejectReconstruction();
    		return this;
    	}
    }
    
    public AbstractHistoryPattern withRenamedSymbol(BlackBox bb, String oldName, String newName) {
    	if (this.name.equals(oldName)) {
    		return new SymbolMatcher(newName);
    	} else {
    		return this;
    	}
    }

	@Override
	protected AbstractHistoryPattern reconstructed(ArrayList<AbstractHistoryPattern> children) {
		rejectReconstruction();
		return this;
	}
}

