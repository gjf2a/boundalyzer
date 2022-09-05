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

public class MultiResponseDependency extends ResponseDependency {

    private ResponseDependency left, right;
    
    // Pre: one, two != null
    public MultiResponseDependency(HistoryPattern hist, ResponseDependency one, ResponseDependency two) {
        super(hist, false);
        if (one == null) {throw new IllegalArgumentException("one is null");}
        if (two == null) {throw new IllegalArgumentException("two is null");}
        this.left = one;
        this.right = two;
    }

    @Override
    public boolean hasNeededEstimates(ResponseEstimates re, SharedSet<String> ancestors) {
        return left.hasNeededEstimates(re, ancestors) && right.hasNeededEstimates(re, ancestors);
    }
    
    public String toString() {
        return notFilter("(multi " + left + " " + right + ")");
    }

    @Override
    public boolean hasCycle(ResponseEstimates re, SharedSet<String> ancestors) {
        return left.hasCycle(re, ancestors) || right.hasCycle(re, ancestors);
    }
}
