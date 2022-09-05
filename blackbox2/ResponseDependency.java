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

import util.SharedSet;

import java.io.File;

public abstract class ResponseDependency {
    private HistoryPattern basis;
    private boolean negated;
    
    public ResponseDependency(HistoryPattern basis, boolean negated) {
        this.basis = basis;
        this.negated = negated;
    }
    
    public HistoryPattern getBasis() {return basis;}
    
    public boolean isNegated() {return negated;}
    
    abstract public boolean hasNeededEstimates(ResponseEstimates re, SharedSet<String> ancestors);
    
    abstract public boolean hasCycle(ResponseEstimates re, SharedSet<String> ancestors);
    
    abstract public String toString();
    
    protected String notFilter(String toStr) {
        return isNegated() ? "(not " + toStr + ")" : toStr;
    }

    public static void main(String[] args) {
        BlackBoxApp bba = new BlackBoxApp();
        bba.load(new File(args[0]));
        if (!bba.lastOpSucceeded()) {
            System.out.println(bba.lastOpMessage());
            System.exit(1);
        }

        HistoryPattern hist = null;
        try {
            int row = Integer.parseInt(args[1]);
            if (row < 0 || row >= bba.getBlackBox().numRows()) {
                System.out.println("Illegal row " + row);
                System.exit(1);
            }
            hist = bba.getBlackBox().historyAt(row);
        } catch (NumberFormatException nfe) {
            hist = bba.parseHistory(args[1]);
        }
        
        if (hist == null || hist.isError()) {
            System.out.println(hist);
            System.exit(1);
        }
        
        System.out.println("Goal: " + hist);
        
        System.out.println(hist.getResponseDependencies(bba.getBlackBox()));
    }
}
