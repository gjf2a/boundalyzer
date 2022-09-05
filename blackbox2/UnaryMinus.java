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

public class UnaryMinus extends CountingPattern {
    private CountingPattern expr;
    
    public UnaryMinus(CountingPattern nh) {
        super(new AbstractHistoryPattern[]{nh});
        expr = nh;
    }
    
    protected int valueHelp(StimulusSeq hist, Memo m) {
        return -expr.value(hist, m);
    }
    
    public String toString() {
        return '-' + expr.toString();
    }
    
    public int tableValue(CountTable ct) {
        return -expr.tableValue(ct);
    }
    
    public void addVarsTo(CountTable table) {
        expr.addVarsTo(table);
    }
    
    public void addVarValuesTo(CountTable table, StimulusSeq hist, Memo m) {
        expr.addVarValuesTo(table, hist, m);
    }

	@Override
	protected AbstractHistoryPattern reconstructed(ArrayList<AbstractHistoryPattern> children) {
		checkReconstructedChildren(children, 1);
		return new UnaryMinus((CountingPattern)children.get(0));
	}
}
