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

abstract public class SingleMatcher extends HistoryPattern {
    private String single;
    
    public SingleMatcher(String single) {
        super();
        this.single = single;
    }
    
    protected String single() {return single;}
    
    public boolean matchesHelp(StimulusSeq seq, Memo m) {
        if (seq.length() != 1) {
            return false;
        } else {
            return matchesAt(seq, 0);
        }
    }
    
    abstract protected boolean matchesAt(StimulusSeq seq, int n);
    
    abstract protected boolean hasSingle(BlackBox bb, int row);
    
    protected int heuristicHelp(StimulusSeq prefix, ResponseEstimates respTable, Flags f, Memo m) {
        if (f.isNegated()) {
            if (f.isInclusive()) {
                return containsSingle(prefix) ? Integer.MAX_VALUE : 0;
            } else {
                return (prefix.length() == 0 || !containsSingle(prefix)) ? 0:1;
            }
        } else {
            if (prefix.length() == 0 || (f.isInclusive() && !containsSingle(prefix))) {
                return 1;
            } else if (containsSingle(prefix) && (prefix.length() == 1 || f.isInclusive())) {
                return 0;
            } else {
                return Integer.MAX_VALUE;
            }
        }
    }
    
    private void printResult(StimulusSeq prefix, BackMoves result) {
        System.out.println("SingleMatchHistory: " + this + " prefix: " + prefix + " result: " + result);
    }
    
    protected boolean containsSingle(StimulusSeq prefix) {
        for (int i = 0; i < prefix.length(); ++i) {
            if (matchesAt(prefix, i)) {return true;}
        }
        return false;
    }
}
