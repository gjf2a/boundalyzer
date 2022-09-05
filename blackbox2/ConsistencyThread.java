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

public class ConsistencyThread extends SearchThread<Integer> {
    private HeuristicSatSearcher[] searchers;
    private int count;
    private Set<Integer> unknown, consistent, inconsistent;
    
    public ConsistencyThread(BlackBox box) {
        super();
        searchers = new HeuristicSatSearcher[box.numRows()];
        unknown = new TreeSet<Integer>();
        consistent = new TreeSet<Integer>();
        inconsistent = new TreeSet<Integer>();
        count = 0;

        for (int i = 0; i < box.numRows(); ++i) {
            searchers[i] = new HeuristicSatSearcher(box, i);
            unknown.add(i);
        }
    }
    
    public void search() {
        while (unknown.size() > 0 && !terminated()) {
            for (int i = 0; i < searchers.length; ++i) {
                if (unknown.contains(i)) {
                    if (searchers[i].canExpand()) {
                        searchers[i].expandNext();
                        count += 1;
                        if (searchers[i].hasValidLast()) {
                            consistent.add(i);
                            unknown.remove(i);
                        }
                    } else {
                        inconsistent.add(i);
                        unknown.remove(i);
                    }
                }
            }
        }
    }
    
    public SearchData<Integer> makeSearchData() {
        SearchData<Integer> data = new SearchData<Integer>();
        data.setNodesExpanded(count);
        data.addElements("Well-defined rows", consistent);
        data.addElements("Internally inconsistent rows", inconsistent);
        data.addElements("Unknown Rows", unknown);
        data.setGoodNewsField("Well-defined rows", "All rows well-defined");
        return data;
    }
}
