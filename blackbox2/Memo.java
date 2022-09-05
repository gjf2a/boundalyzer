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

public class Memo {
    
    // 
    // Memoization of matches
    //
    
    // Pre: None
    // Post: Returns true if record exists for matcher.matches(arg)
    public boolean hasMatches(HistoryPattern matcher, StimulusSeq arg) {
        return pastMatch.containsKey(new MatchKey(matcher, arg));
    }
    
    // Pre: hasMatches(matcher, arg)
    // Post: Returns record for matcher.matches(arg)
    public boolean getMatches(HistoryPattern matcher, StimulusSeq arg) {
        return pastMatch.get(new MatchKey(matcher, arg));
    }
    
    // Pre: None
    // Post: hasMatches(arg); getMatches(arg) == matches
    public void storeMatches(HistoryPattern matcher, StimulusSeq arg, boolean matches) {
        pastMatch.put(new MatchKey(matcher, arg), matches);
    }

    //
    // Memoization of "count" evaluations
    //
    
    // Pre: None
    // Post: Returns true if record exists for evaluator.value(arg)
    public boolean hasValue(CountingPattern evaluator, StimulusSeq arg) {
        return pastValues.containsKey(new ValKey(evaluator, arg));
    }
    
    // Pre: hasValue(evaluator, arg)
    // Post: Returns record for evaluator.value(arg)
    public int getValue(CountingPattern evaluator, StimulusSeq arg) {
        return pastValues.get(new ValKey(evaluator, arg));
    }
    
    // Pre: None
    // Post: hasValue(evaluator, arg); getValue(evaluator, arg) == value
    public void storeValue(CountingPattern evaluator, StimulusSeq arg, int value) {
        pastValues.put(new ValKey(evaluator, arg), value);
    }
    
    //
    // Memoization of heuristic estimates
    //
    
    // Pre: None
    // Post: Returns true if record exists for estimator.heuristic(arg, f)
    public boolean hasEstimate(HistoryPattern estimator, StimulusSeq seq, Flags f) {
        return pastEstimates.containsKey(new EstKey(estimator, seq, f));
    }
    
    // Pre: hasEstimate(estimator, seq, f)
    // Post: Returns record for estimator.heuristic(arg, f)
    public int getEstimate(HistoryPattern estimator, StimulusSeq seq, Flags f) {
        return pastEstimates.get(new EstKey(estimator, seq, f));
    }
    
    // Pre: None
    // Post: hasEstimate(estimator, seq, f); 
    //       getEstimate(estimator, seq, f) == value;
    //       !isEstimating(estimator, seq, f);
    public void storeEstimate(HistoryPattern estimator, StimulusSeq seq, Flags f, int value) {
        EstKey key = new EstKey(estimator, seq, f);
        estimating.remove(key);
        pastEstimates.put(key, value);
    }
    
    // Pre: !hasEstimate(estimator, seq, f)
    // Post: isEstimating(estimator, seq, f)
    public void estimating(HistoryPattern estimator, StimulusSeq seq, Flags f) {
        if (hasEstimate(estimator, seq, f)) {
            throw new IllegalArgumentException("Already has estimate");
        }
        
        estimating.add(new EstKey(estimator, seq, f));
    }
    
    public boolean isEstimating(HistoryPattern estimator, StimulusSeq seq, Flags f) {
        return estimating.contains(new EstKey(estimator, seq, f));
    }
    
    private class Key {
        private String str;
        public Key(String s) {str = s;}
        public String toString() {return str;}
        public int hashCode() {return str.hashCode();}
        public boolean equals(Object other) {return str.equals(other.toString());}
    }
    
    private class MatchKey extends Key {
        public MatchKey(HistoryPattern sh, StimulusSeq ss) {
            super(sh.toString() + ":" + ss.toString());
        }
    }
    
    private class ValKey extends Key {
        public ValKey(CountingPattern nh, StimulusSeq ss) {
            super(nh.toString() + ":" + ss.toString());
        }
    }
    
    private class EstKey extends Key {
        public EstKey(HistoryPattern sh, StimulusSeq ss, Flags f) {
            super(sh.toString() + ":" + ss.toString() + ":" + ":" + f.toString());
        }
    }
    
    private Map<MatchKey,Boolean> pastMatch = new HashMap<MatchKey,Boolean>();
    private Map<ValKey,Integer> pastValues = new HashMap<ValKey,Integer>();
    private Map<EstKey,Integer> pastEstimates = new HashMap<EstKey,Integer>();
    private Set<EstKey> estimating = new HashSet<EstKey>();
}
