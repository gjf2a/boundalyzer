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

public class UnionMatcher extends InterUnionMatcher {
    public UnionMatcher(HistoryPattern one, HistoryPattern two) {
        super(one, two);
    }
    
    public boolean matchesHelp(StimulusSeq hist, Memo m) {
        return getOne().matches(hist, m) || getTwo().matches(hist, m);
    }
    
    public String toString() {
        return getOne() + " or " + getTwo();
    }
    
    protected int heuristicHelp(StimulusSeq prefix, ResponseEstimates respTable, Flags f, Memo m) {
        return f.isNegated() 
            ? andHeuristic(prefix, respTable, f, m) 
            : orHeuristic(prefix, respTable, f, m);
    }
    
    public sat.Expr getSatExpr(BlackBox bb) {
        return new sat.Or(getOne().getSatExpr(bb), getTwo().getSatExpr(bb));
    }    
    
    @Override
	protected AbstractHistoryPattern reconstructed(ArrayList<AbstractHistoryPattern> children) {
    	checkReconstructedChildren(children, 2);
    	return new UnionMatcher((HistoryPattern)children.get(0), (HistoryPattern)children.get(1));
    }
}
