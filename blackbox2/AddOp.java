package blackbox2;
//Copyright 2009 by Gabriel J. Ferrer
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

import java.util.ArrayList;

public class AddOp extends CountingOp {
    public AddOp(CountingPattern left, CountingPattern right) {
        super(left, right);
    }
    
    public int doOp(int one, int two) {
        return one + two;
    }

    public String opStr() {return "+";}

	@Override
	protected AbstractHistoryPattern reconstructed(ArrayList<AbstractHistoryPattern> children) {
		checkReconstructedChildren(children, 2);
		return new AddOp((CountingPattern)children.get(0), (CountingPattern)children.get(1));
	}
}
