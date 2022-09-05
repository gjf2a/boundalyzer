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

public class Var extends Expr {
    private String name;
    
    public Var(String name) {this.name = name;}
    
    public boolean isTrue(Set<String> varValues) {
        return varValues.contains(this.toString());
    }
    
    protected void addVarsTo(VarInfo vars, boolean negated) {
        vars.add(this.toString(), negated);
    }
    
    public String toString() {return name;}
    public int hashCode() {return toString().hashCode();}
    public boolean equals(Object other) {
        return other instanceof Var && this.toString().equals(other.toString());
    }
}
