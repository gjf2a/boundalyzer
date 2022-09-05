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

import java.util.*;

// Represents a parsing error

public class PatternError extends HistoryPattern {
    private String msg;
    
    public PatternError(String msg) {
        super();
        this.msg = msg;
    }
    
    public boolean isError() {return true;}
    
    protected boolean matchesHelp(StimulusSeq hist, Memo m) {
        return false;
    }
    
    public String toString() {return msg;}
    
    protected int heuristicHelp(StimulusSeq prefix, ResponseEstimates respTable, Flags f, Memo m) {
        return Integer.MAX_VALUE;
    }
    
    public ResponseDependency getResponseDependencies(BlackBox box, boolean negated) {
        return new SimpleResponseDependency(this, negated);
    }

	@Override
	protected AbstractHistoryPattern reconstructed(ArrayList<AbstractHistoryPattern> children) {
		rejectReconstruction();
		return this;
	}
}
