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

public class VarInfo {
    
    private final static int VAR = 1, NOT = 2, BOTH = 3;
    private Map<String,Integer> vars;
    
    public VarInfo() {
        vars = new LinkedHashMap<String,Integer>();
    }
    
    public void add(String var, boolean negated) {
        if (negated) {
            vars.put(var, (is(var) ? BOTH : NOT));
        } else {
            vars.put(var, (isNot(var) ? BOTH : VAR));
        }
    }
    
    public boolean is(String var) {
        return (vars.containsKey(var) && vars.get(var) != NOT);
    }
    
    public boolean isNot(String var) {
        return (vars.containsKey(var) && vars.get(var) != VAR);
    }
    
    public Set<String> allNames() {return vars.keySet();}
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String s: vars.keySet()) {
            sb.append(s + ": " + decode(vars.get(s)) + "\n");
        }
        return sb.toString();
    }
    
    private String decode(int which) {
        switch (which) {
            case VAR: return "VAR";
            case NOT: return "NOT";
            case BOTH: return "BOTH";
        }
        throw new IllegalArgumentException();
    }
}
