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

abstract public class CountingPattern extends AbstractHistoryPattern {
    public CountingPattern(AbstractHistoryPattern[] children) {super(children);}
    public CountingPattern() {super();}
    
    public int value(StimulusSeq hist, Memo m) {
        if (!m.hasValue(this, hist)) {
            m.storeValue(this, hist, valueHelp(hist, m));
        }
        return m.getValue(this, hist);
    }
    
    // Post: Returns value of hist determined by this
    abstract protected int valueHelp(StimulusSeq hist, Memo m);
    
    // Post: Returns value with all CountHistories retrieving their
    //       values from ct
    abstract public int tableValue(CountTable ct);
    
    // Post: Adds all CountHistory objects (either this or a descendent)
    //       to table
    abstract public void addVarsTo(CountTable table);
    
    // Post: Adds all CountHistory objects (either this or a descendent)
    //       to table, along with their computed values in hist.
    abstract public void addVarValuesTo(CountTable table, StimulusSeq hist, Memo m);
}
