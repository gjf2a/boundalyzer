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

public class NumParenPattern extends CountingPattern {
    private CountingPattern inside;
    
    public NumParenPattern(CountingPattern target) {
        super(new AbstractHistoryPattern[]{target});
        inside = target;
    }
    
    protected int valueHelp(StimulusSeq hist, Memo m) {
        return inside.value(hist, m);
    }
    
    public int tableValue(CountTable ct) {
        return inside.tableValue(ct);
    }
    
    public void addVarsTo(CountTable table) {
        inside.addVarsTo(table);
    }
    
    public void addVarValuesTo(CountTable table, StimulusSeq hist, Memo m) {
        inside.addVarValuesTo(table, hist, m);
    }
    
    public String toString() {
        return '(' + inside.toString() + ')';
    }

	@Override
	protected AbstractHistoryPattern reconstructed(ArrayList<AbstractHistoryPattern> children) {
		checkReconstructedChildren(children, 1);
		return new NumParenPattern((CountingPattern)children.get(0));
	}
}
