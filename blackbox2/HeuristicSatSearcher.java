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

public class HeuristicSatSearcher {
    private sat.Searcher satisfier;
    private HeuristicSearcher generator;
    private HistoryPattern toCheck;
    private final boolean debug = false;
    
    public HeuristicSatSearcher(BlackBox box, HistoryPattern hist) {
        toCheck = hist;
        satisfier = new sat.Searcher(hist.getSatExpr(box));
        generator = new HeuristicSearcher(box, hist);
    }
    
    public HeuristicSatSearcher(BlackBox box, int targetRow) {
        this(box, box.historyAt(targetRow));
    }
    
    public boolean canExpand() {
        if (debug) {
            System.out.println("toCheck: " + toCheck);
            System.out.println("satisfier.complete(): " + satisfier.complete());
            System.out.println("satisfier.satisfied(): " + satisfier.satisfied());
            System.out.println("generator.canExpand(): " + generator.canExpand());
        }
        return (!satisfier.complete() || satisfier.satisfied()) && generator.canExpand();
    }
    
    public void expandNext() {
        if (!satisfier.complete() && !satisfier.satisfied()) {
            satisfier.expandNext();
        }
        
        if (canExpand()) {generator.expandNext();}
    }
    
    public StimulusSeq last() {return generator.last();}
    
    public boolean hasValidLast() {return generator.hasValidLast();}
}
