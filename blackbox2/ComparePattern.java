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

public class ComparePattern extends HistoryPattern {
    private CountingPattern one, two;
    private final static int lt = 0, gt = 1, le = 2, ge = 3, eq = 4, ne = 5;
    public final static String ltS = "<",  gtS = ">", leS = "<=", 
                               geS = ">=", eqS = "=", neS = "!=", altEq = "==";
    private int opcode;
    
    private final static boolean debug = false;
    
    public ComparePattern(CountingPattern left, CountingPattern right, String operator) {
        super(new AbstractHistoryPattern[]{left, right});
        one = left;
        two = right;
        if (operator.equals(ltS)) {
            opcode = lt;
        } else if (operator.equals(gtS)) {
            opcode = gt;
        } else if (operator.equals(leS)) {
            opcode = le;
        } else if (operator.equals(geS)) {
            opcode = ge;
        } else if (operator.equals(eqS) || operator.equals(altEq)) {
            opcode = eq;
        } else if (operator.equals(neS)) {
            opcode = ne;
        } else {
            throw new IllegalArgumentException("Illegal operator " + operator);
        }
    }
    
    public boolean matchesHelp(StimulusSeq seq, Memo m) {
        return comparisonTrue(seq, opcode, m);
    }
    
    // Note: For this to be admissible, the different elements being
    // counted must be completely independent of each other.  If there
    // are dependencies, then adding a single stimulus could alter the distance
    // by more than one, thus ruining admissibility.
    //
    protected int heuristicHelp(StimulusSeq prefix, ResponseEstimates respTable, Flags f, Memo m) {
        CountTable table = new CountTable();
        one.addVarValuesTo(table, prefix, m);
        two.addVarValuesTo(table, prefix, m);
        
        int deficit = computeDeficit(table, f.isNegated());
        if (deficit == 0) {
            return 0;
            
        } else if (deficit < 0) {
            throw new IllegalStateException("deficit: " + deficit);
            
        } else {
            int best = Integer.MAX_VALUE;
            
            for (CountPattern var: table.counters()) {
                table.incValue(var);
                int varDeficit = computeDeficit(table, f.isNegated());
                if (varDeficit < deficit) {
                    int estimate = var.heuristic(prefix, respTable, m);
                    if (estimate < Integer.MAX_VALUE) {
                        estimate += deficit - 1;
                        if (estimate < 0) {throw new IllegalStateException("Inside "+this+": estimate: " + estimate);}
                        if (estimate < best) {best = estimate;}
                    }
                }
                table.decValue(var);
            }

            return best;
        }
    }

    // Pre: None
    // Post: Returns the amount of positive improvement
    //       needed for prefix to be a match for this.  If prefix
    //       is already a match for this, returns zero.
    private int computeDeficit(CountTable table, boolean negated) {
        int op = getEffectiveOpcode(negated);
        int deficit = normalizedDiff(table, op);
        return Math.max(deficit, 0);
    }
    
    // Pre: None
    // Post: Returns the left-hand side dervied from the values in table
    //       after rearranging this inequality to be evaluated in the 
    //       form one - two <= 0
    //
    //       This value represents a necessary positive contribution
    //       from variables in table if it exceeds zero
    private int normalizedDiff(CountTable table, int op) {
        int diff = one.tableValue(table) - two.tableValue(table);
        switch (op) {
            case le: return diff;
            case ge: return -diff;
            case lt: return 1 + diff;
            case gt: return 1 - diff;
            case eq: return Math.abs(diff);
            case ne: return (diff == 0) ? 1 : 0;
            default: throw new IllegalStateException("Illegal opcode " + op);        
        }
    }
    
    private int getEffectiveOpcode(boolean negated) {
        if (negated) {
            switch (opcode) {
                case ne: return eq;
                case eq: return ne;
                case lt: return ge;
                case ge: return lt;
                case gt: return le;
                case le: return gt;
            }
            throw new IllegalStateException("Illegal opcode " + opcode);
        } else {
            return opcode;
        }
    }
    
    public String toString() {
        return one + " " + symToStr() + " " + two;
    }
    
    private boolean comparisonTrue(StimulusSeq hist, int effectiveOp, Memo m) {
        switch (effectiveOp) {
            case lt: return one.value(hist, m) < two.value(hist, m);
            case gt: return one.value(hist, m) > two.value(hist, m);
            case le: return one.value(hist, m) <= two.value(hist, m);
            case ge: return one.value(hist, m) >= two.value(hist, m);
            case eq: return one.value(hist, m) == two.value(hist, m);
            case ne: return one.value(hist, m) != two.value(hist, m);
            default: throw new IllegalStateException("Illegal opcode " + opcode);
        }
    }
    
    private String symToStr(int op) {
        switch (op) {
            case lt: return ltS;
            case gt: return gtS;
            case le: return leS;
            case ge: return geS;
            case eq: return eqS;
            case ne: return neS;
        }
        
        throw new IllegalStateException("Illegal opcode " + opcode);
    }
    
    private String symToStr() {
        return symToStr(opcode);
    }

    @Override
    public ResponseDependency getResponseDependencies(BlackBox box, boolean negated) {
        Memo m = new Memo();
        StimulusSeq target = new StimulusSeq(box);
        ResponseDependency result = null;
        
        CountTable table = new CountTable();
        one.addVarValuesTo(table, target, m);
        two.addVarValuesTo(table, target, m);
        
        int op = getEffectiveOpcode(negated);
        int diff = normalizedDiff(table, op);
        if (debug) {System.out.println("diff: " + diff + " [" + one + " " + symToStr(op) + " " + two + "]");}

        Set<CountPattern> unhelpfulVars = new LinkedHashSet<CountPattern>();
        for (CountPattern var: table.counters()) {
            table.incValue(var);
            int varDiff = computeDeficit(table, negated);
            if (debug) {System.out.println("var: " + var + " varDiff: " + varDiff);}
            if (varDiff < diff) {
                if (debug) {System.out.println(var + " is helpful");}
                ResponseDependency varDep = var.getExpr().getResponseDependencies(box, negated);
            
                if (result == null) {
                    result = varDep;
                } else {
                    result = new MultiResponseDependency(this, varDep, result);
                }
            } else if (varDiff > diff) {
                unhelpfulVars.add(var);
                if (debug) {System.out.println(var + " is not helpful");}
            }
            table.decValue(var);
        }
        
        if (result == null) {
            if (debug) {System.out.println(this + " would have been null");}
            if (unhelpfulVars.isEmpty()) {
                result = new SimpleResponseDependency(this, negated);
            } else {
                Iterator<CountPattern> iter = unhelpfulVars.iterator();
                CountPattern var1 = iter.next();
                result = var1.getExpr().getResponseDependencies(box, true);
                while (iter.hasNext()) {
                    CountPattern var2 = iter.next();
                    ResponseDependency var2Dep = var2.getExpr().getResponseDependencies(box, true);
                    result = new MultiResponseDependency(this, var2Dep, result);
                }
            }
        }
        return result;
    }
    
    protected AbstractHistoryPattern reconstructed(ArrayList<AbstractHistoryPattern> children) {
    	checkReconstructedChildren(children, 2);
    	return new ComparePattern((CountingPattern)children.get(0), (CountingPattern)children.get(1), symToStr(opcode));
    }
}