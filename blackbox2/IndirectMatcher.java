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

abstract public class IndirectMatcher extends HistoryPattern {
    public IndirectMatcher(AbstractHistoryPattern[] children) {super(children);}
    
    // Pre: None
    // Post: Returns reference to indirect target
    abstract public HistoryPattern target();
    
    public boolean matchesHelp(StimulusSeq hist, Memo m) {
        return target().matches(hist, m);
    }
    
    protected int heuristicHelp(StimulusSeq prefix, ResponseEstimates respTable, Flags f, Memo m) {
        return target().heuristic(prefix, respTable, f, m);
    }
    
    public sat.Expr getSatExpr(BlackBox bb) {
        return target().getSatExpr(bb);
    }
    /*
    public boolean hasJunkInteractions(StimulusSeq seq) {
        return target().hasJunkInteractions(seq);
    }
    */

    @Override
    public ResponseDependency getResponseDependencies(BlackBox box, boolean negated) {
        return target().getResponseDependencies(box, negated);
    }
}
