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

// BUG: If a possible predecessor history is impossible, it will NOT help 
// towards completeness.

public class CompletenessThread extends HistThread {
    public CompletenessThread(BlackBoxApp boxApp) {super(boxApp);}
    
    protected HeuristicSatSearcher makeSearcher(BlackBoxApp boxApp, String stim) {
        String expr = "";
        for (int row: box.rowsMatchingStim(stim)) {
            HistoryPattern hist = box.historyAt(row);
            if (expr.length() > 0) {
                expr += " and ";
            }
            expr += "not (" + hist + ")";
        }
        return new HeuristicSatSearcher(box, boxApp.parseHistory(expr));
    }
    
    protected String goodTitle() {return "Completely Specified Stimuli";}
    protected String badTitle() {return "Incompletely Specified Stimuli";}
    protected String goodNews() {return "All stimuli completely specified";}
}

