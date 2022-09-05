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

package sat;
import java.util.*;

// The principal distinction between a constant and a variable
// is that the presence of both a constant and its negation
// will never extend the search.  This is because the constant
// is, well, a constant, and hence searching for an alternative
// value for it is pointless.

public class Constant extends Expr {
    private boolean value;
    
    public Constant(boolean value) {this.value = value;}

    public boolean isTrue(Set<String> varValues) {return value;}
    
    protected void addVarsTo(VarInfo vars, boolean negated) {}
}
