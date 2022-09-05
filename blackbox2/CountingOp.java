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

abstract public class CountingOp extends CountingPattern {
    private CountingPattern leftArg, rightArg;
    
    public CountingOp(CountingPattern left, CountingPattern right) {
        super(new AbstractHistoryPattern[]{left, right});
        leftArg = left;
        rightArg = right;
    }
    
    protected int valueHelp(StimulusSeq hist, Memo m) {
        return doOp(leftArg.value(hist, m), rightArg.value(hist, m));
    }
    
    public String toString() {
        return leftArg + " " + opStr() + " " + rightArg;
    }
    
    protected CountingPattern left() {return leftArg;}
    protected CountingPattern right() {return rightArg;}
    
    abstract public int doOp(int one, int two);
    
    abstract public String opStr();
    
    public int tableValue(CountTable ct) {
        return doOp(leftArg.tableValue(ct), rightArg.tableValue(ct));
    }
    
    public void addVarsTo(CountTable table) {
        left().addVarsTo(table);
        right().addVarsTo(table);
    }
    
    public void addVarValuesTo(CountTable table, StimulusSeq hist, Memo m) {
        left().addVarValuesTo(table, hist, m);
        right().addVarValuesTo(table, hist, m);
    }
}
