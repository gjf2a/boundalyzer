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

public class CountPattern extends CountingPattern {
    private HistoryPattern expr;
    private SortedSet<Integer> usedIndices;
    
    public CountPattern(HistoryPattern expr) {
        super(new AbstractHistoryPattern[]{expr});
        this.expr = expr;
    }
    
    public HistoryPattern getExpr() {return expr;}

    // See value() below, except this one has no limit
    protected int valueHelp(StimulusSeq hist, Memo m) {
        return value(hist, Integer.MAX_VALUE, m);
    }
    
    // Pre: hist != null
    // Post: Returns the number of non-overlapping subsequences of hist for 
    //       which this Count's expr is true; if > max, returns max
    //       When counting, prefers smaller subsequences to larger ones
    //       Also stores all the matching indices in the private field 
    //       usedIndices
    public int value(StimulusSeq hist, int max, Memo m) {
        return computeValue(hist, max, false, m);
    }
    
    private int computeValue(StimulusSeq hist, int max, boolean negated, Memo m) {
        int count = 0;
        usedIndices = new TreeSet<Integer>();
        for (int subseqSize = 1; subseqSize <= hist.length(); ++subseqSize) {
            for (int start = 0; start <= hist.length() - subseqSize; ++start) {
                int end = start + subseqSize - 1;
                if (usedIndices.subSet(start, end + 1).isEmpty()) {
                    if (expr.matches(hist.subseq(start, end), m) == !negated) {
                        count += 1;
                        for (int i = start; i <= end; ++i) {
                            usedIndices.add(i);
                        }
                    }
                }
            }
        }
        return Math.min(count, max);
    }
    
    public String toString() {
        return "count " + expr;
    }
    
    public int tableValue(CountTable ct) {
        return ct.getValue(this);
    }
    
    public void addVarsTo(CountTable table) {
        table.addCounter(this);
    }
    
    public void addVarValuesTo(CountTable table, StimulusSeq hist, Memo m) {
        table.setValue(this, valueHelp(hist, m));
    }
    
    public int heuristic(StimulusSeq prefix, ResponseEstimates respTable, Memo m) {
        return expr.heuristic(prefix, respTable, new Flags(false, true), m);
    }
    
    @Override
	protected AbstractHistoryPattern reconstructed(ArrayList<AbstractHistoryPattern> children) {
    	checkReconstructedChildren(children, 2);
    	return new CountPattern((HistoryPattern)children.get(0));
    }
}
