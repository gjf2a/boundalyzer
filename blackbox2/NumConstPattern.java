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

import java.util.ArrayList;

public class NumConstPattern extends CountingPattern {
    private int val;

    public NumConstPattern() {this(0);}
    public NumConstPattern(int n) {
        super();
        val = n;
    }
    
    protected int valueHelp(StimulusSeq hist, Memo m) {return val;}
    
    public int tableValue(CountTable ct) {return val;}
    
    public void addVarsTo(CountTable table) {}
    
    public void addVarValuesTo(CountTable table, StimulusSeq hist, Memo m) {}

    public String toString() {return Integer.toString(val);}
    
    public boolean hasJunkInteractions(StimulusSeq seq) {
        return seq.length() > 0;
    }
    
	protected AbstractHistoryPattern reconstructed(ArrayList<AbstractHistoryPattern> children) {
		rejectReconstruction();
		return this;
	}
}
