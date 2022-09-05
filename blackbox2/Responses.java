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
import util.*;

public class Responses implements Comparable<Responses> {
    private SharedSet<String> responses;
    private String str;
    
    public Responses() {
        responses = new SharedTreeSet<String>();
        str = "";
    }
    
    public Responses(String r) {
        this();
        for (String resp: r.split("\\s")) {
            responses = responses.with(resp);
        }
        str = r;
    }
    
    private Responses(SharedSet<String> responses) {
        this.responses = responses;
        str = null;
    }
    
    public boolean isImpossible() {
        return responses.contains("Impossible");
    }
    
    public SharedSet<String> responses() {return responses;}
    
    public Responses with(String resp) {
        check(resp);
        return new Responses(responses.with(resp));
    }
    
    public Responses without(String resp) {
        return new Responses(responses.without(resp));
    }
    
    public Responses withRenamedResponse(String oldResp, String newResp) {
    	if (includes(oldResp)) {
    		return new Responses(responses.without(oldResp).with(newResp));
    	} else {
    		return this;
    	}
    }
    
    public boolean includes(String resp) {
        return responses.contains(resp);
    }
    
    private void check(String resp) {
        if (resp.split("\\s").length != 1) {
            throw new IllegalArgumentException("Spaces not allowed in responses");
        }
    }
    
    public String toString() {
        if (str == null) {
            StringBuilder sb = new StringBuilder();
            for (String s: responses) {
                sb.append(s);
                sb.append(' ');
            }
            sb.delete(sb.length() - 1, sb.length());
            str = sb.toString();
        }
        return str;
    }
    
    public int compareTo(Responses that) {
        return this.toString().compareTo(that.toString());
    }
    
    public int hashCode() {return toString().hashCode();}
    
    public boolean equals(Object other) {
        if (other instanceof Responses) {
            Responses that = (Responses)other;
            return this.compareTo(that) == 0;
        } else {
            return false;
        }
    }
}
