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

abstract public class HistoryPattern extends AbstractHistoryPattern {
    private final static boolean showHeuristic = false;
    
    public HistoryPattern() {super();}
    public HistoryPattern(AbstractHistoryPattern[] children) {super(children);}
    
    public boolean matches(StimulusSeq seq) {
        return matches(seq, new Memo());
    }
    
    public boolean matches(StimulusSeq seq, Memo m) {
        if (!m.hasMatches(this, seq)) {
            m.storeMatches(this, seq, matchesHelp(seq, m));
        }
        return m.getMatches(this, seq);
    }
    
    abstract protected boolean matchesHelp(StimulusSeq seq, Memo m);

    // Pre: None
    // Post: Returns heuristic estimate of distance from prefix to
    //       a StimulusSeq that is accepted by this;
    //       Returns Integer.MAX_VALUE if the distance is infinite;
    //       Heuristic estimate >= 0
    public int heuristic(StimulusSeq prefix, ResponseEstimates respTable) {
        return heuristic(prefix, respTable, new Flags(), new Memo());
    }
    
    public int heuristic(StimulusSeq prefix, ResponseEstimates respTable, Flags f, Memo m) {
        if (!m.hasEstimate(this, prefix, f)) {
            m.estimating(this, prefix, f);
            int estimate = heuristicHelp(prefix, respTable, f, m);
            if (estimate < 0) {
                String className = this.getClass().getName();
                throw new IllegalStateException("Estimate: " + estimate + " of " + prefix + " by " + this + " (instanceof " + className + ")");
            }
            m.storeEstimate(this, prefix, f, estimate);
        }
        int estimate = m.getEstimate(this, prefix, f);
        if (showHeuristic) {
            System.out.println(this + ": Heuristic evaluation for \"" + prefix + "\": " + estimate);
        }
        return estimate;
    }
    
    abstract protected int heuristicHelp(StimulusSeq prefix, ResponseEstimates respTable, Flags f, Memo m);
    
    public sat.Expr getSatExpr(BlackBox bb) {
        return new sat.Var(toString());
    }
    
    public ResponseDependency getResponseDependencies(BlackBox box) {
        return getResponseDependencies(box, false);
    }
    
    abstract public ResponseDependency getResponseDependencies(BlackBox box, boolean negated);
}
