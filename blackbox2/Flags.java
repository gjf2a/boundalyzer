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

public class Flags {
    private boolean negated, inclusiveStart, inclusiveEnd, skipImpossibleEstimate;
    
    // Post: isNegated() == false; isInclusiveStart() == false;
    //       isInclusiveEnd() == false; isInclusive() == false;
    //       skipsImpossibleEstimate() == false
    public Flags() {
        negated = inclusiveStart = inclusiveEnd = skipImpossibleEstimate = false;
    }
    
    public Flags duplicate() {
        return new Flags(negated, inclusiveStart, inclusiveEnd, skipImpossibleEstimate);
    }
    
    // Post: isNegated() == neg; isInclusiveStart() == inclusive;
    //       isInclusiveEnd() == inclusive; isInclusive() == inclusive;
    //       skipsImpossibleEstimate() == false
    public Flags(boolean neg, boolean inclusive) {
        this(neg, inclusive, inclusive, false);
    }
    
    // Post: isNegated() == neg; isInclusiveStart() == start;
    //       isInclusiveEnd() == end; isInclusive() == (start && end);
    //       skipsImpossibleEstimate() == skipImpEst
    public Flags(boolean neg, boolean start, boolean end, boolean skipImpEst) {
        negated = neg;
        inclusiveStart = start;
        inclusiveEnd = end;
        skipImpossibleEstimate = skipImpEst;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(negated ? "y" : "n");
        sb.append("Ng:");
        sb.append(inclusiveStart ? "y" : "n");
        sb.append("St:");
        sb.append(inclusiveEnd ? "y" : "n");
        sb.append("En:");
        sb.append(skipImpossibleEstimate ? "y" : "n");
        sb.append("SI:");
        return sb.toString();
    }
    
    public boolean isNegated() {return negated;}
    public boolean isInclusiveStart() {return inclusiveStart;}
    public boolean isInclusiveEnd() {return inclusiveEnd;}
    public boolean isInclusive() {
        return isInclusiveStart() && isInclusiveEnd();
    }
    public boolean skipsImpossibleEstimate() {return skipImpossibleEstimate;}
    
    public boolean isImpossibleEstimate(int est) {
        return est == Integer.MAX_VALUE;
    }
    
    public void setNegated(boolean b) {negated = b;}
    public void setInclusiveStart(boolean b) {inclusiveStart = b;}
    public void setInclusiveEnd(boolean b) {inclusiveEnd = b;}
    public void setSkipImpossibleEstimate(boolean b) {skipImpossibleEstimate = b;}
    
    public void flipNegated() {setNegated(!isNegated());}
    public void flipInclusiveStart() {setInclusiveStart(!isInclusiveStart());}
    public void flipInclusiveEnd() {setInclusiveEnd(!isInclusiveEnd());}
    public void flipSkipImpossibleEstimate() {setSkipImpossibleEstimate(!skipsImpossibleEstimate());}
}


