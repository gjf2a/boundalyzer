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

public class ExprThread extends SearchThread<String> {
    private HistoryPattern expr;
    private BlackBox box;
    private StimulusSeq example;
    private int count;
    
    public ExprThread(BlackBox box, HistoryPattern expr) {
        super();
        this.expr = expr;
        this.box = box;
        example = null;
    }
    
    protected void search() {
        HeuristicSatSearcher bss = new HeuristicSatSearcher(box, expr);
        while (!terminated() && bss.canExpand() && example == null) {
            bss.expandNext();
            count += 1;
            if (bss.hasValidLast()) {example = bss.last();}
        }
    }
    
    public SearchData<String> makeSearchData() {
        SearchData<String> data = new SearchData<String>();
        data.setNodesExpanded(count);
        data.addElement("Macro status", (example == null ? "Inconsistent" : "Consistent"));
        return data;
    }
}
