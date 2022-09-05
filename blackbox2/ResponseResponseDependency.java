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

public class ResponseResponseDependency extends ResponseDependency {
    private String resp;
    
    public ResponseResponseDependency(ResponseMatcher rmh, boolean negated) {
        super(rmh, negated);
        this.resp = rmh.single();
    }

    @Override
    public boolean hasNeededEstimates(ResponseEstimates re, SharedSet<String> ancestors) {
        return isNegated() ? true : (hasCycle(re, ancestors) || re.hasEstimateTo(resp));
    }

    @Override
    public String toString() {
        return notFilter("response " + resp);
    }

    @Override
    public boolean hasCycle(ResponseEstimates re, SharedSet<String> ancestors) {
        return ancestors.contains(resp) || re.hasCycle(resp, ancestors);
    }
}
