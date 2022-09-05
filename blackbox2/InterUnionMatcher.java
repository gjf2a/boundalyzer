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

abstract public class InterUnionMatcher extends HistoryPattern {
    private HistoryPattern one, two;
    
    public InterUnionMatcher(HistoryPattern first, HistoryPattern second) {
        super(new AbstractHistoryPattern[]{first, second});
        one = first;
        two = second;
    }
    
    protected HistoryPattern getOne() {return one;}
    protected HistoryPattern getTwo() {return two;}
    
    protected int orHeuristic(StimulusSeq prefix, ResponseEstimates respTable, Flags f, Memo m) {
        return Math.min(one.heuristic(prefix, respTable, f, m), 
                        two.heuristic(prefix, respTable, f, m));
    }
    
    protected int andHeuristic(StimulusSeq prefix, ResponseEstimates respTable, Flags f, Memo m) {
        int est1 = one.heuristic(prefix, respTable, f, m);
        int est2 = two.heuristic(prefix, respTable, f, m);
        if (f.skipsImpossibleEstimate()) {
            if (f.isImpossibleEstimate(est1)) {
                return est2;
            } else if (f.isImpossibleEstimate(est2)) {
                return est1;
            }
        }
        return Math.max(est1, est2);
    }
    
    @Override
    public ResponseDependency getResponseDependencies(BlackBox box, boolean negated) {
        return new MultiResponseDependency(this, getOne().getResponseDependencies(box, negated), getTwo().getResponseDependencies(box, negated));
    }
    
    /*
    public boolean hasJunkInteractions(StimulusSeq seq) {
        return getOne().hasJunkInteractions(seq) && getTwo().hasJunkInteractions(seq);
    }
    */
}   
