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

public class IndependenceThread extends SearchThread<String> {
    private Map<RowPair,HeuristicSatSearcher> rowsToSearcher;
    private Map<String,Set<RowPair>> stimToRows;
    private int count;
    private Set<String> unknown, goodStimuli;
    private Map<String,StimulusSeq> badExamples;
    private BlackBox box;
    
    public IndependenceThread(BlackBox box) {
        super();
        this.box = box;
        
        rowsToSearcher = new TreeMap<RowPair,HeuristicSatSearcher>();
        unknown = new TreeSet<String>();
        goodStimuli = new TreeSet<String>();
        badExamples = new TreeMap<String,StimulusSeq>();
        
        for (String stim: box.allStimuli()) {
            for (int row1: box.rowsMatchingStim(stim)) {
                for (int row2: box.rowsMatchingStim(stim)) {
                    if (row1 > row2) {
                        unknown.add(stim);
                        HistoryPattern hist = new IntersectMatcher(box.historyAt(row1), box.historyAt(row2));
                        RowPair rp = new RowPair(row1, row2, stim);
                        rowsToSearcher.put(rp, new HeuristicSatSearcher(box, hist));
                        addRowsFor(stim, rp);
                    }
                }
            }
            if (!unknown.contains(stim)) {goodStimuli.add(stim);}
        }
        
        count = 0;
    }
    
    private void addRowsFor(String stim, RowPair rp) {
        if (stimToRows == null) {
            stimToRows = new TreeMap<String,Set<RowPair>>();
        }
        
        if (stimToRows.get(stim) == null) {
            stimToRows.put(stim, new TreeSet<RowPair>());
        }
        
        stimToRows.get(stim).add(rp);
    }
    
    public SearchData<String> makeSearchData() {
        SearchData<String> data = new SearchData<String>();
        data.setNodesExpanded(count);
        data.addElements("Stimuli with Independent Rows", goodStimuli);
        data.addExamples("Stimuli with Dependent Rows", badExamples);
        data.addElements("Unknown dependency status", unknown);
        data.setGoodNewsField("Stimuli with Independent Rows", "All rows independent");
        return data;
    }
    
    public void search() {
        while (unknown.size() > 0 && !terminated()) {
            for (String stim: stimToRows.keySet()) {
                if (unknown.contains(stim)) {
                    ArrayList<RowPair> allPairsFor = new ArrayList<RowPair>(stimToRows.get(stim).size());
                    for (RowPair rp: stimToRows.get(stim)) {
                        allPairsFor.add(rp);
                    }
                    for (RowPair rp: allPairsFor) {
                        HeuristicSatSearcher bss = rowsToSearcher.get(rp);
                        if (bss.canExpand()) {
                            bss.expandNext();
                            count += 1;
                            if (bss.hasValidLast()) {
                                badExamples.put(stim, bss.last());
                                unknown.remove(stim);
                            }
                        } else {
                            stimToRows.get(stim).remove(rp);
                            if (stimToRows.get(stim).size() == 0) {
                                goodStimuli.add(stim);
                                unknown.remove(stim);
                            }
                        }
                    }
                }
            }
        }
    }
    
    private class RowPair implements Comparable<RowPair> {
        public RowPair(int r1, int r2, String stim) {
            row1 = Math.max(r1,r2); 
            row2 = Math.min(r1,r2);
            this.stim = stim;
            checkStim(row1);
            checkStim(row2);
        }
        
        private void checkStim(int row) {
        	//if (!box.rowsMatchingStim(stim).contains(row)) {
            if (!box.stimuliAt(row).includes(stim)) {
                throw new IllegalArgumentException("Row " + row + " does not include stimulus " + stim);
            }
        }
        
        public int row1() {return row1;}
        public int row2() {return row2;}
        public String stimulus() {return stim;}
        
        public boolean equals(Object other) {
            if (other instanceof RowPair) {
                RowPair that = (RowPair)other;
                return this.compareTo(that) == 0;
            } else {
                return false;
            }
        }
        
        public int hashCode() {return row1 * box.numRows() + row2;}
        
        public int compareTo(RowPair that) {
            int result = this.stimulus().compareTo(that.stimulus());
            if (result == 0) {
                result = this.hashCode() - that.hashCode();
            }
            return result;
        }
        
        private int row1, row2;
        private String stim;
    }
}

