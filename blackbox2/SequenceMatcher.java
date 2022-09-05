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

public class SequenceMatcher extends HistoryPattern {
    private HistoryPattern one, two;
    private static final int justOne = 1, tryBoth = 2;
    private static final int version = tryBoth;
    private static final boolean debug = false;

    public SequenceMatcher(HistoryPattern first, HistoryPattern second) {
        super(new AbstractHistoryPattern[]{first, second});
        one = first;
        two = second;
    }

    public boolean matchesHelp(StimulusSeq seq, Memo m) {
        for (int middle = 0; middle <= seq.length(); ++middle) {
            if (one.matches(seq.subseq(0, middle - 1), m) && 
                two.matches(seq.subseq(middle, seq.length() - 1), m)) {
                return true;
            }
        }
        return false;
    }
    
    public String toString() {
        return one + ":" + two;
    }
    
    protected int heuristicHelp(StimulusSeq prefix, ResponseEstimates respTable, Flags f, Memo m) {
        if (f.isNegated() && !matches(prefix, m)) {
            return 0;
        }
        
        // I don't like doing this; it seems like a hack.
        // In many situations, it will underestimate when it doesn't have to.
        // But the same applies to the alternative!
        Flags g = f.duplicate();
        g.setInclusiveStart(true);
        
        int minFromSplit = Integer.MAX_VALUE;
        for (int middle = 0; middle <= prefix.length(); ++middle) {
            StimulusSeq leftPart = prefix.subseq(0, middle - 1);
            StimulusSeq rightPart = prefix.subseq(middle, prefix.length() - 1);
            if (one.matches(leftPart, m)) {
                int est2 = two.heuristic(rightPart, respTable, g, m);
                if (debug) {
                    System.out.println("one: " + one + " two: " + two);
                    System.out.println("leftPart: " + leftPart + " rightPart: " + rightPart);
                    System.out.println("est2: " + est2);
                }
                minFromSplit = Math.min(minFromSplit, est2);
                if (debug) {System.out.println("minFromSplit: " + minFromSplit);}
            }
        }

        if (minFromSplit < Integer.MAX_VALUE) {
            return minFromSplit;
        } else {
            int estBase = one.heuristic(prefix, respTable, f, m);
            if (version == justOne) {
                return estBase;
            } else {// tryBoth
                // Seems like a good idea; but has no measurable effect!
                // Try it with Browser.bbx canForward -stim Home PageReceived
                // I think it may make a difference then...
                //g.setSkipImpossibleEstimate(true);
                int est2 = two.heuristic(prefix, respTable, g, m);
                if (g.isImpossibleEstimate(est2)) {
                    return estBase;
                } else {
                    if (debug) {
                        System.out.println("one: " + one + " two: " + two);
                        System.out.println("f: " + f);
                        System.out.println("prefix: " + prefix);
                        System.out.println("estBase: " + estBase + " est2: " + est2);
                    }
                    if (two.checksResponse()) {
                        if (debug) {
                            int max = Math.max(estBase, est2);
                            System.out.println("Returning max: " + max);
                            return max;
                        }
                        return Math.max(estBase, est2);
                    } else {
                        if (debug) {System.out.println("Returning sum");}
                        if (estBase == Integer.MAX_VALUE || est2 == Integer.MAX_VALUE) {
                            return Integer.MAX_VALUE;
                        } else {
                            return estBase + est2;
                        }
                    }
                } 
            }
        }
    }

    @Override
    public ResponseDependency getResponseDependencies(BlackBox box, boolean negated) {
        return new MultiResponseDependency(this, one.getResponseDependencies(box, negated), two.getResponseDependencies(box, negated));
    }

	@Override
	protected AbstractHistoryPattern reconstructed(ArrayList<AbstractHistoryPattern> children) {
		checkReconstructedChildren(children, 2);
		return new SequenceMatcher((HistoryPattern)children.get(0), (HistoryPattern)children.get(1));
	}
}
