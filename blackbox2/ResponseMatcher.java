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

public class ResponseMatcher extends SingleMatcher {
    
    private static final boolean debugHeuristic = false;
    
    private String str, resp;
    
    public ResponseMatcher(String resp) {
        super(resp);
        this.resp = resp;
        str = "response " + resp;
    }
    
    public boolean checksResponse() {
        return true;
    }
    
    protected boolean matchesAt(StimulusSeq seq, int n) {
        return seq.responseAt(n).includes(single());
    }
    
    protected boolean hasSingle(BlackBox bb, int row) {
        return bb.responseAt(row).includes(single());
    }
    
    protected int heuristicHelp(StimulusSeq prefix, ResponseEstimates respTable, Flags f, Memo m) {        
        if (matches(prefix, m)) {
            return 0;
        } else if (f.isNegated() || !f.isInclusiveStart()) {
            if (debugHeuristic) {
                System.out.println("ResponseMatchHistory.heuristicHelp() using SingleMatchHistory.heuristicHelp()");
            }
            return super.heuristicHelp(prefix, respTable, f, m);
        } else {
            if (respTable.hasEstimateTo(single())) {
                if (debugHeuristic) System.out.println("ResponseMatchHistory.heuristicHelp(): (from table) " + respTable.estimateTo(single()));
                return respTable.estimateTo(single());
            } else {
                if (debugHeuristic) System.out.println("ResponseMatchHistory.heuristicHelp(): (default) " + Integer.MAX_VALUE);
                return Integer.MAX_VALUE;
            }
        }
    }

    public String toString() {
        return str;
    }
    /*
    public SharedSet<String> stimuliInUse(BlackBox bb) {
        SharedSet<String> result = new SharedTreeSet<String>();
        for (int row: bb.rowsWithResp(single())) {
            for (String stim: bb.stimulusAt(row).stimuli(bb)) {
                result = result.with(stim);
            }
        }
        return result;
    }
    */
    public SharedSet<String> responsesInUse(BlackBox bb) {
        SharedSet<String> result = new SharedTreeSet<String>();
        return result.with(single());
    }
    
    public SharedSet<String> symbolsInUse(BlackBox bb) {
    	return responsesInUse(bb);
    }
    
    public SharedSet<String> invalidSymbols(BlackBox bb) {
        SharedSet<String> result = new SharedTreeSet<String>();
        if (!bb.allResponses().contains(single())) {
            result = result.with(single());
        }
        return result;
    }

    @Override
    public ResponseDependency getResponseDependencies(BlackBox box, boolean negated) {
        return new ResponseResponseDependency(this, negated);
    }
    
    public AbstractHistoryPattern withRenamedSymbol(BlackBox bb, String oldName, String newName) {
    	if (resp.equals(oldName)) {
    		return new ResponseMatcher(newName);
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
