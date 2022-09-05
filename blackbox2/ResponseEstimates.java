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

// Next steps to implementation:
//
// Triple-check the sanity of the precondition for estimateRows().

import java.io.File;
import java.util.*;
import util.MultiMap;
import util.HashMultiMap;
import util.SharedSet;

public class ResponseEstimates {
    
    private StimulusSeq current;
    private int[] rowToEstimate;
    private Map<String,Integer> responseNameToEstimate;
    private ResponseDependency[] rowToDependencies;
    private MultiMap<String,Integer> responseToRowsPending;
    private SortedSet<Integer> estimableRows;
    private Set<Integer> unestimatedRows;
    
    private static boolean debug = false;
    
    public ResponseEstimates(StimulusSeq seq) {
        current = seq;
        rowToEstimate = new int[numRows()];
        responseNameToEstimate = new HashMap<String,Integer>();
        rowToDependencies = new ResponseDependency[numRows()];
        responseToRowsPending = new HashMultiMap<String,Integer>();
        estimableRows = new TreeSet<Integer>();
        unestimatedRows = new LinkedHashSet<Integer>();
        
        for (int row = 0; row < numRows(); ++row) {
            rowToEstimate[row] = Integer.MAX_VALUE;
            unestimatedRows.add(row);
            rowToDependencies[row] = getBox().historyAt(row).getResponseDependencies(getBox());
            for (String resp: getBox().responseAt(row).responses()) {
                responseToRowsPending.put(resp, row);
            }
        }
        
        for (int row = 0; row < numRows(); ++row) {
            SharedSet<String> ancestors = getBox().responseAt(row).responses();
            if (rowToDependencies[row].hasNeededEstimates(this, ancestors)) {
                if (debug) {System.out.println("Row " + row + " with history '" + getBox().historyAt(row) + "' is estimable immediately");}
                estimableRows.add(row);
            }            
        }
    }
    
    public int numRows() {return getBox().numRows();}
    
    // Pre: None
    // Post: Returns the rows for which estimates will be calulated by the
    //       next call to estimateRows()
    public Set<Integer> nextToEstimate() {return estimableRows;}
    
    // Pre: None
    // Post: Returns all rows that are not yet estimated
    public Set<Integer> notEstimated() {return unestimatedRows;}
    
    // Pre: !isFutile() && !allRowsEstimated()
    // Post: For all rows r in nextToEstimate() => hasEstimateTo(r) is true;
    //       if no further estimates are possible:
    //       - if all rows are estimated => allRowsEstimated() is true 
    //       - otherwise => isFutile() is true
    public void estimateRows() {
        while (!estimableRows.isEmpty()) {
            int row = estimableRows.first();
            estimableRows.remove(row);
            unestimatedRows.remove(row);
            int est = getBox().historyAt(row).heuristic(current, this);
            rowToEstimate[row] = est;
            int responseEst = getResponseEst(est);
            for (String resp: getBox().responseAt(row).responses()) {
                if (!responseNameToEstimate.containsKey(resp) || responseNameToEstimate.get(resp) > responseEst) {
                    responseNameToEstimate.put(resp, responseEst);
                }
                responseToRowsPending.remove(resp, row);
            }
        }
        
        for (int row: unestimatedRows) {
            SharedSet<String> ancestors = getBox().responseAt(row).responses();
            if (rowToDependencies[row].hasNeededEstimates(this, ancestors)) {
                estimableRows.add(row);
            }
        }
    }

    private int getResponseEst(int estimate) {
        int responseEst = estimate + 1;
        return (estimate < responseEst) ? responseEst : estimate;
    }
    
    // Pre: None
    // Post: isFutile() || allRowsEstimated() 
    public void estimateAllRows() {
        while (!isFutile() && !allRowsEstimated()) {
            estimateRows();
        }
    }
    
    public StimulusSeq currentSeq() {return current;}
    
    public BlackBox getBox() {return current.getBox();}
    
    // Pre: None
    // Post: Returns true if an estimate has been stored for row
    public boolean hasEstimateTo(int row) {
        return !unestimatedRows.contains(row);
    }
    
    // Pre: hasEstimateTo(row) || isFutile()
    // Post: Returns stored estimate to row
    public int estimateTo(int row) {
        return rowToEstimate[row];
    }
    
    // Pre: getBox().allResponses().contains(responseName)
    // Post: Returns true if an estimate to responseName has been stored
    public boolean hasEstimateTo(String responseName) {
    	return responseNameToEstimate.containsKey(responseName);
    }
    
    // Pre: getBox().allResponses().contains(responseName)
    //      hasEstimateTo(responseName)
    // Post: Returns estimate from currentSeq() to responseName
    public int estimateTo(String responseName) {
    	if (!hasEstimateTo(responseName)) {
    		throw new IllegalArgumentException(responseName + " has no estimate");
    	}
        return responseNameToEstimate.get(responseName);
    }
    
    public boolean allRowsEstimated() {
        return unestimatedRows.isEmpty();
    }
    
    public boolean isFutile() {
        return !allRowsEstimated() && estimableRows.isEmpty();
    }
    
    // Pre: !ancestors.contains(responseName)
    // Post: Returns true if, for all rows r with responseName, 
    //       getBox().historyAt(r).hasCycle(ancestors.with(responseName))
    public boolean hasCycle(String responseName, SharedSet<String> ancestors) {
        ancestors = ancestors.with(responseName);
        for (int row: getBox().rowsWithResp(responseName)) {
            if (!rowToDependencies[row].hasCycle(this, ancestors)) {
                return false;
            }
        }
        return true;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < numRows(); ++row) {
            sb.append("Row: " + row + " estimate: " + estimateTo(row) + "\n");
        }
        
        for (String responseName: responseNameToEstimate.keySet()) {
            sb.append("Response: " + responseName + " estimate: " + estimateTo(responseName) + "\n");
        }
        return sb.toString();
    }
    
    public static void main(String[] args) {
        debug = true;
        BlackBoxApp bba = new BlackBoxApp();
        bba.load(new File(args[0]));
        if (!bba.lastOpSucceeded()) {
            System.out.println(bba.lastOpMessage());
            System.exit(1);
        }

        ResponseEstimates re = new ResponseEstimates(new StimulusSeq(bba.getBlackBox()));
        while (!re.isFutile() && !re.allRowsEstimated()) {
            re.estimateRows();
            System.out.println("Current table:");
            System.out.println(re);
        }
        
        if (re.isFutile()) {
            System.out.println("Cannot resolve outstanding rows");
        } else if (re.allRowsEstimated()) {
            System.out.println("All rows successfully estimated");
        } else {
            System.out.println("The loop should not yet have stopped...");
        }
    }
}
